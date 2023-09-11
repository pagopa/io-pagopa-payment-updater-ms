package it.gov.pagopa.paymentupdater.deserialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
public class CustomDateTimeDeserializerTest {
  private ObjectMapper mapper;
  private CustomDateTimeDeserializer deserializer;

  @Before
  public void setup() {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(new ParameterNamesModule());
    mapper.registerModule(new Jdk8Module());
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    deserializer = new CustomDateTimeDeserializer();
  }


  @Test
  public void ShouldDecodeALocalDateTimeFromANumericString_Ok() {
    String numericTimestamp = "{\"value\":\"1662588000000\"}";
    LocalDateTime localDateTime = deserializeLocalDateTimeFromString(numericTimestamp);
    Assertions.assertEquals(localDateTime, LocalDateTime.parse("2022-09-07T22:00"));
  }

  @Test
  public void ShouldDecodeALocalDateTimeFromAnISODateTimeString_Ok() {
    String numericTimestamp = "{\"value\":\"2023-08-31T12:00:00.000+00:00\"}";
    LocalDateTime localDateTime = deserializeLocalDateTimeFromString(numericTimestamp);
    Assertions.assertEquals(localDateTime, LocalDateTime.parse("2023-08-31T12:00:00.000"));
  }

  @Test
  public void ShouldThrowForUnrecognizedString_Ok() {
    String numericTimestamp = "{\"value\":\"notadate\"}";
    Assertions.assertThrows(RuntimeException.class, () -> deserializeLocalDateTimeFromString(numericTimestamp));
  }

  @SneakyThrows({JsonParseException.class, IOException.class})
  private LocalDateTime deserializeLocalDateTimeFromString(String json) {
    InputStream stream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    JsonParser parser = mapper.getFactory().createParser(stream);
    DeserializationContext ctxt = mapper.getDeserializationContext();
    parser.nextToken();
    parser.nextToken();
    parser.nextToken();
    return deserializer.deserialize(parser, ctxt);
  }

}
