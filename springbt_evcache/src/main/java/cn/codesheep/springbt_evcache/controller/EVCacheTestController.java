package cn.codesheep.springbt_evcache.controller;

import cn.codesheep.springbt_evcache.service.EVCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EVCacheTestController {

    @Autowired
    private EVCacheService evCacheService;

    @GetMapping("/testevcache")
    public void testEvcache() {

        try {

            // Set ten keys to different values
            for ( int i = 0; i < 10; i++ ) {
                String key = "key_" + i;
                String value = "data_" + i;
                // Set the TTL to 10s
                int ttl = 10;
                evCacheService.setKey(key, value, ttl);
            }

            // Do a "get" for each of those same keys
            for (int i = 0; i < 10; i++) {
                String key = "key_" + i;
                String value = evCacheService.getKey(key);
                System.out.println("Get of " + key + " returned " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
