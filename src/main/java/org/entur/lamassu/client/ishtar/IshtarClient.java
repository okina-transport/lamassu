package org.entur.lamassu.client.ishtar;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import org.entur.lamassu.client.ishtar.dto.LamassuProviderDto;
import org.entur.lamassu.mapper.feedprovider.FeedProviderMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class IshtarClient {

  private static final Duration TIMEOUT = Duration.ofSeconds(10);
  private static final Logger log = LoggerFactory.getLogger(IshtarClient.class);
  private final WebClient client;
  private final TokenService tokenService;
  private final FeedProviderMapper feedProviderMapper;

  public IshtarClient(
    @Value("${ishtar.server.url}") URI ishtarUri,
    TokenService tokenService,
    FeedProviderMapper feedProviderMapper
  ) {
    this.client = WebClient.builder().baseUrl(ishtarUri.toString()).build();
    this.tokenService = tokenService;
    this.feedProviderMapper = feedProviderMapper;
  }

  public List<FeedProvider> fetchGbfsProviders() {
    return client
      .get()
      .uri("/gbfs-apis/for-lamassu")
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()))
      .retrieve()
      .bodyToMono(new ParameterizedTypeReference<List<LamassuProviderDto>>() {})
      .timeout(TIMEOUT)
      .blockOptional()
      .map(this::extractProvidersFromResponse)
      .orElse(Collections.emptyList());
  }

  private List<FeedProvider> extractProvidersFromResponse(
    List<LamassuProviderDto> response
  ) {
    try {
      log.info("Ishtar providers content: {}", response);

      return response.stream().map(feedProviderMapper::mapFromApiResponse).toList();
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse providers response", e);
    }
  }
}
