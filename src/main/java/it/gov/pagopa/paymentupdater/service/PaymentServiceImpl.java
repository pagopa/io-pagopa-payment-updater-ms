package it.gov.pagopa.paymentupdater.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
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
import it.gov.pagopa.paymentupdater.dto.request.ProxyPaymentResponse;
import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.producer.PaymentProducer;
import it.gov.pagopa.paymentupdater.repository.PaymentRepository;
import it.gov.pagopa.paymentupdater.restclient.proxy.ApiClient;
import it.gov.pagopa.paymentupdater.restclient.proxy.api.DefaultApi;
import it.gov.pagopa.paymentupdater.restclient.proxy.model.PaymentRequestsGetResponse;
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
	DefaultApi defaultApi;
	
	@Autowired
	@Qualifier("kafkaTemplatePayments")
	private KafkaTemplate<String, String> kafkaTemplatePayments;
	

	private static String isPaid = "isPaid";

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
	public Map<String, String> checkPayment(Payment payment) throws JsonProcessingException, InterruptedException, ExecutionException {
		Map<String, String> map = new HashMap<>();
		map.put(isPaid, "false");
		try {			
			ApiClient apiClient = new ApiClient();
			if (enableRestKey) {
				apiClient.setApiKey(proxyEndpointKey);
			}
			apiClient.setBasePath(urlProxy);
			
			defaultApi.setApiClient(apiClient);	
			PaymentRequestsGetResponse resp = defaultApi.getPaymentInfo(payment.getRptId(), Constants.X_CLIENT_ID);	
			map.put("dueDate", resp.getDueDate());

			return map;


		} catch (HttpServerErrorException errorException) {
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
								
					if(StringUtils.isNotEmpty(res.getDuedate())) {
						LocalDate localDateDueDate = LocalDate.parse(res.getDuedate());
					
						long longDueDate = payment.getDueDate() != null ? payment.getDueDate().longValue() : 0L;
						LocalDate reminderDueDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(longDueDate),
	                               TimeZone.getDefault().toZoneId()).toLocalDate();
						
						if(!localDateDueDate.equals(reminderDueDate)) {
							message.setDueDate(localDateDueDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
						}
					}
					producer.sendPaymentUpdate(mapper.writeValueAsString(message), kafkaTemplatePayments, topic);
					map.put(isPaid, "true");
					map.put("dueDate", res.getDuedate());
				
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