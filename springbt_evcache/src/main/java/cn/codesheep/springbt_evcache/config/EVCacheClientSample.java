package cn.codesheep.springbt_evcache.config;

import com.netflix.evcache.EVCache;
import com.netflix.evcache.EVCacheException;

import java.util.concurrent.Future;

public class EVCacheClientSample {

    private final EVCache evCache;
    private boolean verboseMode = false;


    public EVCacheClientSample() {
        String deploymentDescriptor = System.getenv("EVC_SAMPLE_DEPLOYMENT");
        if (deploymentDescriptor == null) {
            // No deployment descriptor in the environment, use a default: two local
            // memcached processes configured as two replicas of one shard each.
            deploymentDescriptor = "SERVERGROUP1=192.168.199.77:11211;SERVERGROUP2=192.168.199.78:11211";
        }
        System.setProperty("EVCACHE_APP1.use.simple.node.list.provider", "true");
        System.setProperty("EVCACHE_APP1-NODES", deploymentDescriptor);
        evCache = new EVCache.Builder().setAppName("EVCACHE_APP1").build();
    }



    public void setKey(String key, String value, int timeToLive) throws Exception {
        try {
            Future<Boolean>[] _future = evCache.set(key, value, timeToLive);

            // Wait for all the Futures to complete.
            // In "verbose" mode, show the status for each.
            for (Future<Boolean> f : _future) {
            	boolean didSucceed = f.get();
            	if (verboseMode) {
                    System.out.println("per-shard set success code for key " + key + " is " + didSucceed);
                }
            }
            if (!verboseMode) {
                // Not verbose. Just give one line of output per "set," without a success code
                System.out.println("finished setting key " + key);
            }
        } catch (EVCacheException e) {
            e.printStackTrace();
        }
    }



    public String getKey(String key) {
        try {
            String _response = evCache.<String>get(key);
            return _response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setVerboseMode( boolean verboseMode ) {
        this.verboseMode = verboseMode;
    }

    public boolean setVerboseMode() {
        return this.verboseMode;
    }


//    public static void main(String[] args) {
//
//        // set verboseMode based on the environment variable
//        verboseMode = ("true".equals(System.getenv("EVCACHE_SAMPLE_VERBOSE")));
//
//        if (verboseMode) {
//            System.out.println("To run this sample app without using Gradle:");
//            System.out.println("java -cp " + System.getProperty("java.class.path") + " com.netflix.evcache.sample.EVCacheClientSample");
//        }
//
//        try {
//            EVCacheClientSample evCacheClientSample = new EVCacheClientSample();
//
//            // Set ten keys to different values
//            for (int i = 0; i < 10; i++) {
//                String key = "key_" + i;
//                String value = "data_" + i;
//                // Set the TTL to 24 hours
//                int ttl = 10;
//                evCacheClientSample.setKey(key, value, ttl);
//            }
//
//            // Do a "get" for each of those same keys
//            for (int i = 0; i < 10; i++) {
//                String key = "key_" + i;
//                String value = evCacheClientSample.getKey(key);
//                System.out.println("Get of " + key + " returned " + value);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//	    System.exit(0);
//    }
}
