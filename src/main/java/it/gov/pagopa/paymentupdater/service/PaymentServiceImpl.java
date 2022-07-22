package it.gov.pagopa.paymentupdater.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.dto.PaymentMessage;
import it.gov.pagopa.paymentupdater.dto.request.ProxyPaymentResponse;
import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.producer.PaymentProducer;
import it.gov.pagopa.paymentupdater.repository.PaymentRepository;
import it.gov.pagopa.paymentupdater.util.Constants;
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
	public Map<String, Boolean> checkPayment(String rptId) throws JsonMappingException, JsonProcessingException, InterruptedException, ExecutionException {
		Map<String, Boolean> map = new HashMap<>();
		map.put("isPaid", false);
		try {
			String url = urlProxy.concat("%s");
			url = String.format(url, rptId);

			HttpHeaders headers = new HttpHeaders();
			if (enableRestKey)
				headers.set(Constants.OCP_APIM_SUB_KEY, proxyEndpointKey);
			HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
			restTemplate.exchange(url, HttpMethod.GET, requestEntity, ProxyPaymentResponse.class);
			return map;
		} catch (HttpServerErrorException errorException) {
			// the reminder is already paid
			ProxyPaymentResponse res = mapper.readValue(errorException.getResponseBodyAsString(),
					ProxyPaymentResponse.class);
			if (res.getDetail_v2().equals("PPT_RPT_DUPLICATA")
					&& errorException.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
				Payment reminder = paymentRepository.getPaymentByRptId(rptId);
				if (Objects.nonNull(reminder)) {
					reminder.setPaidFlag(true);
					reminder.setPaidDate(LocalDateTime.now());
					paymentRepository.save(reminder);
					PaymentMessage message = new PaymentMessage();
					message.setNoticeNumber(reminder.getContent_paymentData_noticeNumber());
					message.setPayeeFiscalCode(reminder.getContent_paymentData_payeeFiscalCode());
					message.setSource("payments");
					producer.sendReminder(mapper.writeValueAsString(message), kafkaTemplatePayments, topic);
					map.put("isPaid", true);
				}
				return map;
			} else {
				throw errorException;
			}
		}
	}

	@Override
	public Optional<Payment> findById(String messageId) {
		return paymentRepository.findById(messageId);
	}

}
