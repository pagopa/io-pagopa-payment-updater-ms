package it.gov.pagopa.paymentupdater.deserialize;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Objects;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.dto.payments.PaymentRoot;
import it.gov.pagopa.paymentupdater.util.PaymentUtil;
import it.gov.pagopa.paymentupdater.util.TelemetryCustomEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentRootDeserializer implements Deserializer<PaymentRoot> {

	ObjectMapper mapper;

	public PaymentRootDeserializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public  PaymentRoot deserialize(String s, byte[] bytes) {
		PaymentRoot paymentRoot = null;
		try {
			paymentRoot = mapper.readValue(bytes, PaymentRoot.class);
			if (Objects.isNull(paymentRoot.getDebtorPosition()) || Objects.isNull(paymentRoot.getCreditor())) throw new JsonParseException();
		} catch (Exception e) {
			log.error("Error in deserializing the PaymentRoot for consumer payment-updates");
			log.error(e.getMessage());
			handleErrorPaymentMessage(bytes, e);
		}
		return paymentRoot;
	}
	
	
	private void handleErrorPaymentMessage(byte[] bytes, Exception e) {
		try {
			String message = new String(bytes, StandardCharsets.UTF_8);
			log.error("The error paymentMessage: {}", message);
			TelemetryCustomEvent.writeTelemetry("ErrorDeserializingPayment", new HashMap<>(), PaymentUtil.getErrorMap(message));
		} catch (Exception e1) {
			log.error(e1.getMessage());
		}
	}
	
	

}
