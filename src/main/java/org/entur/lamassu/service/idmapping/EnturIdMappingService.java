package org.entur.lamassu.service.idmapping;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!mdm")
public class EnturIdMappingService extends BaseIdMappingService {}
