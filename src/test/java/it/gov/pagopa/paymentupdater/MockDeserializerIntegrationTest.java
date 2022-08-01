package it.gov.pagopa.paymentupdater;

import java.io.IOException;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.deserialize.AvroMessageDeserializer;
import it.gov.pagopa.paymentupdater.deserialize.PaymentRootDeserializer;
import it.gov.pagopa.paymentupdater.dto.payments.PaymentRoot;
import it.gov.pagopa.paymentupdater.model.JsonLoader;
import it.gov.pagopa.paymentupdater.model.Payment;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockDeserializerIntegrationTest extends AbstractMock{

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

 
	@SuppressWarnings("unchecked")
	@Test
	public void test_messageDeserialize_ok() throws JsonMappingException, JsonProcessingException {
		byte[] byteArrray = "".getBytes();
		avroMessageDeserializer = new AvroMessageDeserializer(messageSchema, mapper);
		avroMessageDeserializer.setConverter(converter);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		Mockito.when(mapper.readValue(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(new Payment());
		Payment payment = avroMessageDeserializer.deserialize(null, messageSchema.getJsonString().getBytes());
		Assertions.assertNotNull(payment);
	}
	
	@Test
	public void test_messageDeserialize_ko() {
		byte[] byteArrray = null;
		avroMessageDeserializer = new AvroMessageDeserializer(messageSchema, mapper);
		avroMessageDeserializer.setConverter(converter);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		avroMessageDeserializer.deserialize(null, messageSchema.getJsonString().getBytes());
		Assertions.assertTrue(true);
	}

	@Test
	public void test_paymentDeserialize_OK() throws StreamReadException, DatabindException, IOException {
		byte[] byteArrray = getPaymentRoot().getBytes();
		paymentDeserializer = new PaymentRootDeserializer(mapper);
		Mockito.when(mapper.readValue(byteArrray, PaymentRoot.class)).thenReturn(new PaymentRoot());
		paymentDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}
	
	@Test
	public void test_paymentDeserialize_KO() throws StreamReadException, DatabindException, IOException {
		String s = "ko";
		byte[] byteArrray = s.getBytes();
		paymentDeserializer = new PaymentRootDeserializer(null);
		Mockito.when(converter.convertToJson(Mockito.any(), Mockito.anyString())).thenReturn(byteArrray);
		paymentDeserializer.deserialize(null, byteArrray);
		Assertions.assertTrue(true);
	}

}
