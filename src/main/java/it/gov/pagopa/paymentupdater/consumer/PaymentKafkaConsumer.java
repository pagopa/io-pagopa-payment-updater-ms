package it.gov.pagopa.paymentupdater.consumer;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.event.RetryOnErrorEvent;
import it.gov.pagopa.paymentupdater.dto.PaymentMessage;
import it.gov.pagopa.paymentupdater.dto.payments.PaymentRoot;
import it.gov.pagopa.paymentupdater.model.PaymentRetry;
import it.gov.pagopa.paymentupdater.producer.PaymentProducer;
import it.gov.pagopa.paymentupdater.service.PaymentRetryService;
import it.gov.pagopa.paymentupdater.service.PaymentService;
import it.gov.pagopa.paymentupdater.util.TelemetryCustomEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentKafkaConsumer {

	@Autowired
	PaymentService paymentService;	
	@Autowired
	PaymentRetryService paymentRetryService;	
	@Autowired
	@Qualifier("kafkaTemplatePayments")
	private KafkaTemplate<String, String> kafkaTemplatePayments;	
	@Autowired
	PaymentProducer producer;	
	@Autowired
	ObjectMapper mapper;	
	@Value("${kafka.paymentupdates}")
	private String producerTopic;	
	@Value("${interval.function}")
	private int intervalFunction;
	@Value("${attempts.max}")
	private int attemptsMax;

	private CountDownLatch latch = new CountDownLatch(1);
    @Transactional
	@KafkaListener(topics = "${kafka.payment}", groupId = "consumer-Payment", containerFactory = "kafkaListenerContainerFactoryPaymentRoot")
	public void paymentKafkaListener(PaymentRoot root) throws JsonProcessingException {
		log.debug("Received payment-root: {} ", root);
		if (Objects.nonNull(root) && Objects.nonNull(root.getDebtorPosition()) && Objects.nonNull(root.getDebtorPosition().getNoticeNumber())){
			PaymentMessage message = new PaymentMessage();
			message.setSource("payments");
			message.setNoticeNumber(root.getDebtorPosition().getNoticeNumber());
			message.setPayeeFiscalCode(root.getCreditor() != null ? root.getCreditor().getIdPA() : null);
			message.setPaid(true);
	
			var maybeReminderToSend = paymentService.getPaymentByNoticeNumberAndFiscalCode(message.getNoticeNumber(),
					message.getPayeeFiscalCode());
			if (maybeReminderToSend.isPresent()) {
				var reminderToSend = maybeReminderToSend.get();
				reminderToSend.setPaidFlag(true);
				paymentService.save(reminderToSend); 
	
				message.setFiscalCode(reminderToSend.getFiscal_code());
				message.setMessageId(reminderToSend.getId());
	
				sendPaymentUpdateWithRetry(mapper.writeValueAsString(message));
			} else {
				log.info("Not found reminder in payment data with notice number: {}", message.getNoticeNumber());
			}
		}
		this.latch.countDown();
	}
	
	private void sendPaymentUpdateWithRetry(String message) {
		IntervalFunction intervalFn = IntervalFunction.of(intervalFunction);
		RetryConfig retryConfig = RetryConfig.custom()
				.maxAttempts(attemptsMax)			
				.intervalFunction(intervalFn)
				.build();
		Retry retry = Retry.of("sendNotificationWithRetry", retryConfig);
		Function<Object, Object> sendReminderFn = Retry.decorateFunction(retry, 
				notObj -> {
					try {				
						return producer.sendReminder(message, kafkaTemplatePayments, producerTopic);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
		Retry.EventPublisher publisher = retry.getEventPublisher();
		publisher.onError(event -> {
			if (event.getNumberOfRetryAttempts() == attemptsMax) {
				//when max attempts are reached
				PaymentRetry retryMessage = messageToRetry(message);
				List<PaymentRetry> paymentList = paymentRetryService.getPaymentRetryByNoticeNumberAndFiscalCode(retryMessage.getNoticeNumber(), retryMessage.getPayeeFiscalCode());
				if (Objects.nonNull(retryMessage) && paymentList.isEmpty()) {
					paymentRetryService.save(retryMessage);
					TelemetryCustomEvent.writeTelemetry("ErrorSendPaymentUpdate", new HashMap<>(), getErrorMap(retryMessage, event));
				}								
			}
		});
		sendReminderFn.apply(message);
	}
	
	private PaymentRetry messageToRetry(String message) {
		try {
			return mapper.readValue(message, PaymentRetry.class);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage());
		}
		return null;
	}
	
	private Map<String, String> getErrorMap(PaymentRetry message, RetryOnErrorEvent event ) {
		Map<String, String> properties = new HashMap<>();
		properties.put(message.getNoticeNumber(), " Call failed after maximum number of attempts");
		properties.put("time", event.getCreationTime().toString());
		if (Objects.nonNull(event.getLastThrowable().getMessage())) 
				properties.put("message", event.getLastThrowable().getMessage());
		if (Objects.nonNull(event.getLastThrowable().getCause())) 
				properties.put("cause", event.getLastThrowable().getCause().toString());		
		return properties;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

}
