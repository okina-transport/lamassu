package org.entur.lamassu.ishtar;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import org.entur.lamassu.config.feedprovider.FeedProviderMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
      .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
      .timeout(TIMEOUT)
      .onErrorResume(e -> {
        // Log l'erreur et retourne une map vide
        return Mono.just(Collections.emptyMap());
      })
      .blockOptional()
      .map(this::extractProvidersFromResponse)
      .orElse(Collections.emptyList());
  }

  private List<FeedProvider> extractProvidersFromResponse(Map<String, Object> response) {
    try {
      log.info("Ishtar providers content: {}", response);

      if (!(response.get("lamassu") instanceof Map<?, ?> lamassu)) {
        throw new RuntimeException("lamassu is not a map");
      }

      if (!(lamassu.get("providers") instanceof List<?> providersList)) {
        throw new RuntimeException("providers is not a list");
      }

      return providersList.stream()
              .filter(Map.class::isInstance)
              .map(Map.class::cast)
              .map(feedProviderMapper::mapFromApiResponse)
              .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse providers response", e);
    }
  }
}
