package org.entur.lamassu.service;

import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSVersion;

public interface SystemDiscoveryService {
  SystemDiscovery getSystemDiscovery(GBFSVersion.Version version);
  GBFSManifest getGBFSManifest();
}
