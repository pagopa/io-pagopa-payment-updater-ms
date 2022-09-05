package it.gov.pagopa.paymentupdater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.Charset;
import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.model.Payment;
import it.gov.pagopa.paymentupdater.producer.PaymentProducer;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class MockControllerTest extends AbstractMock {
        @Autowired
        private MockMvc mvc;

        @Autowired
        ObjectMapper mapper;

        @Mock
        private PaymentProducer producer;


        @Test
        public void callGetMessagePayment() throws Exception {
                Payment payment = new Payment();
                payment.setDueDate(Instant.now().toEpochMilli());
                mockFindIdWithResponse(payment);
                // when
                MockHttpServletResponse response = mvc.perform(
                                get("/api/v1/payment/check/messages/ABC")
                                                .accept(MediaType.APPLICATION_JSON))
                                .andReturn().getResponse();
                // then
                assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        }

        @Test
        public void test_checkAssistenzaIsPaidTrue() throws Exception {
                mockGetPaymentByNoticeNumber(getTestReminder());
                mockSaveWithResponse(getTestReminder());
                HttpServerErrorException errorResponse = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "", mapper.writeValueAsString(getProxyResponse()).getBytes(), Charset.defaultCharset());
                Mockito.when(restTemplate.exchange(
                                ArgumentMatchers.anyString(),
                                ArgumentMatchers.any(HttpMethod.class),
                                ArgumentMatchers.any(HttpEntity.class),
                                ArgumentMatchers.<Class<String>>any())).thenThrow(errorResponse);

        }

}