package org.entur.lamassu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;

@Configuration
public class JmsConfig {

  @Bean
  public MessageConverter messageCreator() {
    return new MappingJackson2MessageConverter();
  }
}
