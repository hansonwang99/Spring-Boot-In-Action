
# 《Spring Boot应用监控实战》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---




---

## 概述

之前讲过[Docker容器的可视化监控](https://www.jianshu.com/p/9e47ffaf5e31)，即监控容器的运行情况，包括 CPU使用率、内存占用、网络状况以及磁盘空间等等一系列信息。同样利用SpringBoot作为微服务单元的实例化技术选型时，我们不可避免的要面对的一个问题就是如何实时监控应用的运行状况数据，比如：健康度、运行指标、日志信息、线程状况等等。本文就该问题做一点探索并记录试验过程。

>**注：** 本文原载于  [**My Personal Blog：**](http://www.codesheep.cn)， [**CodeSheep · 程序羊**](http://www.codesheep.cn) ！

---

## 入门使用：Actuator插件

Actuator插件是SpringBoot原生提供的一个服务，可以通过暴露端点路由，用来输出应用中的诸多 **端点信息**。实战一下！

- pom.xml中添加依赖：

```
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

启动Spring Boot应用程序之后，只要在浏览器中输入端点信息就能获得应用的一些状态信息。

常用端点列举如下，可以一个个详细试一下：

- /info           　　　　　　　应用基本信息
- /health       　　　　　　健康度信息
- /metrics     　　　　　运行指标
- /env           　　　　　　　环境变量信息
- /loggers    　　　　　日志相关
- /dump       　　　　　　线程相关信息
- /trace      　　　　　　请求调用轨迹

当然此时只能使用`/health` 和 `/info`端点，其他因为权限问题无法访问。想访问指定端点的话可以在yml配置中添加相关的配置项，比如`/metrics`端点则需要配置：

```
endpoints:
  metrics:
    sensitive: false
```

此时浏览器访问/metrics端点就能得到诸如下面所示的信息：

```
{
	"mem": 71529,
	"mem.free": 15073,
	"processors": 4,
	"instance.uptime": 6376,
	"uptime": 9447,
	"systemload.average": -1.0,
	"heap.committed": 48024,
	"heap.init": 16384,
	"heap.used": 32950,
	"heap": 506816,
	"nonheap.committed": 23840,
	"nonheap.init": 160,
	"nonheap.used": 23506,
	"nonheap": 0,
	"threads.peak": 25,
	"threads.daemon": 23,
	"threads.totalStarted": 28,
	"threads": 25,
	"classes": 6129,
	"classes.loaded": 6129,
	"classes.unloaded": 0,
	"gc.copy.count": 74,
	"gc.copy.time": 173,
	"gc.marksweepcompact.count": 3,
	"gc.marksweepcompact.time": 88,
	"httpsessions.max": -1,
	"httpsessions.active": 0
}
```

当然也可以开启全部端点权限，只需如下配置即可：

```
endpoints:
  sensitive: false
```

由于Actuator插件提供的监控能力毕竟有限，而且UI比较简陋，因此需要一个更加成熟一点的工具

---

## Spring Boot Admin监控系统

SBA则是基于Actuator更加进化了一步，其是一个针对Actuator接口进行UI美化封装的监控工具。我们来实验一下。

- 首先来创建一个Spring Boot Admin Server工程作为服务端

pom.xml中加入如下依赖：

```
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server</artifactId>
	<version>1.5.7</version>
</dependency>

<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-server-ui</artifactId>
	<version>1.5.7</version>
</dependency>
```

然后在应用主类上通过加注解来启用Spring Boot Admin

```
@EnableAdminServer
@SpringBootApplication
public class SpringbtAdminServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbtAdminServerApplication.class, args);
	}
}
```

启动程序，浏览器打开 `localhost:8081` 查看Spring Boot Admin主页面：

![Spring Boot Admin主页面](https://upload-images.jianshu.io/upload_images/9824247-47e70841db255449.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

此时Application一栏空空如也，等待待监控的应用加入

- 创建要监控的Spring Boot应用

pom.xml中加入以下依赖

```
<dependency>
	<groupId>de.codecentric</groupId>
	<artifactId>spring-boot-admin-starter-client</artifactId>
	<version>1.5.7</version>
</dependency>
```

然后在yml配置中添加如下配置，将应用注册到Admin服务端去：

```
spring:
  boot:
    admin:
      url: http://localhost:8081
      client:
        name: AdminTest
```

Client应用一启动，Admin服务立马推送来了消息，告诉你AdminTest上线了：

![应用上线推送消息](https://upload-images.jianshu.io/upload_images/9824247-acfd45ffe9c94676.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

此时去Admin主界面上查看，发现Client应用确实已经注册上来了：

![Client应用已注册上来](https://upload-images.jianshu.io/upload_images/9824247-28f8817ac21e4ea4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 查看Detail

![Detail信息](https://upload-images.jianshu.io/upload_images/9824247-c2dcddd5b96c79e5.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



- 查看 Metrics

![Metrics信息](https://upload-images.jianshu.io/upload_images/9824247-be0cc63e4a60d61e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 查看 Enviroment

![Enviroment信息](https://upload-images.jianshu.io/upload_images/9824247-476b042bce07cbe6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 查看JMX

![JMX信息](https://upload-images.jianshu.io/upload_images/9824247-de237fa0450c7818.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 查看Threads

![Threads信息](https://upload-images.jianshu.io/upload_images/9824247-e7e5f254a8642d48.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 查看Trace与详情

![Trace信息](https://upload-images.jianshu.io/upload_images/9824247-1a4f1fe47603045c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


点击最上方JOURNAL，会看到被监控应用程序的事件变化：

![应用程序的事件变化信息](https://upload-images.jianshu.io/upload_images/9824247-ff6097407f42d281.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

图中可以清晰地看到，应用从 **REGISTRATION → UNKNOWN → UP** 的状态跳转。

这样就将Actuator插件提供的所有端点信息在SBA中全部尝试了一遍。

---

## 参考文献

- http://codecentric.github.io/spring-boot-admin/1.5.7/

---

## 后记

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---
