package it.gov.pagopa.paymentupdater.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMessage {

	String messageId;
	String noticeNumber;
	String payeeFiscalCode;
	boolean paid;
	Long dueDate;
	double amount;
	String source;
	String fiscalCode;
}
