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

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedidmapper.v3.V3SystemPricingPlansFeedIdMapper;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSData;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.springframework.stereotype.Component;

@Component
public class V3SystemPricingPlansFeedMapper
  extends AbstractFeedMapper<GBFSSystemPricingPlans> {

  private static final String TARGET_GBFS_VERSION = "3.0";

  private final V3SystemPricingPlansFeedIdMapper idMapper;

  public V3SystemPricingPlansFeedMapper(V3SystemPricingPlansFeedIdMapper idMapper) {
    this.idMapper = idMapper;
  }

  @Override
  public GBFSSystemPricingPlans map(
    GBFSSystemPricingPlans source,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    // TODO should we support custom pricing plans?
    //if (feedProvider.getPricingPlans() != null) {
    //  return customPricingPlans(feedProvider);
    //}

    if (
      source == null || source.getData() == null || source.getData().getPlans() == null
    ) {
      return null;
    }

    var mapped = new GBFSSystemPricingPlans();
    mapped.setVersion(TARGET_GBFS_VERSION);
    mapped.setTtl(source.getTtl());
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setData(mapData(source.getData()));

    idMapper.mapIds(mapped, feedProvider, toOriginalId);
    return mapped;
  }

  private GBFSData mapData(GBFSData data) {
    var mapped = new GBFSData();
    var plans = mapPlans(data.getPlans());
    mapped.setPlans(plans);
    return mapped;
  }

  private List<GBFSPlan> mapPlans(List<GBFSPlan> plans) {
    return plans.stream().map(this::mapPlan).collect(Collectors.toList());
  }

  private GBFSPlan mapPlan(GBFSPlan plan) {
    var mapped = new GBFSPlan();
    mapped.setPlanId(plan.getPlanId());
    mapped.setUrl(plan.getUrl());
    mapped.setName(plan.getName());
    mapped.setDescription(plan.getDescription());
    mapped.setCurrency(plan.getCurrency());
    mapped.setIsTaxable(plan.getIsTaxable());
    mapped.setPrice(plan.getPrice());
    mapped.setSurgePricing(plan.getSurgePricing());
    mapped.setPerKmPricing(plan.getPerKmPricing());
    mapped.setPerMinPricing(plan.getPerMinPricing());
    return mapped;
  }
}
