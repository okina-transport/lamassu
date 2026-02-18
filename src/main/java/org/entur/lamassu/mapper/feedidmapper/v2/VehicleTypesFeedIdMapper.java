package org.entur.lamassu.mapper.feedidmapper.v2;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypesFeedIdMapper implements FeedIdMapper<GBFSVehicleTypes> {

  private final IdMappingService idMappingService;

  public VehicleTypesFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSVehicleTypes mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped == null ||
      mapped.getData() == null ||
      CollectionUtils.isEmpty(mapped.getData().getVehicleTypes())
    ) {
      return;
    }

    Map<String, String> vehicleTypeIds = buildVehicleTypeIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    Map<String, String> pricingPlanIdsMap = buildPricingPlanIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    for (var vehicleType : mapped.getData().getVehicleTypes()) {
      vehicleType.setVehicleTypeId(
        IdMappersUtil.mapId(vehicleType.getVehicleTypeId(), vehicleTypeIds)
      );
      vehicleType.setPricingPlanIds(
        IdMappersUtil.mapIds(vehicleType.getPricingPlanIds(), pricingPlanIdsMap)
      );
      vehicleType.setDefaultPricingPlanId(
        IdMappersUtil.mapId(vehicleType.getDefaultPricingPlanId(), pricingPlanIdsMap)
      );
    }
  }

  private Map<String, String> buildVehicleTypeIdsMap(
    GBFSVehicleTypes mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> vehicleTypeIds = mapped
      .getData()
      .getVehicleTypes()
      .stream()
      .map(GBFSVehicleType::getVehicleTypeId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getVehicleTypeIdsSuperToOriginalMap(vehicleTypeIds, feedProvider)
      : idMappingService.getVehicleTypeIdsOriginalToSuperMap(
        vehicleTypeIds,
        feedProvider
      );
  }

  private Map<String, String> buildPricingPlanIdsMap(
    GBFSVehicleTypes mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> pricingPlanIds = mapped
      .getData()
      .getVehicleTypes()
      .stream()
      .map(GBFSVehicleType::getPricingPlanIds)
      .filter(CollectionUtils::isNotEmpty)
      .flatMap(List::stream)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    pricingPlanIds.addAll(
      mapped
        .getData()
        .getVehicleTypes()
        .stream()
        .map(GBFSVehicleType::getDefaultPricingPlanId)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet())
    );

    return toOriginalId
      ? idMappingService.getPricingPlanIdsSuperToOriginalMap(pricingPlanIds, feedProvider)
      : idMappingService.getPricingPlanIdsOriginalToSuperMap(
        pricingPlanIds,
        feedProvider
      );
  }
}
