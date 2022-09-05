package it.gov.pagopa.paymentupdater.util;

import dto.message;
import it.gov.pagopa.paymentupdater.model.Payment;

public class MessagePaymentMapper {

    public static Payment messageToPayment(message msg) {
        Payment payment = new Payment();
        payment.setId(msg.getId());
        payment.setDueDate(msg.getDueDate());
        payment.setFiscalCode(msg.getFiscalCode());
        payment.setFeature_level_type(msg.getFeatureLevelType());
        payment.setContent_type(msg.getContentType());
        payment.setContent_subject(msg.getContentSubject());
        payment.setCreatedAt(msg.getCreatedAt());
        payment.setContent_paymentData_amount(msg.getContentPaymentDataAmount());
        payment.setContent_paymentData_invalidAfterDueDate(msg.getContentPaymentDataInvalidAfterDueDate());
        payment.setContent_paymentData_noticeNumber(msg.getContentPaymentDataNoticeNumber());
        payment.setContent_paymentData_payeeFiscalCode(msg.getContentPaymentDataPayeeFiscalCode());
        payment.setSenderServiceId(msg.getSenderServiceId());
        payment.setSenderUserId(msg.getSenderUserId());
        payment.setTimeToLiveSeconds(msg.getTimeToLiveSeconds());
        payment.setPending(msg.getIsPending());
        return payment;
    }
}
