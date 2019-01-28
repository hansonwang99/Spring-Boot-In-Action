# 《Spring Boot工程集成全局唯一ID生成器 UidGenerator》


![Profile](http://upload-images.jianshu.io/upload_images/9824247-9872ce92a8fc92b9.jpg)

> 本文共 823字，阅读大约需要 3分钟 ！

---

## 概述

流水号生成器（全局唯一 ID生成器）是服务化系统的基础设施，其在保障系统的正确运行和高可用方面发挥着重要作用。而关于流水号生成算法首屈一指的当属 [Snowflake](https://github.com/twitter/snowflake)雪花算法，然而 Snowflake本身很难在现实项目中直接使用，因此实际应用时需要一种可落地的方案。

UidGenerator 由百度开发，是Java实现的, 基于 Snowflake算法的唯一ID生成器。UidGenerator以组件形式工作在应用项目中, 支持自定义workerId位数和初始化策略, 从而适用于 docker等虚拟化环境下实例自动重启、漂移等场景。 

本文就在项目中来集成 UidGenerator这一工程来作为项目的全局唯一 ID生成器。

>**注：** 本文首发于  [**My Personal Blog**](http://www.codesheep.cn)，欢迎光临 [**小站**](http://www.codesheep.cn)

本文内容脑图如下：

![本文内容脑图](https://upload-images.jianshu.io/upload_images/9824247-1fcd68321a12a4dc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


---

## 基础工程创建

只需创建一个 Multi-Moudule的 Maven项目即可，然后我们集成进两个 Module：

- **uid-generator**：[源码在此](https://github.com/baidu/uid-generator)
- **uid-consumer**：消费者（ 使用uid-generator产生全局唯一的流水号 ）

`uid-generator`模块我就不多说了，源码拿过来即可，无需任何改动；而关于 `uid-consumer`模块，先在 pom.xml中添加相关依赖如下：

```
    <dependencies>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>1.3.2</version>
        </dependency>

        <!--for Mysql-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
            <version>8.0.12</version>
        </dependency>

        <!-- druid -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>1.1.9</version>
        </dependency>

        <!--必须放在最后-->
        <dependency>
            <groupId>cn.codesheep</groupId>
            <artifactId>uid-generator</artifactId>
            <version>1.0</version>
        </dependency>

    </dependencies>
```

然后在 application.properties配置文件中添加一些配置（主要是 MySQL和 MyBatis配置）

```
server.port=9999

spring.datasource.url=jdbc:mysql://xxx.xxx.xxx.xxx:3306/xxx?useUnicode=true&characterEncoding=utf-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=xxx
spring.datasource.driver-class-name=com.mysql.jdbc.Driver

mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true
```

完成之后工程缩影如下图所示：

![工程缩影](https://upload-images.jianshu.io/upload_images/9824247-4c21aa48a7bea0f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

下面我们来一步步集成 `UidGenerator`的源码。

---

## 数据库建表

首先去 MySQL数据库中建一个名为 `WORKER_NODE`的数据表，其 sql如下：

```sql
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

---

## Spring详细配置

- **CachedUidGenerator 配置**

> UidGenerator 有两个具体的实现类，分别是 `DefaultUidGenerator` 和 `CachedUidGenerator`，不过官方也推荐了对于性能比较敏感的项目应使用后者，因此本文也使用 `CachedUidGenerator`，而对于 `DefaultUidGenerator`不做过多阐述。

我们引入 UidGenerator源码中的 `cached-uid-spring.xml`文件，里面都是默认配置，我目前没有做任何修改

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

	<!-- UID generator -->
	<bean id="disposableWorkerIdAssigner" class="com.baidu.fsg.uid.worker.DisposableWorkerIdAssigner" />

	<bean id="cachedUidGenerator" class="com.baidu.fsg.uid.impl.CachedUidGenerator">
		<property name="workerIdAssigner" ref="disposableWorkerIdAssigner" />

		<!-- 以下为可选配置, 如未指定将采用默认值 -->
		<!-- RingBuffer size扩容参数, 可提高UID生成的吞吐量. --> 
		<!-- 默认:3， 原bufferSize=8192, 扩容后bufferSize= 8192 << 3 = 65536 -->
		<!--<property name="boostPower" value="3"></property>--> 
		
		<!-- 指定何时向RingBuffer中填充UID, 取值为百分比(0, 100), 默认为50 -->
		<!-- 举例: bufferSize=1024, paddingFactor=50 -> threshold=1024 * 50 / 100 = 512. -->
		<!-- 当环上可用UID数量 < 512时, 将自动对RingBuffer进行填充补全 -->
		<!--<property name="paddingFactor" value="50"></property>--> 
		
		<!-- 另外一种RingBuffer填充时机, 在Schedule线程中, 周期性检查填充 -->
		<!-- 默认:不配置此项, 即不实用Schedule线程. 如需使用, 请指定Schedule线程时间间隔, 单位:秒 -->
		<!--<property name="scheduleInterval" value="60"></property>--> 
		
		<!-- 拒绝策略: 当环已满, 无法继续填充时 -->
		<!-- 默认无需指定, 将丢弃Put操作, 仅日志记录. 如有特殊需求, 请实现RejectedPutBufferHandler接口(支持Lambda表达式) -->
		<!--<property name="rejectedPutBufferHandler" ref="XxxxYourPutRejectPolicy"></property>--> 
		
		<!-- 拒绝策略: 当环已空, 无法继续获取时 -->
		<!-- 默认无需指定, 将记录日志, 并抛出UidGenerateException异常. 如有特殊需求, 请实现RejectedTakeBufferHandler接口(支持Lambda表达式) -->
		<!--<property name="rejectedPutBufferHandler" ref="XxxxYourPutRejectPolicy"></property>--> 
		
	</bean>

</beans>

```

- **Mybatis Mapper XML 配置**

即原样引入 UidGenerator源码中关于工作节点（Worker Node）操作的 mapper xml 文件：`WORKER_NODE.xml`，其内容如下：

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.baidu.fsg.uid.worker.dao.WorkerNodeDAO">
	<resultMap id="workerNodeRes"
			   type="com.baidu.fsg.uid.worker.entity.WorkerNodeEntity">
		<id column="ID" jdbcType="BIGINT" property="id" />
		<result column="HOST_NAME" jdbcType="VARCHAR" property="hostName" />
		<result column="PORT" jdbcType="VARCHAR" property="port" />
		<result column="TYPE" jdbcType="INTEGER" property="type" />
		<result column="LAUNCH_DATE" jdbcType="DATE" property="launchDate" />
		<result column="MODIFIED" jdbcType="TIMESTAMP" property="modified" />
		<result column="CREATED" jdbcType="TIMESTAMP" property="created" />
	</resultMap>

	<insert id="addWorkerNode" useGeneratedKeys="true" keyProperty="id"
		parameterType="com.baidu.fsg.uid.worker.entity.WorkerNodeEntity">
		INSERT INTO WORKER_NODE
		(HOST_NAME,
		PORT,
		TYPE,
		LAUNCH_DATE,
		MODIFIED,
		CREATED)
		VALUES (
		#{hostName},
		#{port},
		#{type},
		#{launchDate},
		NOW(),
		NOW())
	</insert>

	<select id="getWorkerNodeByHostPort" resultMap="workerNodeRes">
		SELECT
		ID,
		HOST_NAME,
		PORT,
		TYPE,
		LAUNCH_DATE,
		MODIFIED,
		CREATED
		FROM
		WORKER_NODE
		WHERE
		HOST_NAME = #{host} AND PORT = #{port}
	</select>
</mapper>
```

---

## 编写业务代码

- **config 类创建与配置**

新建 `UidConfig`类，为我们引入上文的 `cached-uid-spring.xml`配置

```
@Configuration
@ImportResource(locations = { "classpath:uid/cached-uid-spring.xml" })
public class UidConfig {
}
```

- **service 类创建与配置**

新建 `UidGenService`，引入 UidGenerator 生成 UID的业务接口

```
@Service
public class UidGenService {

    @Resource
    private UidGenerator uidGenerator;

    public long getUid() {
        return uidGenerator.getUID();
    }
}
```

- **controller 创建与配置**

新建 `UidTestController`，目的是方便我们用浏览器测试接口并观察效果：

```
@RestController
public class UidTestController {

    @Autowired
    private UidGenService uidGenService;

    @GetMapping("/testuid")
    public String test() {
        return String.valueOf( uidGenService.getUid() );
    }
}
```

---

## 实验测试

我们每启动一次 Spring Boot工程，其即会自动去 MySQL数据的 `WORKER_NODE`表中插入一行关于工作节点的记录，类似下图所示：

![WORKER_NODE 表内容](https://upload-images.jianshu.io/upload_images/9824247-83f748a31001b81e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

接下来我们浏览器访问：http://localhost:9999/testuid

![浏览器测试](https://upload-images.jianshu.io/upload_images/9824247-1ef8c1ca76ec9fa3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

OK，全局唯一流水号ID已经成功生成并返回！

---

## 后记

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---
