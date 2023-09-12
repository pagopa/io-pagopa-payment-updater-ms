package it.gov.pagopa.paymentupdater.deserialize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class CustomDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
  /**
   * The main assumption for this deserializer is that Payment Updater manages
   * only UTC date times.
   * It transforms millisecond based timestamps to second based timestamp by
   * removing all digits above 10th position and it simplifies ISO strings by
   * removing the timezone since it is UTC based.
   *
   * @param jsonParser
   * @param deserializationContext
   * @return
   * @throws IOException when a unmanaged format is present
   */
  @Override
  public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, DateTimeParseException {
    String modelDateTime = jsonParser.getValueAsString();
    LocalDateTime parsedTimeTime;
    try {
      // we parse the local date time from the milliseconds by taking the seconds
      // and considering UTC timezone
      String dataToParse = modelDateTime.length() > 10 ? modelDateTime.substring(0, 10) : modelDateTime;
      parsedTimeTime = LocalDateTime.ofEpochSecond(Long.parseLong(dataToParse), 0, ZoneOffset.UTC);
    } catch (NumberFormatException nfex) {
      try {
        // we parse the local date time removing the timezone
        String dataToParse = modelDateTime.length() > 23 ? modelDateTime.substring(0, 23) : modelDateTime;
        parsedTimeTime = LocalDateTime.parse(dataToParse);
      } catch (DateTimeParseException dtpex) {
        log.error(dtpex.getMessage());
        log.error("Found a not managed format for a date: " + modelDateTime);
        throw dtpex;
      }
    }
    return parsedTimeTime;
  }
}
