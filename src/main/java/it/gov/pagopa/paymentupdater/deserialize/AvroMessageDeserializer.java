package it.gov.pagopa.paymentupdater.deserialize;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.model.JsonLoader;
import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.util.PaymentUtil;
import it.gov.pagopa.paymentupdater.util.TelemetryCustomEvent;
import lombok.extern.slf4j.Slf4j;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;
@Slf4j
public class AvroMessageDeserializer implements Deserializer<Payment> {

	JsonLoader schema;
	ObjectMapper mapper;
	JsonAvroConverter converter;

	public void setConverter(JsonAvroConverter converter) {
		this.converter = converter;
	}



	public AvroMessageDeserializer(JsonLoader jsSchema, ObjectMapper objMapper) {
		schema = jsSchema;
		mapper = objMapper;
	}

	@Override
	public Payment deserialize(String topic, byte[] bytes) {	
		Payment returnObject = null;
		if (bytes != null) {
			try {
				byte[] binaryJson = converter.convertToJson(bytes, schema.getJsonString());
				String avroJson = new String(binaryJson);
				returnObject = mapper.readValue(avroJson, Payment.class);
				if (StringUtils.isEmpty(returnObject.getContent_paymentData_noticeNumber()) || StringUtils.isEmpty(returnObject.getContent_paymentData_payeeFiscalCode())) throw new JsonParseException();
			}catch(Exception e) {
				log.error("Error in deserializing the Reminder for consumer message");
				log.error(e.getMessage());
				handleErrorMessage(bytes, e);
			}

		}

		return returnObject;
	}
	
	private void handleErrorMessage(byte[] bytes, Exception e) {
		try {
			String message = new String(bytes, StandardCharsets.UTF_8);
			log.error("The error Message: {}", message);
			TelemetryCustomEvent.writeTelemetry("ErrorDeserializingMessage", new HashMap<>(), PaymentUtil.getErrorMap(message));
		} catch (Exception e1) {
			log.error(e1.getMessage());
		}
	}

}
