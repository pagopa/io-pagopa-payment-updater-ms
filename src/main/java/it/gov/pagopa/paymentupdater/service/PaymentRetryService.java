package it.gov.pagopa.paymentupdater.service;

import java.util.List;

import it.gov.pagopa.paymentupdater.model.PaymentRetry;

public interface PaymentRetryService {
	
	List<PaymentRetry> findAll();
	
	PaymentRetry save(PaymentRetry retry);
	
	List<PaymentRetry> getPaymentRetryByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode);
	
	void delete(PaymentRetry retry);

}
