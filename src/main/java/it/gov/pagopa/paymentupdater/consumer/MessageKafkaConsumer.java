package it.gov.pagopa.paymentupdater.consumer;

import static it.gov.pagopa.paymentupdater.util.PaymentUtil.checkNullInMessage;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.MessageContentType;
import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.service.PaymentService;
import it.gov.pagopa.paymentupdater.service.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageKafkaConsumer {

	@Autowired
	PaymentService paymentService;

	@Autowired
	PaymentServiceImpl paymentServiceImpl;

	@Autowired
	ObjectMapper mapper;

	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = null;

	@KafkaListener(topics = "${kafka.message}", groupId = "consumer-message", containerFactory = "kafkaListenerContainerFactory", autoStartup = "${message.auto.start}")
	public void messageKafkaListener(Payment reminder)
			throws JsonProcessingException, InterruptedException, ExecutionException {
		log.info("Processing messageId=" + reminder.getId() + " time=" + new Date().toString());
		if (Objects.nonNull(reminder) && Objects.nonNull(reminder.getContent_type())
				&& reminder.getContent_type().equals(MessageContentType.PAYMENT)) {
			log.debug("Received message with id: {} ", reminder.getId());
			checkNullInMessage(reminder);
			payload = reminder.toString();
			var pp = paymentService.getPaymentByNoticeNumberAndFiscalCode(
					reminder.getContent_paymentData_noticeNumber(), reminder.getContent_paymentData_payeeFiscalCode());
			if (pp.isEmpty()) {
				String rptId = reminder.getContent_paymentData_payeeFiscalCode()
						.concat(reminder.getContent_paymentData_noticeNumber());
				reminder.setRptId(rptId);
				Map<String, Boolean> map = paymentServiceImpl.checkPayment(rptId);
				if (map.containsKey("isPaid")) {
					reminder.setPaidFlag(map.get("isPaid"));
				}
				paymentService.save(reminder);
			}
		}

		this.latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public String getPayload() {
		return payload;
	}

}
