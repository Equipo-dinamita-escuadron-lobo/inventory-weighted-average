package com.kardex;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
@Disabled
@ImportAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration.class
})
class KardexApplicationTests {

    @Test
    void contextLoads() {
    }

}
