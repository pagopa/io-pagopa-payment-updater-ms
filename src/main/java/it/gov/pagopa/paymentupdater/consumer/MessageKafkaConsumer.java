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

	private CountDownLatch latch = new CountDownLatch(1);
	private String payload = null;

	@KafkaListener(topics = "${kafka.message}", groupId = "consumer-message", containerFactory = "kafkaListenerContainerFactory", autoStartup = "${message.auto.start}")
	public void messageKafkaListener(Payment paymentMessage)
			throws JsonProcessingException, InterruptedException, ExecutionException {
		log.debug("Processing messageId=" + paymentMessage.getId() + " time=" + new Date().toString()
				+ "paymentMessageContentType="
				+ paymentMessage.getContent_type());
		if (Objects.nonNull(paymentMessage) && Objects.nonNull(paymentMessage.getContent_type())
				&& paymentMessage.getContent_type().equals(MessageContentType.PAYMENT)) {
			log.debug("Received message with id: {} ", paymentMessage.getId());
			checkNullInMessage(paymentMessage);
			payload = paymentMessage.toString();
			var pp = paymentService.getPaymentByNoticeNumberAndFiscalCode(
					paymentMessage.getContent_paymentData_noticeNumber(),
					paymentMessage.getContent_paymentData_payeeFiscalCode());
			if (pp.isEmpty()) {
				String rptId = paymentMessage.getContent_paymentData_payeeFiscalCode()
						.concat(paymentMessage.getContent_paymentData_noticeNumber());
				paymentMessage.setRptId(rptId);
				Map<String, Boolean> map = paymentServiceImpl.checkPayment(rptId);
				if (map.containsKey("isPaid")) {
					paymentMessage.setPaidFlag(map.get("isPaid"));
				}
				paymentService.save(paymentMessage);
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
