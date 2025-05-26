package org.entur.lamassu.config.feedprovider;

import org.apache.commons.collections4.ListUtils;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.TokenService;
import org.entur.lamassu.util.GbfsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Configuration
@Primary
public class FeedProviderApiConfig implements FeedProviderConfig {

  private static final String GBFS_APIS_URI = "/gbfs-apis/for-lamassu";
  private static final String TOKEN_REQUEST_QUEUE = "token.request.queue";

  private final String ishtarBaseUrl;
  private final WebClient webClient;

  @Autowired
  private TokenService tokenService;

  @Autowired
  public FeedProviderApiConfig(
    @Value("${ishtar.server.url}") String ishtarBaseUrl,
    WebClient webClient
  ) {
    this.ishtarBaseUrl = ishtarBaseUrl;
    this.webClient = webClient;
  }

  @Override
  public List<FeedProvider> getProviders() {
    return ListUtils.emptyIfNull(
      getWebClient(GBFS_APIS_URI)
        .retrieve()
        .bodyToMono(Map.class)
        .map(this::extractProviders).onErrorReturn(new ArrayList<>(0))
        .block()
    );
  }

  private List<FeedProvider> extractProviders(Map<String, Object> response) {
    Map<String, Object> lamassu = (Map<String, Object>) response.get("lamassu");
    List<Map<String, Object>> providersData = (List<Map<String, Object>>) lamassu.get(
      "providers"
    );

    return providersData
      .stream()
      .map(this::mapToFeedProvider)
      .collect(Collectors.toList());
  }

  private FeedProvider mapToFeedProvider(Map<String, Object> providerData) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId((String) providerData.get("systemId"));
    provider.setOperatorId((String) providerData.get("operatorId"));
    provider.setOperatorName((String) providerData.get("operatorName"));
    provider.setCodespace((String) providerData.get("codespace"));
    provider.setUrl((String) providerData.get("url"));
    provider.setLanguage((String) providerData.get("language"));
    provider.setExcludeFeeds(
      GbfsUtils.convertToGBFSFeedNameList(providerData.get("excludeFeeds"))
    );

    return provider;
  }

  private void setIfPresent(
    Map<String, Object> map,
    String key,
    Consumer<Object> setter
  ) {
    if (map.containsKey(key)) {
      setter.accept(map.get(key));
    }
  }

  private WebClient.RequestHeadersSpec<?> getWebClient(String uri) {
    return webClient
      .get()
      .uri(ishtarBaseUrl + uri)
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()));
  }
}
