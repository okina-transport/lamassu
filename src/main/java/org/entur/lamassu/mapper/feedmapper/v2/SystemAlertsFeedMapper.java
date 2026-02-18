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
import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v2.SystemAlertsFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.system_alerts.GBFSAlert;
import org.mobilitydata.gbfs.v2_3.system_alerts.GBFSData;
import org.mobilitydata.gbfs.v2_3.system_alerts.GBFSSystemAlerts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemAlertsFeedMapper extends AbstractFeedMapper<GBFSSystemAlerts> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  private final SystemAlertsFeedIdMapper idMapper;

  public SystemAlertsFeedMapper(SystemAlertsFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  public GBFSSystemAlerts map(
    GBFSSystemAlerts systemAlerts,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (systemAlerts == null) {
      return null;
    }

    var mappedSystemAlerts = new GBFSSystemAlerts();
    mappedSystemAlerts.setVersion(targetGbfsVersion);
    mappedSystemAlerts.setLastUpdated(systemAlerts.getLastUpdated());
    mappedSystemAlerts.setTtl(systemAlerts.getTtl());
    mappedSystemAlerts.setData(mapData(systemAlerts.getData()));

    idMapper.mapIds(mappedSystemAlerts, feedProvider, toOriginalId);
    return mappedSystemAlerts;
  }

  private GBFSData mapData(GBFSData data) {
    var mappedData = new GBFSData();
    mappedData.setAlerts(mapAlerts(data.getAlerts()));
    return mappedData;
  }

  private List<GBFSAlert> mapAlerts(List<GBFSAlert> alerts) {
    return alerts.stream().map(this::mapAlert).collect(Collectors.toList());
  }

  private GBFSAlert mapAlert(GBFSAlert alert) {
    var mappedAlert = new GBFSAlert();
    mappedAlert.setAlertId(alert.getAlertId());
    mappedAlert.setLastUpdated(alert.getLastUpdated());
    mappedAlert.setUrl(alert.getUrl());
    mappedAlert.setDescription(alert.getDescription());
    mappedAlert.setRegionIds(new ArrayList<>(alert.getRegionIds()));
    mappedAlert.setDescription(alert.getDescription());
    mappedAlert.setStationIds(new ArrayList<>(alert.getStationIds()));
    mappedAlert.setSummary(alert.getSummary());
    mappedAlert.setTimes(alert.getTimes());
    mappedAlert.setType(alert.getType());
    return mappedAlert;
  }
}
