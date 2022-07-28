package it.gov.pagopa.paymentupdater;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.dto.PaymentMessage;
import it.gov.pagopa.paymentupdater.dto.avro.MessageContentType;
import it.gov.pagopa.paymentupdater.dto.avro.MessageFeatureLevelType;
import it.gov.pagopa.paymentupdater.dto.payments.Creditor;
import it.gov.pagopa.paymentupdater.dto.payments.Debtor;
import it.gov.pagopa.paymentupdater.dto.payments.DebtorPosition;
import it.gov.pagopa.paymentupdater.dto.payments.Payer;
import it.gov.pagopa.paymentupdater.dto.payments.PaymentInfo;
import it.gov.pagopa.paymentupdater.dto.payments.PaymentRoot;
import it.gov.pagopa.paymentupdater.dto.payments.Psp;
import it.gov.pagopa.paymentupdater.dto.request.ProxyPaymentResponse;
import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.model.PaymentRetry;
import it.gov.pagopa.paymentupdater.repository.PaymentRepository;

public abstract class AbstractMock {
	
	@Autowired
	ObjectMapper mapper;

	@Rule
	public MockitoRule rule = MockitoJUnit.rule();

	@MockBean
	protected RestTemplate restTemplate;

	@MockBean
	protected PaymentRepository mockRepository;

	protected void mockSaveWithResponse(Payment returnReminder) {
		Mockito.when(mockRepository.save(Mockito.any(Payment.class))).thenReturn(returnReminder);
	}

	protected void mockFindIdWithResponse(Payment returnReminder1) {
		Mockito.when(mockRepository.findById(Mockito.anyString())).thenReturn(Optional.of(returnReminder1));
	}

