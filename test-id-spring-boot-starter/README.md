![喀拉喀托火山，印度尼西亚 (© Martin Rietze/Alamy)](https://raw.githubusercontent.com/hansonwang99/pic/master/id-springbt-starter/profile.JPG)

---
## 概述

ID号生成器（或：全局唯一ID生成器）是服务端系统的基础设施，而且ID号这个东西基本搞后端开发的程序员天天都要接触。而关于ID生成的算法现在业界首屈一指的当属`Snowflake`雪花算法。

`UidGenerator`正是百度开源的一款基于`Snowflake`雪花算法实现的高性能唯一ID生成器。在本号前文中已经详细使用过`UidGenerator`，但使用过程还是比较繁杂，还需要自己去引`UidGenerator`组件的源码，感觉有点不方便。为此本文基于`UidGenerator`，再来封装一套更利于`Spring Boot`项目使用的`ID`号生成组件，命名为`id-spring-boot-starter`，一看名字就知道是开箱即用的。

---
## 用法

- **导入SQL脚本**

```
DROP TABLE IF EXISTS WORKER_NODE;
CREATE TABLE WORKER_NODE
(
ID BIGINT NOT NULL AUTO_INCREMENT COMMENT 'auto increment id',
HOST_NAME VARCHAR(64) NOT NULL COMMENT 'host name',
PORT VARCHAR(64) NOT NULL COMMENT 'port',
TYPE INT NOT NULL COMMENT 'node type: ACTUAL or CONTAINER',
LAUNCH_DATE DATE NOT NULL COMMENT 'launch date',
MODIFIED TIMESTAMP NOT NULL COMMENT 'modified time',
CREATED TIMESTAMP NOT NULL COMMENT 'created time',
PRIMARY KEY(ID)
)
 COMMENT='DB WorkerID Assigner for UID Generator',ENGINE = INNODB;
```

这一步肯定是省不掉，毕竟`UidGenerator`需要数据库支持

- **pom中加入依赖**

```
<dependency>
	<groupId>cn.codesheep</groupId>
	<artifactId>id-spring-boot-starter</artifactId>
	<version>1.0.0</version>
</dependency>
```

- **配置数据库连接**

```
url: jdbc:mysql://xxx.xxx.xxx.xxx:3306/demo?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useOldAliasMetadataBehavior=true&connectionCollation=utf8mb4_unicode_ci&rewriteBatchedStatements=true&allowMultiQueries=true
username: xxxxxx
password: xxxxxx
```

还是因为`UidGenerator`需要数据库支持

- **修改Spring Boot主类**

Spring Boot应用主类上加上`mybatis`注解即可：

```
@MapperScan({"com.baidu.fsg.uid.worker.dao"})
```

- **代码使用方式**

```
@RestController
public class TestController {

  @Autowired
  private UidGenService uidGenService;

  @GetMapping("/uid")
  public String genUid() {
    return String.valueOf("本次生成的唯一ID号为："+uidGenService.getUid());
  }
}
```

怎么样，用法超级简单吧：
1. 首先用 `Autowired`的方式引入`UidGenService`类；
2. 直接调用`UidGenService`类的`getUid()`方法即可获得一个`Long`型的 `ID`号

- **运行效果**

![运行效果](https://raw.githubusercontent.com/hansonwang99/pic/master/id-springbt-starter/result.JPG)

`demo`源码在此，需要请自提：
- https://github.com/hansonwang99/Spring-Boot-In-Action/tree/master/test-id-spring-boot-starter


---
## id-spring-boot-starter源码下载

需要组件`id-spring-boot-starter`源码进行自定制的，可以去`github`上自行下载源代码，地址在此：
- https://github.com/hansonwang99/Spring-Boot-In-Action/tree/master/id-spring-boot-starter

---
## 几个注意点：
- 由于`UidGenerator`需要数据库支持，因此使用前一定要导数据表，并且要配置`MapperScan`
- 需要高度定制`UidGenerator`组件详情的，可以自行修改`id-spring-boot-starter`内部的`cached-uid-spring.xml`文件，然后重新打`jar`包
- 由于`ID`号生成一般属于系统基础服务，因此可以独立成一个微服务，供其他微服务调用

---

- 个人博客：https://www.codesheep.cn
- GitHub：https://github.com/hansonwang99

另外所有内容都在我的个人微信公众号 **CodeSheep** 最先推出，大家可以关注一下，可 **长按** 或 **扫描** 下面的 **小心心** 来获取更多 **务实、能看懂、可复现的** 原创文。除此之外 **提问/进群交流** 也可以扫码进行！！！

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

[![Badge](https://img.shields.io/badge/link-996.icu-%23FF4D5B.svg)](https://996.icu/#/en_US)
[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)
[![Slack](https://img.shields.io/badge/slack-996icu-green.svg)](https://join.slack.com/t/996icu/shared_invite/enQtNTg4MjA3MzA1MzgxLWQyYzM5M2IyZmIyMTVjMzU5NTE5MGI5Y2Y2YjgwMmJiMWMxMWMzNGU3NDJmOTdhNmRlYjJlNjk5ZWZhNWIwZGM)
