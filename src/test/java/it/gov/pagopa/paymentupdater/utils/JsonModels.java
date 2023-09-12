package it.gov.pagopa.paymentupdater.utils;

public class JsonModels {

  public static String getPaymentWithGivenDueDate(String dueDate) {
    return "{\n" +
      "  \"readFlag\": false,\n" +
      "  \"paidFlag\": true,\n" +
      "  \"insertionDate\": \"2023-08-04T14:24:10.437Z\",\n" +
      "  \"maxReadMessageSend\": 0,\n" +
      "  \"maxPaidMessageSend\": 0,\n" +
      "  \"paidDate\": \"2023-08-20T18:24:18.435Z\",\n" +
      "  \"rptId\": \"12345678901234567890\",\n" +
      "  \"senderServiceId\": \"ASDFGHJKL1234567890\",\n" +
      "  \"senderUserId\": \"ASDFGHJKL1234567890\",\n" +
      "  \"timeToLiveSeconds\": 3600,\n" +
      "  \"dueDate\": \"" + dueDate + "\",\n" +
      "  \"createdAt\": \"1691147078888\",\n" +
      "  \"isPending\": false,\n" +
      "  \"content_subject\": \"aSubject\",\n" +
      "  \"content_type\": \"PAYMENT\",\n" +
      "  \"content_paymentData_amount\": 2102,\n" +
      "  \"content_paymentData_noticeNumber\": \"1234567890\",\n" +
      "  \"content_paymentData_invalidAfterDueDate\": false,\n" +
      "  \"content_paymentData_payeeFiscalCode\": \"12345678901\",\n" +
      "  \"fiscalCode\": \"AAAAAA00A00A000A\",\n" +
      "  \"feature_level_type\": \"STANDARD\",\n" +
      "  \"_class\": \"it.gov.pagopa.paymentupdater.model.Payment\"\n" +
      "}";
  }
}
