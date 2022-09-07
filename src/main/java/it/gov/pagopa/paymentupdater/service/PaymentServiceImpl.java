package it.gov.pagopa.paymentupdater.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.dto.PaymentMessage;
import it.gov.pagopa.paymentupdater.dto.ProxyResponse;
import it.gov.pagopa.paymentupdater.dto.request.ProxyPaymentResponse;
import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.producer.PaymentProducer;
import it.gov.pagopa.paymentupdater.repository.PaymentRepository;
import it.gov.pagopa.paymentupdater.restclient.proxy.ApiClient;
import it.gov.pagopa.paymentupdater.restclient.proxy.api.DefaultApi;
import it.gov.pagopa.paymentupdater.restclient.proxy.model.PaymentRequestsGetResponse;
import it.gov.pagopa.paymentupdater.util.Constants;
import it.gov.pagopa.paymentupdater.util.PaymentUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class PaymentServiceImpl implements PaymentService {

	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	ObjectMapper mapper;
	@Autowired
	RestTemplate restTemplate;
	@Value("${payment.request}")
	private String urlProxy;
	@Value("${kafka.paymentupdates}")
	private String topic;
	@Value("${enable_rest_key}")
	private boolean enableRestKey;
	@Value("${proxy_endpoint_subscription_key}")
	private String proxyEndpointKey;

	@Autowired
	PaymentProducer producer;	
	@Autowired
	DefaultApi defaultApi;
	
	@Autowired
	@Qualifier("kafkaTemplatePayments")
	private KafkaTemplate<String, String> kafkaTemplatePayments;

	@Override
	public Optional<Payment> getPaymentByNoticeNumberAndFiscalCode(String noticeNumber, String fiscalCode) {

		return paymentRepository.getPaymentByNoticeNumberAndFiscalCode(noticeNumber, fiscalCode);
	}

	@Override
	public void save(Payment reminder) {
		paymentRepository.save(reminder);
		log.info("Saved payment id: {}", reminder.getId());
	} 

	@Override
	public ProxyResponse checkPayment(Payment payment) throws JsonProcessingException, InterruptedException, ExecutionException {
		ProxyResponse proxyResp = new ProxyResponse();
		try {
			ApiClient apiClient = new ApiClient();
			if (enableRestKey) {
				apiClient.setApiKey(proxyEndpointKey);
			}
			apiClient.setBasePath(urlProxy);

			defaultApi.setApiClient(apiClient);		
			PaymentRequestsGetResponse resp = defaultApi.getPaymentInfo(payment.getRptId(), Constants.X_CLIENT_ID);

			LocalDate dueDate = PaymentUtil.getLocalDateFromString(resp.getDueDate());	
			proxyResp.setDueDate(dueDate);

			return proxyResp;

		}
		
		catch (HttpServerErrorException errorException) {
			// the reminder is already paid
			ProxyPaymentResponse res = mapper.readValue(errorException.getResponseBodyAsString(), ProxyPaymentResponse.class);
			if (res.getDetail_v2().equals("PPT_RPT_DUPLICATA")
					&& errorException.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
					payment.setPaidFlag(true);
					payment.setPaidDate(LocalDateTime.now());
					PaymentMessage message = new PaymentMessage();
					message.setNoticeNumber(payment.getContent_paymentData_noticeNumber());
					message.setPayeeFiscalCode(payment.getContent_paymentData_payeeFiscalCode());
					message.setSource("payments");			
					PaymentUtil.checkDueDateForPaymentMessage(res.getDuedate(), message);							
					producer.sendPaymentUpdate(mapper.writeValueAsString(message), kafkaTemplatePayments, topic);
					proxyResp.setPaid(true);
					proxyResp.setDueDate(PaymentUtil.getLocalDateFromString(res.getDuedate()));
				return proxyResp;
			} else {
				throw errorException;
			}
		}
	}

	@Override
	public Optional<Payment> findById(String messageId) {
		return paymentRepository.findById(messageId);
	}

	@Override
	public List<Payment> getPaymentsByRptid(String rptid) {
		List<Payment> payments = paymentRepository.getPaymentByRptId(rptid);
		return payments == null ? new ArrayList<>() : payments;
	}

}