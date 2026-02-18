package org.entur.lamassu.mapper.feedprovider;

import java.util.Collections;
import org.entur.lamassu.client.ishtar.dto.LamassuAuthDto;
import org.entur.lamassu.client.ishtar.dto.LamassuProviderDto;
import org.entur.lamassu.model.provider.Authentication;
import org.entur.lamassu.model.provider.AuthenticationScheme;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.GbfsUtils;
import org.springframework.stereotype.Component;

@Component
public class FeedProviderMapper {

  public FeedProvider mapFromApiResponse(LamassuProviderDto providerData) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId(providerData.getSystemId());
    provider.setOperatorId(providerData.getOperatorId());
    provider.setOperatorName(providerData.getOperatorName());
    provider.setCodespace(providerData.getCodespace().toUpperCase());
    provider.setUrl(providerData.getUrl());
    provider.setLanguage(providerData.getLanguage());
    provider.setVersion(providerData.getVersion());
    provider.setAggregate(providerData.getAggregate());
    provider.setGbfsModality(providerData.getModality());
    provider.setExcludeFeeds(
      GbfsUtils.convertToGBFSFeedNameList(providerData.getExcludeFeeds())
    );

    provider.setPricingPlans(Collections.emptyList());
    provider.setVehicleTypes(Collections.emptyList());
    mapAuthentication(providerData.getAuthentication(), provider);

    return provider;
  }

  private static void mapAuthentication(
    LamassuAuthDto lamassuAuthDto,
    FeedProvider provider
  ) {
    if (lamassuAuthDto != null) {
      Authentication authentication = new Authentication();

      AuthenticationScheme authType = lamassuAuthDto.getType();
      if (authType != null) {
        authentication.setScheme(authType);
        authentication.setProperties(authentication.getProperties());
        provider.setAuthentication(authentication);
      }
    }
  }
}
