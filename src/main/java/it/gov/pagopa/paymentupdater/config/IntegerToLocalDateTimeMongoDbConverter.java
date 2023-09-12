package it.gov.pagopa.paymentupdater.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.TimeZone;

@ReadingConverter
public class IntegerToLocalDateTimeMongoDbConverter implements Converter<Integer, LocalDateTime> {

  @Override
  public LocalDateTime convert(Integer source) {
    return Optional.ofNullable(source).map(date -> LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.UTC)).orElse(null);
  }
}
