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
	 return Optional.ofNullable(source).map(date -> LocalDateTime.ofInstant(Instant.ofEpochSecond(source),
              TimeZone.getDefault().toZoneId())).orElse(null);
  }
}