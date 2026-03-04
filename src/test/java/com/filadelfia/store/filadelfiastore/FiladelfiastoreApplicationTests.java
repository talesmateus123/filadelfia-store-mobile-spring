package com.filadelfia.store.filadelfiastore;

import com.filadelfia.store.filadelfiastore.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class FiladelfiastoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
