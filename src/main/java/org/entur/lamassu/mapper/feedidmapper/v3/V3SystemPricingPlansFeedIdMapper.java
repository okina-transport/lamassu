package org.entur.lamassu.mapper.feedidmapper.v3;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.feedidmapper.FeedIdMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.idmapping.IdMappingService;
import org.entur.lamassu.util.IdMappersUtil;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.springframework.stereotype.Component;

@Component
public class V3SystemPricingPlansFeedIdMapper
  implements FeedIdMapper<GBFSSystemPricingPlans> {

  private final IdMappingService idMappingService;

  public V3SystemPricingPlansFeedIdMapper(IdMappingService idMappingService) {
    this.idMappingService = idMappingService;
  }

  @Override
  public void mapIds(
    GBFSSystemPricingPlans mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    if (
      mapped == null ||
      mapped.getData() == null ||
      CollectionUtils.isEmpty(mapped.getData().getPlans())
    ) {
      return;
    }

    Map<String, String> planIdsMap = buildPlanIdsMap(mapped, feedProvider, toOriginalId);

    for (var plan : mapped.getData().getPlans()) {
      plan.setPlanId(IdMappersUtil.mapId(plan.getPlanId(), planIdsMap));
    }
  }

  private Map<String, String> buildPlanIdsMap(
    GBFSSystemPricingPlans mapped,
    FeedProvider feedProvider,
    boolean toOriginalId
  ) {
    Set<String> planIds = mapped
      .getData()
      .getPlans()
      .stream()
      .map(GBFSPlan::getPlanId)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toSet());

    return toOriginalId
      ? idMappingService.getPricingPlanIdsSuperToOriginalMap(planIds, feedProvider)
      : idMappingService.getPricingPlanIdsOriginalToSuperMap(planIds, feedProvider);
  }
}
