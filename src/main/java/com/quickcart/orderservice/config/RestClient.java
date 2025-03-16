package com.quickcart.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClient {

    @Bean
    public RestTemplate template() {
        return new RestTemplate();
    }
}
