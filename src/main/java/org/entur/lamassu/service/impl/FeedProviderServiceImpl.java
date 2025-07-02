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

package org.entur.lamassu.service.impl;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.ishtar.IshtarClient;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.mapper.entitymapper.TranslationMapper;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeedProviderServiceImpl implements FeedProviderService {

  private final TranslationMapper translationMapper;
  private final FeedProviderConfig feedProviderConfig;
  private final FeedUpdater feedUpdater;
  private final IshtarClient ishtarClient;
  private final GBFSV2FeedCache gbfsV2FeedCache;
  private final GBFSV3FeedCache gbfsV3FeedCache;

  @Autowired
  public FeedProviderServiceImpl(
    FeedProviderConfig feedProviderConfig,
    TranslationMapper translationMapper,
    FeedUpdater feedUpdater,
    IshtarClient ishtarClient,
    GBFSV2FeedCache gbfsV2FeedCache,
    GBFSV3FeedCache gbfsV3FeedCache
  ) {
    this.feedProviderConfig = feedProviderConfig;
    this.translationMapper = translationMapper;
    this.feedUpdater = feedUpdater;
    this.ishtarClient = ishtarClient;
    this.gbfsV2FeedCache = gbfsV2FeedCache;
    this.gbfsV3FeedCache = gbfsV3FeedCache;
  }

  @Override
  public List<FeedProvider> getFeedProviders() {
    return feedProviderConfig.getProviders();
  }

  @Override
  public List<Operator> getOperators() {
    return getFeedProviders().stream().map(this::mapOperator).distinct().toList();
  }

  private Operator mapOperator(FeedProvider feedProvider) {
    var operator = new Operator();
    operator.setId(feedProvider.getOperatorId());
    operator.setName(
      translationMapper.mapSingleTranslation(
        feedProvider.getLanguage(),
        feedProvider.getOperatorName()
      )
    );
    return operator;
  }

  @Override
  public FeedProvider getFeedProviderBySystemId(String systemId) {
    return feedProviderConfig
      .getProviders()
      .stream()
      .filter(fp -> systemId.equals(fp.getSystemId()))
      .findFirst()
      .orElse(null);
  }

  @Override
  public List<String> getCodespaces() {
    return getFeedProviders()
      .stream()
      .map(FeedProvider::getCodespace)
      .distinct()
      .toList();
  }

  @Override
  public List<String> getSystems() {
    return getFeedProviders().stream().map(FeedProvider::getSystemId).toList();
  }

  @Override
  public void deleteFeedProviderBySystemId(String systemId) {
    FeedProvider fp = getFeedProviderBySystemId(systemId);
    if (fp == null) {
      log.info("No feed provider with systemId {}", systemId);
      return;
    }
    log.info("Delete feed provider with systemId {}", systemId);
    feedProviderConfig.getProviders().remove(fp);
    log.info("Clear cache from provider with systemId {}", systemId);
    gbfsV2FeedCache.clear(fp);
    gbfsV3FeedCache.clear(fp);
    // stop then recreate subscriptions
    restartFeedUpdaters();
  }

  @Scheduled(fixedRate = 5 * 60 * 1000)
  @Override
  public void refreshFeedProviders() {
    List<FeedProvider> ishtarProviders = this.ishtarClient.fetchGbfsProviders();
    if (CollectionUtils.isEmpty(ishtarProviders)) {
      log.info("No providers from ISHTAR, abort refresh");
      return;
    }
    boolean update = false;
    List<FeedProvider> providers = getFeedProviders();
    for (FeedProvider ishtarProvider : ishtarProviders) {
      FeedProvider existing = getFeedProviderBySystemId(ishtarProvider.getSystemId());
      if (existing != null) {
        if (!existing.equals(ishtarProvider)) {
          // provider is updated
          log.info(
            "Update provider with systemId {} / url {}",
            ishtarProvider.getSystemId(),
            ishtarProvider.getUrl()
          );
          updateFeedProvider(ishtarProvider, existing);
          update = true;
          // clear previous cached data
          gbfsV2FeedCache.clear(ishtarProvider);
          gbfsV3FeedCache.clear(ishtarProvider);
        }
      } else {
        // new provider
        log.info(
          "Add new provider with systemId {} / url {}",
          ishtarProvider.getSystemId(),
          ishtarProvider.getUrl()
        );
        providers.add(ishtarProvider);
        update = true;
      }
    }
    if (update) {
      log.info("Updated providers from ISHTAR");
      feedProviderConfig.setProviders(providers);
      restartFeedUpdaters();
    } else {
      log.info("No changes in providers");
    }
  }

  private static void updateFeedProvider(FeedProvider incoming, FeedProvider existing) {
    existing.setOperatorId(incoming.getOperatorId());
    existing.setOperatorName(incoming.getOperatorName());
    existing.setCodespace(incoming.getCodespace());
    existing.setUrl(incoming.getUrl());
    existing.setLanguage(incoming.getLanguage());
    existing.setAuthentication(incoming.getAuthentication());
    existing.setExcludeFeeds(incoming.getExcludeFeeds());
    existing.setAggregate(incoming.getAggregate());
    existing.setVehicleTypes(incoming.getVehicleTypes());
    existing.setPricingPlans(incoming.getPricingPlans());
    existing.setVersion(incoming.getVersion());
  }

  private void restartFeedUpdaters() {
    // restarts subscriptions based on FeedProviderConfig bean providers
    log.info("Restart feed updaters");
    this.feedUpdater.stop();
    this.feedUpdater.start();
  }
}
