package org.entur.lamassu.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("mdm")
public class MdmConfig {

  @Bean("mdmRestClient")
  public RestClient mdmRestClient(@Value("${mdm.api.url}") String mdmApiUrl) {
    ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings
      .defaults()
      .withConnectTimeout(Duration.ofSeconds(1))
      .withReadTimeout(Duration.ofSeconds(10));
    var requestFactory = ClientHttpRequestFactoryBuilder.jdk().build(settings);
    return RestClient.builder().baseUrl(mdmApiUrl).requestFactory(requestFactory).build();
  }
}
