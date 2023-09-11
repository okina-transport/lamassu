package org.entur.lamassu.service.impl;

import org.entur.lamassu.mapper.entitymapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {
    private SystemDiscovery systemDiscovery;

    @Autowired
    private FeedProviderService feedProviderService;

    @Autowired
    private SystemDiscoveryMapper systemDiscoveryMapper;



    public SystemDiscoveryServiceImpl() {
    }

    @PostConstruct
    @Override
    public void resetSystemDiscovery(){
        systemDiscovery = new SystemDiscovery();
        systemDiscovery.setSystems(
                feedProviderService.getFeedProviders().stream()
                        .map(systemDiscoveryMapper::mapSystemDiscovery).collect(Collectors.toList())
        );
    }

    @Override
    public SystemDiscovery getSystemDiscovery() {
        return systemDiscovery;
    }
}
