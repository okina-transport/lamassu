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

import org.entur.gbfs.v2_3.system_pricing_plans.GBFSPerKmPricing;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSPerMinPricing;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PricingSegment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PricingPlanMapper {

    private final TranslationMapper translationMapper;

    @Autowired
    public PricingPlanMapper(TranslationMapper translationMapper) {
        this.translationMapper = translationMapper;
    }

    public PricingPlan mapPricingPlan(GBFSPlan plan, String language) {
        var mapped = new PricingPlan();
        mapped.setId(plan.getPlanId());
        mapped.setName(translationMapper.mapSingleTranslation(language, plan.getName()));
        mapped.setDescription(translationMapper.mapSingleTranslation(language, plan.getDescription()));
        mapped.setUrl(plan.getUrl());
        mapped.setCurrency(plan.getCurrency());
        mapped.setPrice(plan.getPrice().floatValue());
        mapped.setTaxable(plan.getIsTaxable());
        mapped.setSurgePricing(plan.getSurgePricing());
        mapped.setPerKmPricing(mapPerKmPricing(plan.getPerKmPricing()).orElse(null));
        mapped.setPerMinPricing(mapPerMinPricing(plan.getPerMinPricing()).orElse(null));
        return mapped;
    }

    private Optional<List<PricingSegment>> mapPerKmPricing(List<GBFSPerKmPricing> pricingSegments) {
        return Optional.ofNullable(pricingSegments)
                .map(p -> p.stream()
                        .map(pricingSegment -> getPricingSegment(pricingSegment.getStart(), pricingSegment.getRate(), pricingSegment.getInterval(), pricingSegment.getEnd()))
                        .collect(Collectors.toList())
                );
    }

    private Optional<List<PricingSegment>> mapPerMinPricing(List<GBFSPerMinPricing> pricingSegments) {
        return Optional.ofNullable(pricingSegments)
                .map(p -> p.stream()
                        .map(pricingSegment -> getPricingSegment(pricingSegment.getStart(), pricingSegment.getRate(), pricingSegment.getInterval(), pricingSegment.getEnd()))
                        .collect(Collectors.toList())
                );
    }

    private PricingSegment getPricingSegment(Integer start, Double rate, Integer interval, Integer end) {
        var mapped = new PricingSegment();
        mapped.setStart(start);
        mapped.setRate(rate != null ? rate.floatValue() : null);
        mapped.setInterval(interval);
        mapped.setEnd(end);
        return mapped;
    }
}
