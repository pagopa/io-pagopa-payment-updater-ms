package it.gov.pagopa.paymentupdater.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.gov.pagopa.paymentupdater.deserialize.CustomDateTimeDeserializer;
import org.springframework.data.annotation.Id;

import dto.FeatureLevelType;
import dto.MessageContentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("java:S116")
public class Message {

	@Id
	protected String id;
	protected String senderServiceId;
	protected String senderUserId="undefined";
	protected int timeToLiveSeconds;
  @JsonDeserialize(using = CustomDateTimeDeserializer.class)
  private LocalDateTime dueDate;
	protected long createdAt;
	protected boolean isPending = true;
	protected String content_subject;
	protected MessageContentType content_type;
	protected double content_paymentData_amount;
	protected String content_paymentData_noticeNumber;
	protected boolean content_paymentData_invalidAfterDueDate;
	protected String content_paymentData_payeeFiscalCode;
	protected String fiscalCode;
	protected FeatureLevelType feature_level_type;

}
