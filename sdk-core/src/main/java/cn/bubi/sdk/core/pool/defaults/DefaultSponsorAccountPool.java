package cn.bubi.sdk.core.pool.defaults;

import cn.bubi.sdk.core.pool.SponsorAccount;
import cn.bubi.sdk.core.pool.SponsorAccountPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiezhengchao@bubi.cn
 * @since 17/11/20 上午10:40.
 * 默认账户池实现
 */
public class DefaultSponsorAccountPool implements SponsorAccountPool{

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSponsorAccountPool.class);

    private ConcurrentLinkedQueue<SponsorAccount> availableQueue = new ConcurrentLinkedQueue<>();// 可用队列
    private ConcurrentHashMap<String, SponsorAccount> unAvailableSponsorAccountMap = new ConcurrentHashMap<>();// 不可用集合
    private ScheduledThreadPoolExecutor scheduledCheck = new ScheduledThreadPoolExecutor(2);

    public DefaultSponsorAccountPool(){
        scheduledCheck.scheduleAtFixedRate(new UnAvailableSponsorAccountCheck(), 1, 30, TimeUnit.MINUTES);
    }

    @Override
    public void addSponsorAccount(SponsorAccount... sponsorAccounts){
        availableQueue.addAll(Arrays.asList(sponsorAccounts));
    }

    @Override
    public SponsorAccount getRichSponsorAccount(){
        SponsorAccount sponsorAccount = availableQueue.poll();
        if (sponsorAccount == null) {
            throw new RuntimeException("getRichSponsorAccount result is null! please check config");
        }
        sponsorAccount.initExpire();
        unAvailableSponsorAccountMap.put(sponsorAccount.getAddress(), sponsorAccount);
        return sponsorAccount;
    }

    @Override
    public void notifyRecover(String address){
        SponsorAccount sponsorAccount = unAvailableSponsorAccountMap.remove(address);
        if (sponsorAccount != null) {
            availableQueue.add(sponsorAccount);
        }
    }

    private class UnAvailableSponsorAccountCheck implements Runnable{

        @Override
        public void run(){
            try {
                Collection<SponsorAccount> sponsorAccounts = unAvailableSponsorAccountMap.values();
                Iterator<SponsorAccount> sponsorAccountIterator = sponsorAccounts.iterator();
                while (sponsorAccountIterator.hasNext()) {
                    SponsorAccount sponsorAccount = sponsorAccountIterator.next();
                    if (sponsorAccount.expire()) {
                        sponsorAccountIterator.remove();
                        availableQueue.add(sponsorAccount);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("UnAvailableSponsorAccountCheck throw exception!", e);
            }
        }
    }

}
