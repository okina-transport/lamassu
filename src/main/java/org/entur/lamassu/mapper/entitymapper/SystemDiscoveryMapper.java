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

package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.discovery.System;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.FeedUrlUtil;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemDiscoveryMapper {

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  public System mapSystemDiscovery(
    FeedProvider feedProvider,
    GBFSVersion.Version version,
    boolean enableGbfsV3ToV2Mapping
  ) {
    var mapped = new System();
    mapped.setId(feedProvider.getSystemId());
    boolean isV3Fp =
      feedProvider.getVersion() != null && feedProvider.getVersion().startsWith("3.");
    if (version == GBFSVersion.Version._3_0) {
      mapped.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeed.Name.GBFS, feedProvider));
    }
    if (version == GBFSVersion.Version._2_3) {
      if (!isV3Fp || enableGbfsV3ToV2Mapping) {
        mapped.setUrl(
          FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeedName.GBFS, feedProvider).toString()
        );
      }
    }
    return mapped;
  }
}
