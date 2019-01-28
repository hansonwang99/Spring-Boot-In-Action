package cn.codesheep.springbt_evcache.service;

import cn.codesheep.springbt_evcache.config.EVCacheClientSample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EVCacheService {

    @Autowired
    private EVCacheClientSample evCacheClient;

    public void setKey( String key, String value, int timeToLive ) {
        try {
            evCacheClient.setKey( key, value, timeToLive );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getKey( String key ) {
        return evCacheClient.getKey( key );
    }
}
