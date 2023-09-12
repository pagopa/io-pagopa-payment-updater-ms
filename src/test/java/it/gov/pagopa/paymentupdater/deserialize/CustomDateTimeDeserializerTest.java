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
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

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
  public void ShouldDecodeALocalDateTimeFromStringWithKnownFormat_Ok() {
    Stream.of(new String[][]{
      {"{\"value\":\"2023-08-31T12:00:00.000Z\"}", "2023-08-31T12:00"},
      {"{\"value\":\"2023-08-31T12:00:00.000+00.00\"}", "2023-08-31T12:00"},
      {"{\"value\":\"1662588000000\"}", "2022-09-07T22:00"},
      {"{\"value\":\"0\"}", "1970-01-01T00:00"}
    }).forEach((String[] kv) -> {
      LocalDateTime localDateTime = deserializeLocalDateTimeFromString(kv[0]);
      Assertions.assertEquals(localDateTime, LocalDateTime.parse(kv[1]));
    });
  }

  @Test
  public void ShouldThrowForUnrecognizedString_Ok() {
    Stream.of(new String[]{
      "{\"value\":\"notadatelongversion\"}",
      "{\"value\":\"notadate\"}"
    }).forEach((String v) -> {
      Assertions.assertThrows(DateTimeParseException.class, () -> deserializeLocalDateTimeFromString(v));
    });
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
