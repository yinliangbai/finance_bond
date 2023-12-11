package com.baiyinliang.finance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class FinanceApplicationTests {

    @Test
    void contextLoads() {
        ConfigurableApplicationContext context = new AnnotationConfigReactiveWebApplicationContext();
    }

}
