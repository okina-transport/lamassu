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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.entur.lamassu.mapper.feedidmapper.v2.StationStatusFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.station_status.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StationStatusFeedMapper extends AbstractFeedMapper<GBFSStationStatus> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  private final StationStatusFeedIdMapper idMapper;

  public StationStatusFeedMapper(StationStatusFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  @Override
  public GBFSStationStatus map(
    GBFSStationStatus source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSStationStatus();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();
    mapped.setStations(
      data.getStations().stream().map(this::mapStation).collect(Collectors.toList())
    );
    return mapped;
  }

  private GBFSStation mapStation(GBFSStation station) {
    var mapped = new GBFSStation();
    mapped.setStationId(station.getStationId());
    mapped.setNumBikesAvailable(station.getNumBikesAvailable());
    mapped.setVehicleTypesAvailable(
      mapVehicleTypesAvailable(station.getVehicleTypesAvailable()).orElse(null)
    );
    mapped.setNumBikesDisabled(station.getNumBikesDisabled());
    if (station.getNumDocksAvailable() == null || station.getNumDocksAvailable() < 1) {
      mapped.setNumDocksAvailable(0);
    } else {
      mapped.setNumDocksAvailable(station.getNumDocksAvailable());
    }
    if (CollectionUtils.isEmpty(station.getVehicleDocksAvailable())) {
      mapped.setVehicleDocksAvailable(null);
    } else {
      mapped.setVehicleDocksAvailable(
        mapVehicleDocksAvailable(station.getVehicleDocksAvailable()).orElse(null)
      );
    }
    mapped.setNumDocksDisabled(station.getNumDocksDisabled());
    mapped.setIsInstalled(station.getIsInstalled());
    mapped.setIsRenting(station.getIsRenting());
    mapped.setIsReturning(station.getIsReturning());
    mapped.setLastReported(station.getLastReported());
    return mapped;
  }

  private Optional<List<GBFSVehicleTypesAvailable>> mapVehicleTypesAvailable(
    List<GBFSVehicleTypesAvailable> vehicleTypesAvailable
  ) {
    return Optional
      .ofNullable(vehicleTypesAvailable)
      .map(vtsa ->
        vtsa
          .stream()
          .map(vta -> {
            var mapped = new GBFSVehicleTypesAvailable();
            mapped.setVehicleTypeId(vta.getVehicleTypeId());
            mapped.setCount(vta.getCount());
            return mapped;
          })
          .collect(Collectors.toList())
      );
  }

  private Optional<List<GBFSVehicleDocksAvailable>> mapVehicleDocksAvailable(
    List<GBFSVehicleDocksAvailable> vehicleDocksAvailable
  ) {
    return Optional
      .ofNullable(vehicleDocksAvailable)
      .map(vdsa ->
        vdsa
          .stream()
          .map(vda -> {
            var mapped = new GBFSVehicleDocksAvailable();
            mapped.setVehicleTypeIds(new ArrayList<>(vda.getVehicleTypeIds()));
            if (vda.getCount() == null || vda.getCount() < 1) {
              mapped.setCount(0);
            } else {
              mapped.setCount(vda.getCount());
            }
            return mapped;
          })
          .collect(Collectors.toList())
      );
  }
}
