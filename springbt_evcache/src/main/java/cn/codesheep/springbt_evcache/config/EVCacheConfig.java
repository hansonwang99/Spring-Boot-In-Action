package cn.codesheep.springbt_evcache.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EVCacheConfig {

    @Bean
    public EVCacheClientSample evcacheClient() {
        EVCacheClientSample evCacheClient = new EVCacheClientSample();
        return evCacheClient;
    }
}
