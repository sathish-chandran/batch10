package com;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class TestMessageController {

		@Test
		public void testMessage() {
			MessageController ms = new MessageController();
			Assertions.assertEquals(ms.sayHello(), "hello from bootcamp");
		}
		
		@Test
		public void testHello() {
			MessageController ms = new MessageController();
			Assertions.assertEquals(ms.testMessage(), "test");
		}
	}


