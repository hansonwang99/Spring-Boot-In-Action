
# 《Spring Boot 工程集成全局唯一ID生成器 Vesta》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---


>本文内容脑图如下：

![本文内容脑图](https://upload-images.jianshu.io/upload_images/9824247-cd6b40e50cc5926a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



> 文章共 760字，阅读大约需要 2分钟 ！

---

## 概 述

在前一篇文章 [《Spring Boot工程集成全局唯一ID生成器 UidGenerator》](http://www.codesheep.cn/2018/10/24/springbt-uid-generator/) 中给大家推荐了一款由百度开发的基于 Snowflake算法实现的全局唯一ID生成器 **UidGenerator**，而本文则给大家再度推荐一款优秀的全局唯一ID生成器，名叫 Vesta。

[Vesta](https://github.com/cloudatee/vesta-id-generator) 是艳鹏大佬的开源作品，基于Java开发，其体验地址 [在此](http://vesta.cloudate.net/genid)。Vesta 是一款通用的 ID产生器，互联网俗称统一发号器，其具有几大很具有优势的特性：
- 全局唯一
- 粗略有序
- 可反解
- 可制造
- 分布式

而且支持三种发布模式：
- 嵌入式发布模式
- 中心服务器发布模式
- REST 发布模式

根据业务的性能需求，它可以产生 **最大峰值型** 和 **最小粒度型** 两种类型的 ID，它的实现架构使其具有高性能，高可用和可伸缩等互联网产品需要的质量属性，是一款通用的高性能的发号器产品。

本文就在 Spring Boot项目中将 Vesta耍起来！

>**注：** 本文首发于  [**My Personal Blog：CodeSheep·程序羊**](http://www.codesheep.cn)，欢迎光临 [**小站**](http://www.codesheep.cn)

---

## 基础工程搭建

Spring Boot基础工程的搭建我不再赘述，创建好工程后 `pom`中需要加入如下依赖：

```xml
        <dependency>
            <groupId>com.robert.vesta</groupId>
            <artifactId>vesta-service</artifactId>
            <version>0.0.1</version>
        </dependency>

        <dependency>
            <groupId>com.robert.vesta</groupId>
            <artifactId>vesta-intf</artifactId>
            <version>0.0.1</version>
        </dependency>
```

> 对应的 Jar包去编译一下 Vesta源码即可获得，[源码在此](https://github.com/cloudatee/vesta-id-generator)

---

## Vesta 配置导入

- **在项目`resources`目录中加入 Vesta的配置文件**

引入`vesta-rest.properties`，配置如下：

```
vesta.machine=1021  # 机器ID
vesta.genMethod=0   # 生成方式，0表示使用嵌入发布模式
vesta.type=1        # ID类型，1表示最小粒度型
```



引入 `vesta-rest-main.xml`，配置如下：

```
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd">

  <bean
    class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="locations" value="classpath:ext/vesta/vesta-rest.properties"/>
  </bean>

  <bean id="idService" class="com.robert.vesta.service.factory.IdServiceFactoryBean"
    init-method="init">
    <property name="providerType" value="PROPERTY"/>
    <property name="type" value="${vesta.type}"/>
    <property name="genMethod" value="${vesta.genMethod}"/>
    <property name="machineId" value="${vesta.machine}"/>
  </bean>

</beans>
```

好，接下来我们创建一个 Config配置类来将 `vesta-rest-main.xml`配置文件加载进项目

- **创建 UidConfig配置类**

```java
@Configuration
@ImportResource( locations = { "classpath:ext/vesta/vesta-rest-main.xml" } )
public class UidConfig {
}
```

---

## 编写 Vesta Service

这里面包含的是和 ID生成器相关的几个重要工具接口，主要有：

- `genId` 生成全局唯一 ID号
- `explainId` 反解全局唯一 ID号，得到可以解释 ID号含义的 JSON数据
- `makeId` 手工制造 ID

来看代码吧

```java
@Service
public class UidService {

    @Resource
    private IdService idService;

    public long genId() {
        return idService.genId();
    }

    public Id explainId( long id ) {
        return idService.expId(id);
    }

    public long makeId( long version, long type, long genMethod, long machine, long time, long seq ) {

        long madeId = -1;
        if (time == -1 || seq == -1)
            throw new IllegalArgumentException( "Both time and seq are required." );
        else if (version == -1) {
            if (type == -1) {
                if (genMethod == -1) {
                    if (machine == -1) {
                        madeId = idService.makeId(time, seq);
                    } else {
                        madeId = idService.makeId(machine, time, seq);
                    }
                } else {
                    madeId = idService.makeId(genMethod, machine, time, seq);
                }
            } else {
                madeId = idService.makeId(type, genMethod, machine, time, seq);
            }
        } else {
            madeId = idService.makeId(version, type, genMethod, time,
                    seq, machine);
        }

        return madeId;
    }

}
```


---

## 编写测试 Controller

我们针对上述 `UidService`中提供的三个工具接口来各自编写一个测试接口：

```java
@RestController
public class UidController {

    @Autowired
    private UidService uidService;

    @RequestMapping("/genid")
    public long genId() {
        return uidService.genId();
    }

    @RequestMapping("/expid")
    public Id explainId(@RequestParam(value = "id", defaultValue = "0") long id) {
        return uidService.explainId( id );
    }

    @RequestMapping("/makeid")
    public long makeId(
            @RequestParam(value = "version", defaultValue = "-1") long version,
            @RequestParam(value = "type", defaultValue = "-1") long type,
            @RequestParam(value = "genMethod", defaultValue = "-1") long genMethod,
            @RequestParam(value = "machine", defaultValue = "-1") long machine,
            @RequestParam(value = "time", defaultValue = "-1") long time,
            @RequestParam(value = "seq", defaultValue = "-1") long seq) {

        return uidService.makeId( version, type, genMethod, machine, time, seq );
    }
}
```

---

## 实验验证

- **实验一**

首先我们用浏览器调用接口 `genid`，来返回生成的全局唯一 ID流水号，一切都是那么的简单优雅：

![生成全局唯一流水号](https://upload-images.jianshu.io/upload_images/9824247-0c2908f764e051cb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


- **实验二**

由于 Vesta生成的全局唯一流水号具有 **可反解** 的优良特性，因此我们可以先生成一个流水号，然后调用 `expid`接口来反解出流水号所代表的意义：

![全局唯一流水号的反解效果](https://upload-images.jianshu.io/upload_images/9824247-ae83ee458f6b92a2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

## 后 记

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---

