package it.gov.pagopa.paymentupdater;

import java.time.LocalDate;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.gov.pagopa.paymentupdater.consumer.MessageKafkaConsumer;
import it.gov.pagopa.paymentupdater.consumer.PaymentKafkaConsumer;
import it.gov.pagopa.paymentupdater.producer.PaymentProducer;
import it.gov.pagopa.paymentupdater.util.ApplicationContextProvider;

@SpringBootTest(classes = Application.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Import(it.gov.pagopa.paymentupdater.KafkaTestContainersConfiguration.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9093", "port=9093" })
public class MessageKafkaConsumerTest extends AbstractMock{
	
    @Autowired
    private PaymentProducer producer;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @InjectMocks
    MessageKafkaConsumer messageKafkaConsumer;
    
    @InjectMocks
    PaymentKafkaConsumer paymentEventKafkaConsumer;
    
	@Autowired
	ObjectMapper mapper;
	
	@Value("${kafka.paymentupdates}")
	private String producerTopic;

	
    @SuppressWarnings("unchecked")
	@Test
    public void test_producerKafka_Ok() throws JsonProcessingException, InterruptedException, ExecutionException {
    	kafkaTemplate = new KafkaTemplate<>((ProducerFactory<String, String>) ApplicationContextProvider.getBean("producerFactory"));
    	producer.sendReminder(selectPaymentMessageObject("1231", "", "2121", "AAABBB77Y66A444A", false, LocalDate.now(), 0.0, "test", "BBBPPP77J99A888A"), kafkaTemplate, "payment-updates");
    	Assertions.assertTrue(true);
    }
    
	@Test
	public void test_messageEventKafkaConsumer_GENERIC_OK() throws Throwable {
		messageKafkaConsumer = (MessageKafkaConsumer) ApplicationContextProvider.getBean("messageEventKafkaConsumer");
		mockSaveWithResponse(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A",3));
		messageKafkaConsumer.messageKafkaListener(selectReminderMockObject("", "1","PAYMENT","AAABBB77Y66A444A",3));
		Assertions.assertTrue(messageKafkaConsumer.getPayload().contains("paidFlag=false"));
		Assertions.assertEquals(0L, messageKafkaConsumer.getLatch().getCount());
	}
}

