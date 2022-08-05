package it.gov.pagopa.paymentupdater;

import java.io.IOException;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.io.output.ByteArrayOutputStream;

import dto.MessageContentType;
import dto.message;
import it.gov.pagopa.paymentupdater.deserialize.AvroMessageDeserializer;
import it.gov.pagopa.paymentupdater.deserialize.PaymentRootDeserializer;
import it.gov.pagopa.paymentupdater.dto.payments.PaymentRoot;
import it.gov.pagopa.paymentupdater.exception.AvroDeserializerException;
import it.gov.pagopa.paymentupdater.model.JsonLoader;
import it.gov.pagopa.paymentupdater.model.Payment;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockDeserializerIntegrationTest extends AbstractMock {

	@MockBean
	JsonAvroConverter converter;

	@Mock
	ObjectMapper mapper;

	@InjectMocks
	AvroMessageDeserializer avroMessageDeserializer = null;

	@InjectMocks
	PaymentRootDeserializer paymentDeserializer = null;

	@Autowired
	@Qualifier("messageSchema")
	JsonLoader messageSchema;

	@Test
	public void test_messageDeserialize_ok() throws JsonMappingException, JsonProcessingException, IOException {
		avroMessageDeserializer = new AvroMessageDeserializer();
		message paymentMessage = new message();
		paymentMessage.setId("ID");
		paymentMessage.setFiscalCode("A_FISCAL_CODE");
		paymentMessage.setSenderServiceId("ASenderServiceId");
		paymentMessage.setSenderUserId("ASenderUserId");
		paymentMessage.setContentType(MessageContentType.PAYMENT);
		paymentMessage.setContentSubject("ASubject");
		paymentMessage.setContentPaymentDataNoticeNumber("test");
		paymentMessage.setContentPaymentDataPayeeFiscalCode("test");
		DatumWriter<message> writer = new SpecificDatumWriter<>(
				message.class);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Encoder encoder = EncoderFactory.get().binaryEncoder(bos, null);
		writer.write(paymentMessage, encoder);
		encoder.flush();
		Payment payment = avroMessageDeserializer.deserialize(null, bos.toByteArray());
		Assertions.assertNotNull(payment);
	}

	@Test
	public void test_messageDeserialize_ko() {
		Assertions.assertThrows(AvroDeserializerException.class,
				() -> avroMessageDeserializer.deserialize(null, messageSchema.getJsonString().getBytes()));
	}

	@Test
	public void test_paymentDeserialize_OK() throws StreamReadException, DatabindException, IOException {
		PaymentRoot paymentRoot = getPaymentRootObject();
		byte[] byteArray = getPaymentRoot().getBytes();
		paymentDeserializer = new PaymentRootDeserializer(mapper);
		Mockito.when(mapper.readValue(byteArray, PaymentRoot.class)).thenReturn(paymentRoot);
		PaymentRoot deserialized = paymentDeserializer.deserialize(null, byteArray);
		Assertions.assertNotNull(deserialized);
	}

	@Test
	public void test_paymentDeserialize_KO() throws StreamReadException, DatabindException, IOException {
		String s = "ko";
		byte[] byteArray = s.getBytes();
		paymentDeserializer = new PaymentRootDeserializer(null);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArray);
		Assertions.assertThrows(DeserializationException.class,
				() -> paymentDeserializer.deserialize(null, byteArray));
	}

}
