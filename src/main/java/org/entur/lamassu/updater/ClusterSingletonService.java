package org.entur.lamassu.updater;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ClusterSingletonService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RLock lock;
    private boolean isLeader = false;

    @Autowired
    FeedUpdateScheduler feedUpdateScheduler;

    public ClusterSingletonService(RedissonClient redisson) {
        this.lock = redisson.getLock("leader");
    }

    /**
     * Check leadership status every 30 seconds
     *
     * If we are currently the leader, renew leadership lease and if that fails, stop scheduling updates
     *
     * If we are not currently the leader, try to become leader, and if that succeeds, start scheduling updates.
     *
     * Leadership lease time is 60 seconds.
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        if (isLeader()) {
            logger.info("I am already the leader. Will try to renew.");
            try {
                boolean res = tryToBecomeLeader();
                if (res) {
                    logger.info("Leadership renewed.");
                } else {
                    logger.info("Lost leadership");
                    isLeader = false;
                    feedUpdateScheduler.stop();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            logger.info("Trying to become leader.");
            try {
                boolean res = tryToBecomeLeader();
                if (res) {
                    logger.info("I became the leader");
                    isLeader = true;
                    feedUpdateScheduler.start();
                } else {
                    logger.info("Sorry, someone else is the leader, try again soon");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isLeader() {
        return lock.isHeldByCurrentThread() || isLeader;
    }

    private boolean tryToBecomeLeader() throws InterruptedException {
        return lock.tryLock(1, 60, TimeUnit.SECONDS);
    }
}
