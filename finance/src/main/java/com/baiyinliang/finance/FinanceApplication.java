package com.baiyinliang.finance;

import com.baiyinliang.finance.tools.WxConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableConfigurationProperties(value = WxConfig.class)
public class FinanceApplication {

    @Bean
    RestTemplate restTemplate(){
        return new RestTemplate();
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        // context.getBeanFactory();
        AbstractApplicationContext context1 = new ClassPathXmlApplicationContext();
        SpringApplication.run(FinanceApplication.class, args);
//        context.refresh();
    }

}
