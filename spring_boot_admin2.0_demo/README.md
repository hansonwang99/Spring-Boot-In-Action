
# 《Spring Boot Admin 2.0开箱体验》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---

---

## 概述

在我之前的 [《Spring Boot应用监控实战》](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247483771&idx=1&sn=7c5f103a816c16e453e04141d7433bf9&chksm=fdded7bfcaa95ea9a5dbe81114d32c1908bf8da0b3366bfbfcbe2473445cdba73c5e2060d5f3#rd) 一文中，讲述了如何利用 **Spring Boot Admin 1.5.X** 版本来可视化地监控 Spring Boot 应用。说时迟，那时快，现在 Spring Boot Admin 都更新到 **2.0** 版本了，并且可以对当下热门的 **Spring Boot 2.0**  和 **Spring Cloud Finchley.RELEASE** 进行监控，因此本文就来了解并实践一下！

>**注：** 本文原载于  [**My Personal Blog：**](http://www.codesheep.cn)， [**CodeSheep · 程序羊**](http://www.codesheep.cn) ！

---

---

## Spring Boot Admin 2.0新特性

Spring Boot Admin 2.0 变化还是挺多的，具体参考 [官网说明](http://codecentric.github.io/spring-boot-admin/current/#_changes_with_2_x)，这里列几条主要的：

- 使用Vue.js重写了UI界面，漂亮得不像实力派

- 直接集成了基于 spring security 的认证，无需引入第三方模块

- 加入 session endpoint 的监控支持

等等...

下面就实际试验来操作感受一下！

---

---

## 搭建 Spring Boot Admin Server

- 创建一个 **SpringBoot 2.0.3 RELEASE** 工程并添加依赖

```
    <dependencies>
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-server</artifactId>
            <version>2.0.1</version>
        </dependency>

        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-server-ui</artifactId>
            <version>2.0.1</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
```

- **应用主类添加注解**

```
@SpringBootApplication
@EnableAdminServer
public class SbaServer20Application {

    public static void main(String[] args) {
        SpringApplication.run(SbaServer20Application.class, args);
    }
}
```

- **启动 Spring Boot Admin Server**

浏览器打开 `localhost:8080`，就可以看到小清新的页面了

![小清新的页面](https://upload-images.jianshu.io/upload_images/9824247-68c3f8c53585f57d.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

可以看到这个 UI 的变化和 1.5.X 时代的差距还是蛮大的，此时被监控的应用数目还为0。

接下来我们就来创建一个待监控的Spring Boot 2.0示例。

---

---

## 创建 Spring Boot Admin Client

此处我们依然创建一个 Spring Boot 2.0.3.RELEASE 的应用，然后加入到Spring Boot Admin之中进行监控

- **pom.xml中添加依赖**

```
    <dependencies>
        <dependency>
            <groupId>de.codecentric</groupId>
            <artifactId>spring-boot-admin-starter-client</artifactId>
            <version>2.0.1</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>

```

- 编辑配置文件

```
server.port=8081
spring.application.name=Spring Boot Client
spring.boot.admin.client.url=http://localhost:8080
management.endpoints.web.exposure.include=*
```

- 启动 Spring Boot Admin Client 应用

此时 Spring Boot Admin的页面上应用上线的消息推送过来了：

![应用上线推送](https://upload-images.jianshu.io/upload_images/9824247-d648d63311cb07e2.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

---

## 实际实验

被监控应用上线之后，我们进入 Spring Boot Admin页面鼓捣看看

- **Wallboard 有点小清新**

![Wallboard](https://upload-images.jianshu.io/upload_images/9824247-dca7a27cd7e4c724.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Applications 概览**

![Applications概览](https://upload-images.jianshu.io/upload_images/9824247-d38bd79f789b9e2b.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Applications上线日志一目了然**

![Applications上线日志一目了然](https://upload-images.jianshu.io/upload_images/9824247-ad6f0153b8bfcf55.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Applications Details**

![Applications Details](https://upload-images.jianshu.io/upload_images/9824247-e84d8fa344dc3e7a.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Metrics**

![Metrics](https://upload-images.jianshu.io/upload_images/9824247-6f668e0ce9188759.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Environment**

![Environment](https://upload-images.jianshu.io/upload_images/9824247-a62c780ed018b7b7.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **JMX**

![JMX](https://upload-images.jianshu.io/upload_images/9824247-576ddcbdc84733a3.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Threads**

![Threads](https://upload-images.jianshu.io/upload_images/9824247-2c823eeb06943cd8.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- **Http Traces**

![Http Traces](https://upload-images.jianshu.io/upload_images/9824247-1e9858abf09d4bc3.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

---

## 后记


> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---
