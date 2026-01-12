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

package org.entur.lamassu.mapper.feedmapper.v3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v3.V3StationStatusFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.station_status.*;
import org.springframework.stereotype.Component;

@Component
public class V3StationStatusFeedMapper extends AbstractFeedMapper<GBFSStationStatus> {

  private static final String TARGET_GBFS_VERSION = "3.0";

  private final V3StationStatusFeedIdMapper idMapper;

  public V3StationStatusFeedMapper(V3StationStatusFeedIdMapper idMapper) {
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
    mapped.setVersion(TARGET_GBFS_VERSION);
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
    mapped.setNumVehiclesAvailable(station.getNumVehiclesAvailable());
    mapped.setVehicleTypesAvailable(
      mapVehicleTypesAvailable(station.getVehicleTypesAvailable()).orElse(null)
    );
    mapped.setNumVehiclesDisabled(station.getNumVehiclesDisabled());
    mapped.setNumDocksAvailable(station.getNumDocksAvailable());
    mapped.setVehicleDocksAvailable(
      mapVehicleDocksAvailable(station.getVehicleDocksAvailable()).orElse(null)
    );
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
            mapped.setCount(vda.getCount());
            return mapped;
          })
          .collect(Collectors.toList())
      );
  }
}
