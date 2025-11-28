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

package org.entur.lamassu.model.provider;

import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleType;

@Data
public class FeedProvider {

  private String systemId;
  private String operatorId;
  private String operatorName;
  private String codespace;
  private String url;
  private String language;
  private Authentication authentication;
  private List<GBFSFeedName> excludeFeeds;
  private Boolean aggregate;
  private List<GBFSVehicleType> vehicleTypes;
  private List<GBFSPlan> pricingPlans;
  private String version;
  private Instant lastSuccessfulProducerCall;
  private Instant lastFailedProducerCall;
  private String datasetId;
  private GbfsModality gbfsModality;
}
