package it.gov.pagopa.paymentupdater.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.gov.pagopa.paymentupdater.dto.ProxyResponse;
import it.gov.pagopa.paymentupdater.model.Payment;

public interface PaymentService {

	Optional<Payment> getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode);

	void save(Payment reminder);

	ProxyResponse checkPayment(Payment payment) throws JsonProcessingException, InterruptedException, ExecutionException;

	Optional<Payment> findById(String messageId);
	
	List<Payment> getPaymentsByRptid(String rptid);

	int countFindById(String id);

}
