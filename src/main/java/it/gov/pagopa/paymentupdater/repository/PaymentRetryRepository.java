package it.gov.pagopa.paymentupdater.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import it.gov.pagopa.paymentupdater.model.PaymentRetry;

public interface PaymentRetryRepository extends MongoRepository<PaymentRetry, String>, PagingAndSortingRepository<PaymentRetry, String>{

	@Query("{'noticeNumber':?0, 'payeeFiscalCode':?1}")
	List<PaymentRetry> getPaymentRetryByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode);
	@Query(value = "{}")
	Page<PaymentRetry> findTopElements(Pageable page);
}