	public void mockGetPaymentByNoticeNumberAndFiscalCodeWithResponse(Payment reminder) {
		Mockito.when(mockRepository.getPaymentByNoticeNumberAndFiscalCode(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(Optional.of(reminder));
	}

	public void mockGetPaymentByNoticeNumber(Payment reminder) {
		Mockito.when(mockRepository.getPaymentByRptId(Mockito.anyString())).thenReturn(reminder);
	}

	protected Payment selectReminderMockObject(String type, String id, String contentType, String fiscalCode,
			int numReminder) {
		Payment returnReminder1 = null;
		returnReminder1 = new Payment();
		returnReminder1.setId(id);
		returnReminder1.setContent_type(MessageContentType.valueOf(contentType));
		returnReminder1.setFiscal_code(fiscalCode);
		returnReminder1.setContent_paymentData_dueDate(LocalDate.now());
		return returnReminder1;

	}

	protected String selectPaymentMessageObject(String type, String messageId, String noticeNumber,
			String payeeFiscalCode, boolean paid, LocalDate dueDate, double amount, String source, String fiscalCode) throws JsonProcessingException {
		PaymentMessage paymentMessage = null;
		paymentMessage = new PaymentMessage(messageId, noticeNumber, payeeFiscalCode, paid, dueDate, amount, source,
				fiscalCode);
		return mapper.writeValueAsString(paymentMessage);
	}

	protected ProxyPaymentResponse getProxyResponse() {
		ProxyPaymentResponse paymentResponse = new ProxyPaymentResponse();
		paymentResponse.setCodiceContestoPagamento("");
		paymentResponse.setImportoSingoloVersamento("20");
		paymentResponse.setDetail_v2("PPT_RPT_DUPLICATA");
		paymentResponse.setDetail("");
		paymentResponse.setInstance("");
		paymentResponse.setStatus(500);
		paymentResponse.setType("");
		paymentResponse.setTitle("");
		return paymentResponse;
	}
	
	protected PaymentRetry getPaymentRetry() {
		PaymentRetry retry = new PaymentRetry();
		retry.setAmount(0);
		retry.setId("1");
		retry.setNoticeNumber("abc");
		retry.setPaid(true);
		retry.setMessageId("123");
		retry.setPayeeFiscalCode("ABC");
		retry.setSource("payments");
		Assertions.assertEquals(0, retry.getAmount());
		Assertions.assertEquals("1", retry.getId());
		Assertions.assertEquals("abc", retry.getNoticeNumber());
		Assertions.assertEquals(true, retry.isPaid());
		Assertions.assertEquals("123", retry.getMessageId());
		Assertions.assertEquals("ABC", retry.getPayeeFiscalCode());
		Assertions.assertEquals("payments", retry.getSource());
		return retry;
	}

	protected String getPaymentRoot() {
		PaymentRoot pr = new PaymentRoot();
		Creditor creditor = new Creditor();
		DebtorPosition debtorPosition = new DebtorPosition();
		Payer payer = new Payer();
		PaymentInfo info = new PaymentInfo();
		Psp psp = new Psp();
		creditor.setIdPA("test");
		creditor.setCompanyName("test");
		creditor.setIdBrokerPA("");
		creditor.setIdStation("");
		pr.setCreditor(creditor);
		Debtor debtor = new Debtor();
		debtor.setFullName("test");
		debtor.setEntityUniqueIdentifierType("");
		debtor.setEntityUniqueIdentifierValue("");
		pr.setDebtor(debtor);
		pr.setComplete("test");
		debtorPosition.setNoticeNumber("A1234");
		pr.setDebtorPosition(debtorPosition);
		pr.setMissingInfo(new ArrayList<>());
		pr.setIdPaymentManager("1234");
		psp.setIdChannel("1234");
		psp.setPsp("test");
		psp.setIdPsp("test");
		pr.setPsp(psp);
		pr.setUuid("123");
		pr.setVersion("");
		info.setAmount("123");
		info.setDueDate("9999/12/31");
		info.setFee("123");
		info.setTotalNotice("");
		info.setApplicationDate("");
		info.setPaymentDateTime("");
		info.setPaymentMethod("");
		info.setPaymentToken("");
		info.setTouchpoint("");
		info.setTransferDate("");
		pr.setPaymentInfo(info);
		payer.setFullName("test");
		payer.setEntityUniqueIdentifierValue("");
		payer.setEntityUniqueIdentifierType("");
		pr.setPayer(payer);
		Assertions.assertEquals("test", creditor.getIdPA());
		Assertions.assertEquals("test", creditor.getCompanyName());
		Assertions.assertEquals("", creditor.getIdBrokerPA());
		Assertions.assertEquals("", creditor.getIdStation());
		Assertions.assertEquals("test", debtor.getFullName());
		Assertions.assertEquals("", debtor.getEntityUniqueIdentifierType());
		Assertions.assertEquals("", debtor.getEntityUniqueIdentifierValue());
		Assertions.assertEquals("A1234", debtorPosition.getNoticeNumber());
		Assertions.assertEquals("test", pr.getComplete());
		Assertions.assertEquals("1234", pr.getIdPaymentManager());
		Assertions.assertEquals("test", pr.getComplete());
		Assertions.assertEquals("test", psp.getPsp());
		Assertions.assertEquals("1234", psp.getIdChannel());
		Assertions.assertEquals("test", psp.getIdPsp());
		Assertions.assertEquals("test", payer.getFullName());
		Assertions.assertEquals("123", info.getAmount());
		return pr.toString();
	}

	protected Payment getTestReminder() {
		Payment reminder = new Payment();
		reminder.setReadFlag(true);
		reminder.setDateReminder(new ArrayList<>());
		reminder.setLastDateReminder(LocalDateTime.of(2022, 01, 01, 1, 1));
		reminder.setMaxPaidMessageSend(10);
		reminder.setReadDate(LocalDateTime.of(2022, 01, 01, 1, 1));
		reminder.setMaxReadMessageSend(10);
		reminder.setContent_paymentData_amount(0.0);
		reminder.setContent_paymentData_invalidAfterDueDate(true);
		reminder.setContent_paymentData_payeeFiscalCode("");
		reminder.setContent_subject("");
		reminder.setCreatedAt(1l);
		reminder.setPending(false);
		reminder.setSenderServiceId("");
		reminder.setTimestamp(1l);
		reminder.setContent_paymentData_amount(0.0);
		reminder.setTimeToLiveSeconds(5);
		reminder.setContent_paymentData_noticeNumber("");
		reminder.setFeatureLevelType(MessageFeatureLevelType.ADVANCED);
		reminder.setSenderServiceId("");
		reminder.setSenderUserId("");
		getPaymentRetry();
		Assertions.assertEquals(1l, reminder.getCreatedAt());
		Assertions.assertEquals(MessageFeatureLevelType.ADVANCED, reminder.getFeatureLevelType());
		Assertions.assertEquals(true, reminder.isReadFlag());
		Assertions.assertEquals(new ArrayList<>(), reminder.getDateReminder());
		Assertions.assertEquals(LocalDateTime.of(2022, 01, 01, 1, 1), reminder.getLastDateReminder());
		Assertions.assertEquals(10, reminder.getMaxPaidMessageSend());
		Assertions.assertEquals(LocalDateTime.of(2022, 01, 01, 1, 1), reminder.getReadDate());
		Assertions.assertEquals(10, reminder.getMaxReadMessageSend());
		Assertions.assertEquals(0.0, reminder.getContent_paymentData_amount());
		Assertions.assertEquals(true, reminder.isContent_paymentData_invalidAfterDueDate());
		Assertions.assertEquals("", reminder.getContent_paymentData_payeeFiscalCode());
		Assertions.assertEquals("", reminder.getSenderUserId());
		Assertions.assertEquals(5, reminder.getTimeToLiveSeconds());
		Assertions.assertEquals(false, reminder.isPending());
		return reminder;
	}

}
