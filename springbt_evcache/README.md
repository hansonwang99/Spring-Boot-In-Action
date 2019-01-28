
# 《EVCache缓存在 Spring Boot中的实战》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---


---

## 概 述

[EVCache](https://github.com/Netflix/EVCache) 是 **Netflix**开源的分布式缓存系统，基于 Memcached缓存和 Spymemcached客户端实现，其用在了大名鼎鼎的 AWS亚马逊云上，并且为云计算做了优化，提供高效的缓存服务。

本文利用 Memcached作为后端缓存实例服务器，并结合 Spring Boot，来实践一下 EVCache客户端的具体使用。

>**注：** 本文首发于  [**My Personal Blog：CodeSheep·程序羊**](https://www.codesheep.cn)，欢迎光临 [**小站**](https://www.codesheep.cn)

---

## 编译 EVCache

- **第一步：Clone**

```
git clone git@github.com:Netflix/EVCache.git
```

- **第二步：编译构建**

```
 ./gradlew build
Downloading https://services.gradle.org/distributions/gradle-2.10-bin.zip
.................................................................................................................................

...

:evcache-client:check
:evcache-client:build
:evcache-client-sample:writeLicenseHeader
:evcache-client-sample:licenseMain
Missing header in: evcache-client-sample/src/main/java/com/netflix/evcache/sample/EVCacheClientSample.java
:evcache-client-sample:licenseTest UP-TO-DATE
:evcache-client-sample:license
:evcache-client-sample:compileTestJava UP-TO-DATE
:evcache-client-sample:processTestResources UP-TO-DATE
:evcache-client-sample:testClasses UP-TO-DATE
:evcache-client-sample:test UP-TO-DATE
:evcache-client-sample:check
:evcache-client-sample:build

BUILD SUCCESSFUL

Total time: 22.866 secs
```

- **第三步：得到构建生成物**

![得到 EVCache构建生成物](https://upload-images.jianshu.io/upload_images/9824247-7aeca2727ce2b075.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


同时 `~/EVCache/evcache-client/build/reports` 目录下会生成相应构建报告：

![EVCache构建报告](https://upload-images.jianshu.io/upload_images/9824247-b206e34be4b40c58.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> 接下来我们结合 Spring工程，来实战一下 EVCache Client的具体使用。

---

## 环境准备 / 工程搭建

首先准备好两台 memcached实例：

- **192.168.199.77:11211**
- **192.168.199.78:11211**

接下来搭建一个SpringBoot工程，过程不再赘述，需要注意的一点是 pom中需加入 EVCache的依赖支持

```
<dependency>
	<groupId>com.netflix.evcache</groupId>
	<artifactId>evcache-client</artifactId>
	<version>4.137.0-SNAPSHOT</version>
</dependency>
```

>注：我将 Spring工程设置在 8899端口启动

---

## EVCache Client导入

- **编写 EVCache Client包装类**

```
public class EVCacheClient {

    private final EVCache evCache;   // 关键角色在此

    public EVCacheClient() {
        String deploymentDescriptor = System.getenv("EVC_SAMPLE_DEPLOYMENT");
        if ( deploymentDescriptor == null ) {
            deploymentDescriptor = "SERVERGROUP1=192.168.199.77:11211;SERVERGROUP2=192.168.199.78:11211";
        }
        System.setProperty("EVCACHE_APP1.use.simple.node.list.provider", "true");
        System.setProperty("EVCACHE_APP1-NODES", deploymentDescriptor);
        evCache = new EVCache.Builder().setAppName("EVCACHE_APP1").build();
    }

    public void setKey(String key, String value, int timeToLive) throws Exception {
        try {
            Future<Boolean>[] _future = evCache.set(key, value, timeToLive);
            for (Future<Boolean> f : _future) {
            	boolean didSucceed = f.get();
            	// System.out.println("per-shard set success code for key " + key + " is " + didSucceed);
                // 此处可以针对 didSucceed做相应判断
            }
            System.out.println("finished setting key " + key);
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

}
```

很明显上述类主要提供了两个关键工具函数： `setKey` 和 `getKey` 


- **EVCache Config 配置导入**

我们将 EVCacheClient 注入到Spring容器中

```
@Configuration
public class EVCacheConfig {

    @Bean
    public EVCacheClient evcacheClient() {
        EVCacheClient evCacheClient = new EVCacheClient();
        return evCacheClient;
    }
}
```

---

## 编写 EVCache Service

上面几步完成之后，Service的编写自然顺理成章，仅仅是一层封装而已

```
@Service
public class EVCacheService {

    @Autowired
    private EVCacheClient evCacheClient;

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
```

---

## 编写测试 Controller

我们编写一个方便用于测试的控制器，里面进行一系列对于缓存的 `set` 和 `get`，从而便于观察实验结果

```
@RestController
public class EVCacheTestController {

    @Autowired
    private EVCacheService evCacheService;

    @GetMapping("/testevcache")
    public void testEvcache() {

        try {

            for ( int i = 0; i < 10; i++ ) {
                String key = "key_" + i;
                String value = "data_" + i;
                int ttl = 180;           // 此处将缓存设为三分钟（180s）生存期，时间一过，缓存即会失效
                evCacheService.setKey(key, value, ttl);
            }

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
```

---

## 实验验证

工程启动后，我们调用 Rest接口：`localhost:8899/testevcache`，观察控制台中对于 `key_0` 到 `key_9` 等十个缓存 key的操作细节如下：

- **在 memcached集群中插入十条数据： `key_0` 到 `key_9`**

> 注意此处是向每个后端 memcached缓存实例中都写入了 10条测试数据

![在 memcached集群中插入十条数据](https://upload-images.jianshu.io/upload_images/9824247-06e88cfb3b959caf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **从后端 memcached集群中读取刚插入的 10条数据**

![从后端 memcached集群中读取刚插入的 10条数据](https://upload-images.jianshu.io/upload_images/9824247-b9a5028ff7aadd0d.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



- **为了验证数据确实写入到后端 memcached，我们可以 telnet到后端 memcached中进行一一验证**

而且这些数据的有效时间仅3分钟，3分钟后再次验证会发现数据已过期

```
[root@localhost ~]# telnet 127.0.0.1 11211
Trying 127.0.0.1...
Connected to 127.0.0.1.
Escape character is '^]'.
get key_0
VALUE key_0 0 6
data_0
END
get key_1
VALUE key_1 0 6
data_1
END
get key_2       
VALUE key_2 0 6
data_2
END
get key_3
VALUE key_3 0 6
data_3
END
get key_4
VALUE key_4 0 6
data_4
END
get key_5
VALUE key_5 0 6
data_5
END
get key_6
VALUE key_6 0 6
data_6
END
get key_7
VALUE key_7 0 6
data_7
END
get key_8
VALUE key_8 0 6
data_8
END
get key_9
VALUE key_9 0 6
data_9
END
```

---

## 本文扩展

当然本文所演示的 EVCache配合 memcached使用时，memcached被硬编码进代码，实际过程中使用，可以将其与 ZK等服务发现服务进行一个结合，实现灵活运用，这就不在本文进行赘述。

![结合服务发现灵活运用](https://upload-images.jianshu.io/upload_images/9824247-0913d40fc91ceb85.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


---

## 后 记

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---


