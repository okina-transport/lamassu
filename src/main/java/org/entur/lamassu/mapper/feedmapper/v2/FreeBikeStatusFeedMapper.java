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

package org.entur.lamassu.mapper.feedmapper.v2;

import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v2.FreeBikeStatusFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSBike;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSData;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FreeBikeStatusFeedMapper extends AbstractFeedMapper<GBFSFreeBikeStatus> {

  private final String targetGbfsVersion;

  private final FreeBikeStatusFeedIdMapper idMapper;

  public FreeBikeStatusFeedMapper(
    @Value("${org.entur.lamassu.targetGbfsVersion:2.2}") String targetGbfsVersion,
    FreeBikeStatusFeedIdMapper idMapper
  ) {
    this.targetGbfsVersion = targetGbfsVersion;
    this.idMapper = idMapper;
  }

  @Override
  public GBFSFreeBikeStatus map(
    GBFSFreeBikeStatus source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSFreeBikeStatus();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();

    mapped.setBikes(
      data
        .getBikes()
        .stream()
        .filter(bike ->
          (bike.getLon() != null && bike.getLat() != null) || bike.getStationId() != null
        )
        .map(this::mapBike)
        .collect(Collectors.toList())
    );

    return mapped;
  }

  protected GBFSBike mapBike(GBFSBike bike) {
    var mapped = new GBFSBike();
    mapped.setBikeId(bike.getBikeId());
    mapped.setLat(bike.getLat());
    mapped.setLon(bike.getLon());
    mapped.setIsReserved(bike.getIsReserved());
    mapped.setIsDisabled(bike.getIsDisabled());
    mapped.setRentalUris(bike.getRentalUris());
    mapped.setVehicleTypeId(bike.getVehicleTypeId());
    mapped.setLastReported(bike.getLastReported());
    mapped.setCurrentRangeMeters(bike.getCurrentRangeMeters());
    mapped.setCurrentFuelPercent(bike.getCurrentFuelPercent());
    mapped.setStationId(bike.getStationId());
    mapped.setHomeStationId(bike.getHomeStationId());
    mapped.setPricingPlanId(bike.getPricingPlanId());
    mapped.setVehicleEquipment(bike.getVehicleEquipment());
    mapped.setAvailableUntil(bike.getAvailableUntil());
    return mapped;
  }
}
