package it.gov.pagopa.paymentupdater.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.gov.pagopa.paymentupdater.model.Payment;

public interface PaymentService {

	Optional<Payment> getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode);

	void save(Payment reminder);

	Map<String, String> checkPayment(Payment payment) throws JsonProcessingException, InterruptedException, ExecutionException;

	Optional<Payment> findById(String messageId);

}
