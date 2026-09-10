package org.entur.lamassu.service.idmapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.entur.lamassu.client.mdm.MdmClient;
import org.entur.lamassu.client.mdm.dto.OkinaIdenfierDto;
import org.entur.lamassu.client.mdm.dto.ParkingIdentifierDto;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
@Profile("mdm")
@Slf4j
public class MdmIdMappingService extends BaseIdMappingService {

  private final MdmClient mdmClient;

  private final String mdmIdPrefix;

  public MdmIdMappingService(
    MdmClient mdmClient,
    @Value("${mdm.id.prefix:MOBIITI}") String mdmIdPrefix
  ) {
    this.mdmClient = mdmClient;
    this.mdmIdPrefix = mdmIdPrefix;
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
      List<ParkingIdentifierDto> pids = mdmClient.findStationIdsByOperator(
        feedProvider.getSystemId()
      );
      for (var pid : pids) {
        stationIdsMap.put(pid.getOriginalId(), pid.getSuperId());
      }
    } catch (RestClientException e) {
      log.error("Error retrieving station IDs from MDM", e);
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
      List<ParkingIdentifierDto> parkingIds = mdmClient.findStationIdsByOperator(
        feedProvider.getSystemId()
      );
      for (var parkingId : parkingIds) {
        stationIdsMap.put(parkingId.getSuperId(), parkingId.getOriginalId());
      }
    } catch (RestClientException e) {
      log.error("Error retrieving station IDs from MDM", e);
    }
    return stationIdsMap;
  }

  @Override
  public String getSystemIdOriginalToSuper(String id, FeedProvider feedProvider) {
    try {
      OkinaIdenfierDto organisationId = findSystemIdByOriginalIdOrCreate(id);
      return String.format(
        "%s:Organisation:%d",
        mdmIdPrefix,
        organisationId.getSuperId()
      );
    } catch (RestClientException e) {
      log.error("Error retrieving or creating system super ID from MDM", e);
    }
    return super.getSystemIdOriginalToSuper(id, feedProvider);
  }

  private OkinaIdenfierDto findSystemIdByOriginalIdOrCreate(String id) {
    try {
      return mdmClient.findSystemIdByOriginalId(id);
    } catch (HttpClientErrorException.NotFound e) {
      log.warn("Organisation with originalId {} not found in MDM, creating it", id);
      return createOrganisation(id);
    }
  }

  private OkinaIdenfierDto createOrganisation(String id) {
    return mdmClient.createOrganisation(id);
  }

  @Override
  public String getSystemIdSuperToOriginal(String id, FeedProvider feedProvider) {
    String prefix = String.format("%s:Organisation:", mdmIdPrefix);
    if (id.startsWith(prefix)) {
      try {
        Long mdmSuperId = Long.parseLong(id.substring(prefix.length()));
        OkinaIdenfierDto organisationId = mdmClient.findSystemIdBySuperId(mdmSuperId);
        return organisationId.getOriginalId();
      } catch (NumberFormatException e) {
        // do nothing
      } catch (RestClientException e) {
        log.error("Error retrieving system original ID from MDM", e);
      }
    }
    return super.getSystemIdSuperToOriginal(id, feedProvider);
  }
}
