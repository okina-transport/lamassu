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
import org.entur.lamassu.mapper.feedidmapper.v2.SystemRegionsFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.system_regions.GBFSData;
import org.mobilitydata.gbfs.v2_3.system_regions.GBFSRegion;
import org.mobilitydata.gbfs.v2_3.system_regions.GBFSSystemRegions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemRegionsFeedMapper extends AbstractFeedMapper<GBFSSystemRegions> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  private final SystemRegionsFeedIdMapper idMapper;

  public SystemRegionsFeedMapper(SystemRegionsFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  @Override
  public GBFSSystemRegions map(
    GBFSSystemRegions source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSSystemRegions();
    mapped.setVersion(targetGbfsVersion);
    mapped.setTtl(source.getTtl());
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();
    mapped.setRegions(mapRegions(data.getRegions()));
    return mapped;
  }

  private List<GBFSRegion> mapRegions(List<GBFSRegion> regions) {
    return regions.stream().map(this::mapRegion).collect(Collectors.toList());
  }

  private GBFSRegion mapRegion(GBFSRegion region) {
    var mapped = new GBFSRegion();
    mapped.setRegionId(region.getRegionId());
    mapped.setName(region.getName());
    return mapped;
  }
}
