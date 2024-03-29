package it.gov.pagopa.paymentupdater.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
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
	public void save(Payment reminder) {
		paymentRepository.save(reminder);
		log.info("Saved payment id: {}", reminder.getId());
	}

	@Override

	public ProxyResponse checkPayment(Payment payment)
			throws JsonProcessingException, InterruptedException, ExecutionException {
		ProxyResponse proxyResp = new ProxyResponse();
		try {
			ApiClient apiClient = new ApiClient();
			if (enableRestKey) {
				apiClient.addDefaultHeader("Ocp-Apim-Subscription-Key", proxyEndpointKey);
			}
			apiClient.setBasePath(urlProxy);

			defaultApi.setApiClient(apiClient);
			PaymentRequestsGetResponse resp = defaultApi.getPaymentInfo(payment.getRptId());

			LocalDate dueDate = PaymentUtil.getLocalDateFromString(resp.getDueDate());
			proxyResp.setDueDate(dueDate);

			return proxyResp;

		}

		catch (HttpServerErrorException errorException) {
			// the reminder is already paid
			ProxyPaymentResponse res = mapper.readValue(errorException.getResponseBodyAsString(),
					ProxyPaymentResponse.class);
			if (res.getDetail_v2() != null) {
				if (Arrays.asList("PAA_PAGAMENTO_DUPLICATO", "PPT_RPT_DUPLICATA").contains(res.getDetail_v2())
						&& errorException.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {

					List<Payment> payments = paymentRepository.getPaymentByRptId(payment.getRptId());
					payments.add(payment);
					for (Payment pay : payments) {
						pay.setPaidFlag(true);
						LocalDate proxyDate = PaymentUtil.getLocalDateFromString(res.getDuedate());
						PaymentUtil.checkDueDateForPayment(proxyDate, pay);
						paymentRepository.save(pay);

						PaymentMessage message = new PaymentMessage();
						message.setMessageId(pay.getId());
						message.setFiscalCode(pay.getFiscalCode());
						message.setNoticeNumber(payment.getContent_paymentData_noticeNumber());
						message.setPayeeFiscalCode(payment.getContent_paymentData_payeeFiscalCode());
						message.setSource("payments");
						PaymentUtil.checkDueDateForPaymentMessage(res.getDuedate(), message);
						producer.sendPaymentUpdate(mapper.writeValueAsString(message), kafkaTemplatePayments, topic);
					}

					proxyResp.setPaid(true);
					proxyResp.setDueDate(PaymentUtil.getLocalDateFromString(res.getDuedate()));
					return proxyResp;
				}
				proxyResp.setPaid(false);
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

	@Override
	public int countById(String id) {
		return paymentRepository.countById(id);
	}

}
