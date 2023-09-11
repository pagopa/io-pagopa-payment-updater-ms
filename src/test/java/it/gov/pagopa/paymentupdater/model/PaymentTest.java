package it.gov.pagopa.paymentupdater.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import it.gov.pagopa.paymentupdater.utils.JsonModels;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)

public class PaymentTest {

  private ObjectMapper mapper;

  @Before
  public void setup() {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new ParameterNamesModule());
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }


  @Test
  public void ShouldDecodeAPaymentWithUTCDueDate_Ok() throws JsonProcessingException {
    String paymentJson = JsonModels.getPaymentWithGivenDueDate("2023-08-31T12:00:00.000Z");
    Payment payment = mapper.readValue(paymentJson, Payment.class);
    Assertions.assertEquals(payment.getDueDate(), LocalDateTime.parse("2023-08-31T12:00"));
  }

  @Test
  public void ShouldDecodeAPaymentWithUTCTimezoneDueDate_Ok() throws JsonProcessingException {
    String paymentJson = JsonModels.getPaymentWithGivenDueDate("2023-08-31T12:00:00.000+00.00");
    Payment payment = mapper.readValue(paymentJson, Payment.class);
    Assertions.assertEquals(payment.getDueDate(), LocalDateTime.parse("2023-08-31T12:00"));
  }

  @Test
  public void ShouldDecodeAPaymentWithNumericDueDate_Ok() throws JsonProcessingException {
    String paymentJson = JsonModels.getPaymentWithGivenDueDate("1662588000000");
    Payment payment = mapper.readValue(paymentJson, Payment.class);
    Assertions.assertEquals(payment.getDueDate(), LocalDateTime.parse("2022-09-07T22:00"));
  }

  @Test
  public void ShouldFailDecodingAPaymentWithWrongFormatDueDate_Ok() throws JsonProcessingException {
    String paymentJson = JsonModels.getPaymentWithGivenDueDate("notadate");
    Assertions.assertThrows(JsonMappingException.class, () -> mapper.readValue(paymentJson, Payment.class));
  }

}
