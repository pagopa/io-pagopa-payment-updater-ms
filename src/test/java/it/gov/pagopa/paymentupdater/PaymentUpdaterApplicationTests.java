package it.gov.pagopa.paymentupdater;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class PaymentUpdaterApplicationTests {
	@Test
	void main() {
		Application.main(new String[] {});
		Assertions.assertTrue(true);
	}
}
