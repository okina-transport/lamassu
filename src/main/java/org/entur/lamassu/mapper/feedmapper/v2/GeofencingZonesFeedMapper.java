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

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v2.GeofencingZonesFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.geofencing_zones.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeofencingZonesFeedMapper extends AbstractFeedMapper<GBFSGeofencingZones> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  private final GeofencingZonesFeedIdMapper idMapper;

  public GeofencingZonesFeedMapper(GeofencingZonesFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  @Override
  public GBFSGeofencingZones map(
    GBFSGeofencingZones source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSGeofencingZones();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();
    mapped.setGeofencingZones(mapGeofencingZones(data.getGeofencingZones()));
    return mapped;
  }

  private GBFSGeofencingZones__1 mapGeofencingZones(
    GBFSGeofencingZones__1 geofencingZones
  ) {
    var mapped = new GBFSGeofencingZones__1();
    mapped.setType(geofencingZones.getType());
    mapped.setFeatures(mapFeatures(geofencingZones.getFeatures()));
    return mapped;
  }

  private List<GBFSFeature> mapFeatures(List<GBFSFeature> features) {
    return features.stream().map(this::mapFeature).collect(Collectors.toList());
  }

  private GBFSFeature mapFeature(GBFSFeature feature) {
    var mapped = new GBFSFeature();
    mapped.setType(feature.getType());
    mapped.setGeometry(feature.getGeometry());
    mapped.setProperties(mapProperties(feature.getProperties()));
    return mapped;
  }

  private GBFSProperties mapProperties(GBFSProperties properties) {
    var mapped = new GBFSProperties();
    mapped.setName(properties.getName());
    mapped.setStart(properties.getStart());
    mapped.setEnd(properties.getEnd());
    mapped.setRules(
      properties.getRules().stream().map(this::mapRule).collect(Collectors.toList())
    );
    return mapped;
  }

  private GBFSRule mapRule(GBFSRule rule) {
    var mapped = new GBFSRule();
    mapped.setVehicleTypeId(rule.getVehicleTypeId());
    mapped.setRideAllowed(rule.getRideAllowed());
    mapped.setMaximumSpeedKph(rule.getMaximumSpeedKph());
    mapped.setRideThroughAllowed(rule.getRideThroughAllowed());
    mapped.setStationParking(rule.getStationParking());
    return mapped;
  }
}
