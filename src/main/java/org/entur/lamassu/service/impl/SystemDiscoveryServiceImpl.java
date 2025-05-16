package org.entur.lamassu.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {

  private final SystemDiscovery systemDiscovery;
  private final GBFSManifest gbfsManifest;

  public SystemDiscoveryServiceImpl(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper,
    @Value("${org.entur.lamassu.baseUrl}") String baseUrl,
    @Value(
      "${fr.okina.lamassu.enableGbfsV3ToV2Mapping:false}"
    ) boolean enableGbfsV3ToV2Mapping
  ) {
    this.systemDiscovery = mapSystemDiscovery(feedProviderService, systemDiscoveryMapper);
    this.gbfsManifest =
      mapGBFSManifest(feedProviderService, baseUrl, enableGbfsV3ToV2Mapping);
  }

  @Override
  public SystemDiscovery getSystemDiscovery() {
    return systemDiscovery;
  }

  @Override
  public GBFSManifest getGBFSManifest() {
    return gbfsManifest;
  }

  @NotNull
  private SystemDiscovery mapSystemDiscovery(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper
  ) {
    var mappedSystemDiscovery = new SystemDiscovery();
    mappedSystemDiscovery.setSystems(
      feedProviderService
        .getFeedProviders()
        .stream()
        .map(systemDiscoveryMapper::mapSystemDiscovery)
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
      fp.getVersion() == null ||
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
    gbfsVersions.add(
      new GBFSVersion()
        .withVersion(GBFSVersion.Version._3_0)
        .withUrl(FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeed.Name.GBFS, fp))
    );
    return gbfsVersions;
  }
}
