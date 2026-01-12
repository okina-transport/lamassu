/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.mapper.feedidmapper.v2;

import io.micrometer.common.util.StringUtils;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSBike;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.springframework.stereotype.Component;

@Component
public class FreeBikeStatusFeedIdMapper implements FeedIdMapper<GBFSFreeBikeStatus> {

  private final IdMappingService idMappingService;

  public FreeBikeStatusFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSFreeBikeStatus mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped.getData() == null || CollectionUtils.isEmpty(mapped.getData().getBikes())
    ) {
      return;
    }

    Map<String, String> bikeIdsMap = buildBikeIdsMap(mapped, feedProvider, toOriginalId);
    Map<String, String> vehicleTypeIdsMap = buildVehicleTypeIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );
    Map<String, String> stationIdsMap = buildStationIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );
    Map<String, String> pricingPlanIdsMap = buildPricingPlanIdsMap(
      mapped,
      feedProvider,
      toOriginalId
    );

    for (var bike : mapped.getData().getBikes()) {
      bike.setBikeId(IdMappersUtil.mapId(bike.getBikeId(), bikeIdsMap));
      bike.setVehicleTypeId(
        IdMappersUtil.mapId(bike.getVehicleTypeId(), vehicleTypeIdsMap)
      );
      bike.setStationId(IdMappersUtil.mapId(bike.getStationId(), stationIdsMap));
      bike.setHomeStationId(IdMappersUtil.mapId(bike.getHomeStationId(), stationIdsMap));
      bike.setPricingPlanId(
        IdMappersUtil.mapId(bike.getPricingPlanId(), pricingPlanIdsMap)
      );
    }
  }

  private Map<String, String> buildPricingPlanIdsMap(
    GBFSFreeBikeStatus mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> pricingPlanIds = mapped
      .getData()
      .getBikes()
      .stream()
      .map(GBFSBike::getPricingPlanId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getPricingPlanIdsSuperToOriginalMap(pricingPlanIds, feedProvider)
      : idMappingService.getPricingPlanIdsOriginalToSuperMap(
        pricingPlanIds,
        feedProvider
      );
  }

  private Map<String, String> buildStationIdsMap(
    GBFSFreeBikeStatus mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> stationIds = mapped
      .getData()
      .getBikes()
      .stream()
      .map(GBFSBike::getStationId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    stationIds.addAll(
      mapped
        .getData()
        .getBikes()
        .stream()
        .map(GBFSBike::getHomeStationId)
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet())
    );

    return toOriginalId
      ? idMappingService.getStationIdsSuperToOriginalMap(stationIds, feedProvider)
      : idMappingService.getStationIdsOriginalToSuperMap(stationIds, feedProvider);
  }

  private Map<String, String> buildVehicleTypeIdsMap(
    GBFSFreeBikeStatus mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> vehicleTypeIds = mapped
      .getData()
      .getBikes()
      .stream()
      .map(GBFSBike::getVehicleTypeId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getVehicleTypeIdsSuperToOriginalMap(vehicleTypeIds, feedProvider)
      : idMappingService.getVehicleTypeIdsOriginalToSuperMap(
        vehicleTypeIds,
        feedProvider
      );
  }

  private Map<String, String> buildBikeIdsMap(
    GBFSFreeBikeStatus mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> bikeIds = mapped
      .getData()
      .getBikes()
      .stream()
      .map(GBFSBike::getBikeId)
      .filter(StringUtils::isNotBlank)
      .map(bikeId ->
        bikeId
          .replace(" ", "_")
          .replace("(", "_")
          .replace(")", "_")
          .replace("é", "_")
          .replace("à", "_")
      )
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getBikeIdsSuperToOriginalMap(bikeIds, feedProvider)
      : idMappingService.getBikeIdsOriginalToSuperMap(bikeIds, feedProvider);
  }
}
