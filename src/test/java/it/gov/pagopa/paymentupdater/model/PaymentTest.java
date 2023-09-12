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
import java.util.HashMap;
import java.util.stream.Stream;

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
  public void ShouldDecodeAPaymentWithKnownFormatDueDate_Ok() {
    Stream.of(new String[][]{
      {"2023-08-31T12:00:00.000Z", "2023-08-31T12:00"},
      {"2023-08-31T12:00:00.000+00.00", "2023-08-31T12:00"},
      {"1662588000000", "2022-09-07T22:00"},
      {"0", "1970-01-01T00:00"}
    }).forEach((String[] kv) -> {
      String paymentJson = JsonModels.getPaymentWithGivenDueDate(kv[0]);
      Payment payment = null;
      try {
        payment = mapper.readValue(paymentJson, Payment.class);
      } catch (JsonProcessingException e) {
        // should never happen for well formatted dates
        throw new RuntimeException(e);
      }
      Assertions.assertEquals(payment.getDueDate(), LocalDateTime.parse(kv[1]));
    });
  }

  @Test
  public void ShouldFailDecodingAPaymentWithWrongFormatDueDate_Ok() {
    Stream.of(new String[]{
      "notadatelongversion",
      "notadate"
    }).forEach((String v) -> {
      String paymentJson = JsonModels.getPaymentWithGivenDueDate(v);
      Assertions.assertThrows(JsonMappingException.class, () -> mapper.readValue(paymentJson, Payment.class));
    });
  }

}
