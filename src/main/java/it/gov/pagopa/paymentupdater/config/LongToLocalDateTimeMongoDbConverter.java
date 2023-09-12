package it.gov.pagopa.paymentupdater.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class LongToLocalDateTimeMongoDbConverter implements Converter<Long, LocalDateTime> {

  @Override
  public LocalDateTime convert(Long source) {
    if (source > 9999999999L) {
      // timestamp greater than 10 digits are in milliseconds or microseconds
      // we have to take just the seconds because the greatest 10 digits seconds
      // based timestamp (9999999999) will represent Saturday 20 November 2286 17:46:39
      String stringified = String.valueOf(source);
      stringified = stringified.substring(0, 10);
      source = Long.valueOf(stringified);
    }
    Long secondsTimestamp = source;
    return Optional.ofNullable(source).map(date -> LocalDateTime.ofInstant(Instant.ofEpochSecond(secondsTimestamp),
      TimeZone.getDefault().toZoneId())).orElse(null);
  }
}
