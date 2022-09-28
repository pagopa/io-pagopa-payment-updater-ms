package it.gov.pagopa.paymentupdater.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import it.gov.pagopa.paymentupdater.util.PaymentUtil;

@ReadingConverter
public class LongToLocalDateTimeMongoDbConverter implements Converter<Long, LocalDateTime> {

  @Override
  public LocalDateTime convert(Long source) {
	  //getLocalDateTime() is mandatory to avoid server time zone problems
	 return Optional.ofNullable(source).map(date -> PaymentUtil.getLocalDateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(source),
              TimeZone.getDefault().toZoneId()).toLocalDate())).orElse(null);
  }
}