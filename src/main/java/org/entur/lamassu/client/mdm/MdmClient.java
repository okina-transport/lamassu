package org.entur.lamassu.client.mdm;

import jakarta.ws.rs.core.UriBuilder;
import java.util.List;
import lombok.NonNull;
import org.entur.lamassu.client.mdm.dto.OkinaIdenfierDto;
import org.entur.lamassu.client.mdm.dto.ParkingIdentifierDto;
import org.entur.lamassu.service.TokenService;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Profile("mdm")
@Component
public class MdmClient {

  private final RestClient client;
  private final TokenService tokenService;

  public MdmClient(RestClient mdmRestClient, TokenService tokenService) {
    this.client = mdmRestClient;
    this.tokenService = tokenService;
  }

  public List<ParkingIdentifierDto> findStationIdsByOperator(@NonNull String operator)
    throws RestClientException {
    // GBFS station_information maps to NETEX parking
    return client
      .get()
      .uri(
        UriBuilder.fromUri("parkings/byOperator").queryParam("operator", operator).build()
      )
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()))
      .retrieve()
      .toEntity(new ParameterizedTypeReference<List<ParkingIdentifierDto>>() {})
      .getBody();
  }

  public OkinaIdenfierDto findSystemIdByOriginalId(@NonNull String originalId)
    throws RestClientException {
    // GBFS system_information maps to NETEX organisation
    return client
      .method(HttpMethod.GET)
      .uri(
        UriBuilder
          .fromUri("organisations/byOriginalId")
          .queryParam("originalId", originalId)
          .build()
      )
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()))
      .retrieve()
      .toEntity(OkinaIdenfierDto.class)
      .getBody();
  }

  public OkinaIdenfierDto findSystemIdBySuperId(@NonNull Long superId)
    throws RestClientException {
    // GBFS system_information maps to NETEX organisation
    return client
      .method(HttpMethod.GET)
      .uri(
        UriBuilder
          .fromUri("organisations/bySuperId")
          .queryParam("superId", superId)
          .build()
      )
      .body(List.of(superId))
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()))
      .retrieve()
      .toEntity(OkinaIdenfierDto.class)
      .getBody();
  }

  public OkinaIdenfierDto createOrganisation(@NonNull String originalId)
    throws RestClientException {
    OkinaIdenfierDto organisation = new OkinaIdenfierDto();
    // dataset is required by the MDM API but not used for organisations
    organisation.setDataset(originalId);
    organisation.setOriginalId(originalId);
    return client
      .post()
      .uri("organisations")
      .headers(headers -> headers.setBearerAuth(tokenService.getToken()))
      .body(organisation)
      .retrieve()
      .toEntity(OkinaIdenfierDto.class)
      .getBody();
  }
}
