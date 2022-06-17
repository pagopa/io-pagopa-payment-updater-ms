package it.gov.pagopa.paymentupdater.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties
@Document(collection = "#{@collectionName}")
@ToString
@CompoundIndexes({
    @CompoundIndex(name="uniquePayment", def = "{'content_paymentData_noticeNumber' : 1,'content_paymentData_payeeFiscalCode' : 1}" ,
    		unique = true, sparse = true)})
public class Payment extends Message{

	private boolean readFlag;
	private boolean paidFlag;
	@CreatedDate
	private LocalDateTime insertionDate;
	private List<LocalDateTime> dateReminder;
	private LocalDateTime lastDateReminder;
	private int maxReadMessageSend;
	private int maxPaidMessageSend;
	private LocalDateTime readDate;
	private LocalDateTime paidDate;

}