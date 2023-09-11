package org.entur.lamassu.external.data;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class ExternalDataRouteBuilder extends RouteBuilder {

    @Value("${realtime.gbfs.system.information.process.queue}")
    private String externalGBFSSystemInformationQueue;

    @Value("${realtime.gbfs.station.information.process.queue}")
    private String externalGBFSStationInformationQueue;

    @Value("${realtime.gbfs.free.bike.status.process.queue}")
    private String externalGBFSFreeBikeStatusQueue;

    @Value("${realtime.gbfs.pricing.plans.process.queue}")
    private String externalGBFSPricingPlanQueue;




    @Override
    public void configure() throws Exception {
        from(externalGBFSSystemInformationQueue)
                .bean(ExternalDataHandler.class, "processIncomingSystemInformation");

        from(externalGBFSStationInformationQueue)
                .bean(ExternalDataHandler.class, "processIncomingStationInformation");

        from(externalGBFSFreeBikeStatusQueue)
                .bean(ExternalDataHandler.class, "processIncomingFreeBikeStatus");

        from(externalGBFSPricingPlanQueue)
                .bean(ExternalDataHandler.class, "processIncomingPricingPlan");
    }
}
