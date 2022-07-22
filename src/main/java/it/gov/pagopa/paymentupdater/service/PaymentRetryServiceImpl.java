package it.gov.pagopa.paymentupdater.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.gov.pagopa.paymentupdater.model.PaymentRetry;
import it.gov.pagopa.paymentupdater.repository.PaymentRetryRepository;
@Service
@Transactional
public class PaymentRetryServiceImpl implements PaymentRetryService {
	
	@Autowired PaymentRetryRepository paymentRetryRepository;

	@Override
	public List<PaymentRetry> findAll() {
		return paymentRetryRepository.findAll();
	}

	@Override
	public PaymentRetry save(PaymentRetry retry) {
		return paymentRetryRepository.save(retry);
	}

	@Override
	public List<PaymentRetry> getPaymentRetryByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode) {
		return paymentRetryRepository.getPaymentRetryByNoticeNumberAndFiscalCode(noticeNumber, fiscalCode);
	}

	@Override
	public void delete(PaymentRetry retry) {
		 paymentRetryRepository.delete(retry);
	}

}
