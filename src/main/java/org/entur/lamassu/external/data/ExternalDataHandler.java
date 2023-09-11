package org.entur.lamassu.external.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_3.gbfs.GBFS;
import org.entur.gbfs.v2_3.gbfs.GBFSFeed;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_3.gbfs.GBFSFeeds;
import org.entur.gbfs.v2_3.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_3.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.leader.feedcachesupdater.FeedCachesUpdater;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.FeedUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.entur.lamassu.util.Constants.SYSTEM_ID_HEADER_NAME;

/**
 * Class to handle external data comming through activeMq and import into Lamassu cache
 */
@Service
public class ExternalDataHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExternalDataHandler.class);

    @Autowired
    private FeedCachesUpdater feedCachesUpdater;

    @Autowired
    private FeedProviderService feedProviderService;

    @Autowired
    private SystemDiscoveryService systemDiscoveryService;

    @Autowired
    private GBFSFeedCache feedCache;

    @Value("${org.entur.lamassu.baseUrl}")
    private String baseUrl;

    private ObjectMapper objectMapper = new ObjectMapper();


    /**
     * Read pricing plan from activeMq and ingest it into the cache
     * @param e
     *  exchange from activeMq
     */
    public void processIncomingPricingPlan(Exchange e) {
        String pricingPlanJson = e.getIn().getBody(String.class);

        try {
            GBFSSystemPricingPlans pricingPlan = objectMapper.readValue(pricingPlanJson, GBFSSystemPricingPlans.class);
            String systemId = e.getMessage().getHeader(SYSTEM_ID_HEADER_NAME, String.class);
            FeedProvider feedProvider = getOrCreateProvider(systemId);
            if (pricingPlan.getTtl() == null) {
                pricingPlan.setTtl(3600);
            }

            feedCachesUpdater.updateFeedCache(feedProvider, GBFSFeedName.SystemPricingPlans, pricingPlan);
            updateFeeds(GBFSFeedName.SystemPricingPlans, feedProvider);

        } catch (Exception ex) {
            logger.error("Error while reading external pricing plan data", e);
        }

    }

    /**
     * Read Free bike status from activeMq and ingest it into the cache
     * @param e
     *  exchange from activeMq
     */

    public void processIncomingFreeBikeStatus(Exchange e) {

        String freeBikeStatusJson = e.getIn().getBody(String.class);
        try {
            GBFSFreeBikeStatus freeBikeStatus = objectMapper.readValue(freeBikeStatusJson, GBFSFreeBikeStatus.class);
            String systemId = e.getMessage().getHeader(SYSTEM_ID_HEADER_NAME, String.class);
            FeedProvider feedProvider = getOrCreateProvider(systemId);
            if (freeBikeStatus.getTtl() == null) {
                freeBikeStatus.setTtl(3600);
            }

            feedCachesUpdater.updateFeedCache(feedProvider, GBFSFeedName.FreeBikeStatus, freeBikeStatus);
            updateFeeds(GBFSFeedName.FreeBikeStatus, feedProvider);

        } catch (Exception ex) {
            logger.error("Error while reading external free bike status data", e);
        }

    }

    /**
     * Read station information from activeMq and ingest it into the cache
     * @param e
     *  exchange from activeMq
     */

    public void processIncomingStationInformation(Exchange e) {

        String stationInformationJson = e.getIn().getBody(String.class);

        try {
            GBFSStationInformation stationInformation = objectMapper.readValue(stationInformationJson, GBFSStationInformation.class);
            String systemId = e.getMessage().getHeader(SYSTEM_ID_HEADER_NAME, String.class);

            FeedProvider feedProvider = getOrCreateProvider(systemId);
            if (stationInformation.getTtl() == null) {
                stationInformation.setTtl(3600);
            }

            feedCachesUpdater.updateFeedCache(feedProvider, GBFSFeedName.StationInformation, stationInformation);
            updateFeeds(GBFSFeedName.StationInformation, feedProvider);

        } catch (Exception ex) {
            logger.error("Error while reading external station information data", e);
        }


    }

    /**
     * Update feed list stored in Lamassu
     * @param feedName
     *  the name of the feed to add
     * @param feedProvider
     *  the provider for which a new feed must be added
     */
    private void updateFeeds(GBFSFeedName feedName, FeedProvider feedProvider) {

        Map<String, GBFSFeeds> feedsData = new HashMap<>();
        GBFSFeeds frFeeds = new GBFSFeeds();
        List<GBFSFeed> feedList = new ArrayList<>();
        GBFSFeed systemFeed = new GBFSFeed();
        systemFeed.setName(feedName);
        systemFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, feedName, feedProvider));
        feedList.add(systemFeed);
        frFeeds.setFeeds(feedList);
        feedsData.put("fr", frFeeds);
        updateGBFSFeed(feedProvider, feedsData);
    }


    /**
     * Read system information from activeMq and ingest it into the cache
     * @param e
     *  exchange from activeMq
     */
    public void processIncomingSystemInformation(Exchange e) {

        String systemInformationJson = e.getIn().getBody(String.class);
        try {
            GBFSSystemInformation systemInformation = objectMapper.readValue(systemInformationJson, GBFSSystemInformation.class);
            String systemId = e.getMessage().getHeader(SYSTEM_ID_HEADER_NAME, String.class);

            FeedProvider feedProvider = getOrCreateProvider(systemId);
            if (systemInformation.getTtl() == null) {
                systemInformation.setTtl(3600);
            }

            feedCachesUpdater.updateFeedCache(feedProvider, GBFSFeedName.SystemInformation, systemInformation);
            updateFeeds(GBFSFeedName.SystemInformation, feedProvider);

        } catch (Exception ex) {
            logger.error("Error while reading external system information data", e);
        }


    }


    /**
     * Update GBFSFeeds stored in LAMASSU
     * @param feedProvider
     *  the provider for which feeds must be updated
     * @param newFeedsData
     *  the updated feed data
     */
    private void updateGBFSFeed(FeedProvider feedProvider, Map<String, GBFSFeeds> newFeedsData) {

        Object gbfsFeedObj = feedCache.find(GBFSFeedName.GBFS, feedProvider);


        if (gbfsFeedObj == null) {
            GBFS gbfsFeed = new GBFS();
            gbfsFeed.setVersion("2.3");
            gbfsFeed.setTtl(3600);
            long currentTimeSeconds = System.currentTimeMillis() / 1000;
            gbfsFeed.setLastUpdated((int) currentTimeSeconds);
            gbfsFeed.setFeedsData(newFeedsData);
            feedCachesUpdater.updateFeedCache(feedProvider, GBFSFeedName.GBFS, gbfsFeed);
        } else {
            GBFS gbfsFeed = (GBFS) gbfsFeedObj;


            for (Map.Entry<String, GBFSFeeds> entry : newFeedsData.entrySet()) {
                // key = fr,en...
                String key = entry.getKey();
                GBFSFeeds value = entry.getValue();


                if (gbfsFeed.getFeedsData().containsKey(key)) {
                    // language is already existing. Looking into the language if newFeeds are existing or need to be added
                    GBFSFeeds existingFeedsData = gbfsFeed.getFeedsData().get(key);

                    for (GBFSFeed newFeed : value.getFeeds()) {
                        addFeed(existingFeedsData, newFeed);
                    }
                } else {
                    // feeds are not existing for this language. Adding new language
                    gbfsFeed.getFeedsData().put(key, value);
                }
            }

            feedCachesUpdater.updateFeedCache(feedProvider, GBFSFeedName.GBFS, gbfsFeed);

        }
    }


    /**
     * Adds a new GBFSFeed to theexisting ones
     * @param existingFeedsData
     *  all existings feed data
     * @param newFeed
     *  the new feed to add
     */
    private void addFeed(GBFSFeeds existingFeedsData, GBFSFeed newFeed) {

        for (GBFSFeed feed : existingFeedsData.getFeeds()) {
            if (feed.getName().equals(newFeed.getName())) {
                return;
            }
        }

        existingFeedsData.getFeeds().add(newFeed);
    }


    /**
     * Recover a provider from a systemId. Creates it if it does not exist
     * @param systemId
     *  the systemId for which the provider must be recovered
     * @return
     *  the feed provider
     */
    private FeedProvider getOrCreateProvider(String systemId) {

        FeedProvider foundProvider = feedProviderService.getFeedProviderBySystemId(systemId);

        if (foundProvider != null) {
            return foundProvider;
        }

        FeedProvider newProv = new FeedProvider();
        newProv.setSystemId(systemId);
        newProv.setLanguage("fr");
        feedProviderService.addProvider(newProv);
        systemDiscoveryService.resetSystemDiscovery();


        return newProv;

    }
}
