package it.gov.pagopa.paymentupdater.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import it.gov.pagopa.paymentupdater.model.ApiPaymentMessage;
import it.gov.pagopa.paymentupdater.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Api(tags = "API  Payment")
@RestController
@Validated
@RequestMapping(value = "api/v1/payment", produces = APPLICATION_JSON_VALUE, consumes = {
		MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE }, method = RequestMethod.OPTIONS)
@RequiredArgsConstructor
public class PaymentController {

	@Autowired
	PaymentService paymentService;

	@GetMapping(value = "/check/messages/{messageId}")
	public ResponseEntity<ApiPaymentMessage> getMessagePayment(@PathVariable String messageId) {
		return paymentService.findById(messageId)
				.map(pay -> ApiPaymentMessage.builder().messageId(pay.getId())
						.dueDate(Optional.ofNullable(pay.getDueDate())
								.map(date -> date.toLocalDate())
								.orElseGet(() -> null))
						.paid(pay.isPaidFlag())
						.amount(pay.getContent_paymentData_amount())
						.fiscalCode(pay.getFiscalCode())
						.noticeNumber(pay.getContent_paymentData_noticeNumber())
						.build())
				.map(paymentMessage -> new ResponseEntity<>(paymentMessage, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

	}

}
