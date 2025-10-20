package org.entur.lamassu.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.entur.lamassu.mapper.entitymapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.FeedUrlUtil;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSData;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSDataset;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {

  private final FeedProviderService feedProviderService;
  private final SystemDiscoveryMapper systemDiscoveryMapper;
  private final String baseUrl;
  private final boolean enableGbfsV3ToV2Mapping;

  public SystemDiscoveryServiceImpl(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper,
    @Value("${org.entur.lamassu.baseUrl}") String baseUrl,
    @Value(
      "${fr.okina.lamassu.enableGbfsV3ToV2Mapping:false}"
    ) boolean enableGbfsV3ToV2Mapping
  ) {
    this.feedProviderService = feedProviderService;
    this.systemDiscoveryMapper = systemDiscoveryMapper;
    this.baseUrl = baseUrl;
    this.enableGbfsV3ToV2Mapping = enableGbfsV3ToV2Mapping;
  }

  public SystemDiscovery getSystemDiscovery(GBFSVersion.Version version) {
    // recomputed every time
    return mapSystemDiscovery(
      feedProviderService,
      systemDiscoveryMapper,
      version
    );
  }

  @Override
  public GBFSManifest getGBFSManifest() {
    return mapGBFSManifest(feedProviderService, baseUrl, enableGbfsV3ToV2Mapping);
  }

  @NotNull
  private SystemDiscovery mapSystemDiscovery(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper,
    GBFSVersion.Version version
  ) {
    List<FeedProvider> feedProviders = feedProviderService.getFeedProviders();
    if (!this.enableGbfsV3ToV2Mapping && (version == GBFSVersion.Version._2_3)) {
      // remove version V3 feed(s) from V2 discovery when v3 to v2 mapping is disabled
      feedProviders.removeIf(fp -> fp.getVersion().startsWith("3."));
    }
    var mappedSystemDiscovery = new SystemDiscovery();
    mappedSystemDiscovery.setSystems(
        feedProviders
        .stream()
        .map(fp -> systemDiscoveryMapper.mapSystemDiscovery(fp, version, this.enableGbfsV3ToV2Mapping))
        .toList()
    );
    return mappedSystemDiscovery;
  }

  public GBFSManifest mapGBFSManifest(
    FeedProviderService feedProviderService,
    String baseUrl,
    boolean enableGbfsV3ToV2Mapping
  ) {
    return new GBFSManifest()
      .withVersion(GBFSVersion.Version._3_0.toString())
      .withLastUpdated(new Date())
      .withTtl(3600)
      .withData(
        new GBFSData()
          .withDatasets(
            feedProviderService
              .getFeedProviders()
              .stream()
              .map(fp ->
                new GBFSDataset()
                  .withSystemId(fp.getSystemId())
                  .withVersions(mapGBFSVersions(fp, baseUrl, enableGbfsV3ToV2Mapping))
              )
              .toList()
          )
      );
  }

  private List<GBFSVersion> mapGBFSVersions(
    FeedProvider fp,
    String baseUrl,
    boolean enableGbfsV3ToV2Mapping
  ) {
    List<GBFSVersion> gbfsVersions = new ArrayList<>();
    if (
      StringUtils.isBlank(fp.getVersion()) ||
      fp.getVersion().startsWith("2") ||
      fp.getVersion().startsWith("3") &&
      enableGbfsV3ToV2Mapping
    ) {
      gbfsVersions.add(
        new GBFSVersion()
          .withVersion(GBFSVersion.Version._2_3)
          .withUrl(FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeedName.GBFS, fp).toString())
      );
    }
    if (fp.getVersion() != null && fp.getVersion().startsWith("3")) {
      gbfsVersions.add(
        new GBFSVersion()
          .withVersion(GBFSVersion.Version._3_0)
          .withUrl(FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeed.Name.GBFS, fp))
      );
    }
    return gbfsVersions;
  }
}
