package org.entur.lamassu.service.idmapping;

import jakarta.ws.rs.core.UriBuilder;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.entur.lamassu.client.mdm.dto.ParkingIdentifierDto;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Profile("tiamat")
@Slf4j
public class TiamatIdMappingService extends BaseIdMappingService {

  private final RestClient client;
  private final TokenService tokenService;

  public TiamatIdMappingService(
    @Value("${tiamat.api.url}") String tiamatApiUrl,
    TokenService tokenService
  ) {
    ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings
      .defaults()
      .withConnectTimeout(Duration.ofSeconds(1))
      .withReadTimeout(Duration.ofSeconds(10));
    var requestFactory = ClientHttpRequestFactoryBuilder.jdk().build(settings);
    this.client =
      RestClient.builder().baseUrl(tiamatApiUrl).requestFactory(requestFactory).build();
    this.tokenService = tokenService;
  }

  @Override
  public Map<String, String> getStationIdsOriginalToSuperMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    Map<String, String> stationIdsMap = super.getStationIdsOriginalToSuperMap(
      ids,
      feedProvider
    );
    try {
      List<ParkingIdentifierDto> pids = findStationIdsByOperator(
        feedProvider.getSystemId()
      );
      for (var pid : pids) {
        stationIdsMap.put(pid.getOriginalId(), pid.getSuperId());
      }
    } catch (RestClientException e) {
      log.error("Error retrieving station IDs from Tiamat", e);
    }
    return stationIdsMap;
  }

  @Override
  public Map<String, String> getStationIdsSuperToOriginalMap(
    Set<String> ids,
    FeedProvider feedProvider
  ) {
    Map<String, String> stationIdsMap = super.getStationIdsSuperToOriginalMap(
      ids,
      feedProvider
    );
    try {
      List<ParkingIdentifierDto> parkingIds = findStationIdsByOperator(
        feedProvider.getSystemId()
      );
      for (var parkingId : parkingIds) {
        stationIdsMap.put(parkingId.getSuperId(), parkingId.getOriginalId());
      }
    } catch (RestClientException e) {
      log.error("Error retrieving station IDs from Tiamat", e);
    }
    return stationIdsMap;
  }

  private List<ParkingIdentifierDto> findStationIdsByOperator(@NonNull String operator)
    throws RestClientException {
    // GBFS station_information maps to NETEX parking
    return client
      .get()
      .uri(
        UriBuilder.fromUri("parking/byOperator").queryParam("operator", operator).build()
      )
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()))
      .retrieve()
      .toEntity(new ParameterizedTypeReference<List<ParkingIdentifierDto>>() {})
      .getBody();
  }
}
