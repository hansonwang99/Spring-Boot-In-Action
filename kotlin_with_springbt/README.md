
# 《Kotlin + Spring Boot联合编程》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---

---

## 概述

Kotlin是一门最近比较流行的静态类型编程语言，而且和Groovy、Scala一样同属Java系。Kotlin具有的很多静态语言特性诸如：类型判断、多范式、扩展函数、模式匹配等等让我无法只作为一个吃瓜群众了，所以稍微花了点时间了解了一下该语言。

本文主要介绍一下如何使用Kotlin结合SpringBt开发一个带有数据库交互的REST风格基本程序

>**注：** 本文原载于  [**My Personal Blog：**](http://www.codesheep.cn)， [**CodeSheep · 程序羊**](http://www.codesheep.cn) ！

---

## 实验环境

- JDK不用说了，Kotlin毕竟是运行在JVM环境下的语言，所以JDK必须，我这里用的JDK1.8
- 数据库：MySQL
- 数据库访问组件：Spring data jpa
- J2EE框架：SpringBt 1.5.2.RELEASE
- 构建工具：Gradle

---

## 工程创建

没啥好说的，我这里创建的是基于Gradle的Kotlin工程：

![基于Gradle的Kotlin工程](http://upload-images.jianshu.io/upload_images/9824247-baac893f0e5a1191.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

创建完成后的基本工程样式和SpringBt的工程几乎没任何区别，给张图示意一下好了：

![工程基本样式](http://upload-images.jianshu.io/upload_images/9824247-3c96778d62f47999.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

好啦，接下来我们就来写代码完善这个工程即可

---

## 完善build.gradle配置

我们需要在build.gradle中引入SpringBt依赖，除此之外还要引入一些特定的插件方便我们向写Java代码一样来写Kotlin程序！

在dependencies中加入如下依赖：
```
dependencies {
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre8:$kotlin_version"
    testCompile group: 'junit', name: 'junit', version: '4.12'
    compile("org.springframework.boot:spring-boot-starter-web")
    testCompile("org.springframework.boot:spring-boot-starter-test")
    compile("org.springframework.boot:spring-boot-starter-data-jpa")
    compile('mysql:mysql-connector-java:5.1.13')
}
```

这样SpringBt相关的依赖就配置上了！

接下来我们配置两个非常关键的插件依赖：
- 无参（no-arg）插件
- 全开放（allopen）插件

我们先配上，等下解释：
```
buildscript {
    ext.kotlin_version = '1.1.1'
    ext.springboot_version = '1.5.2.RELEASE'

    repositories {
        mavenCentral()
    }
    dependencies {
        // Kotlin Gradle插件
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
        // SpringBoot Gradle插件
        classpath("org.springframework.boot:spring-boot-gradle-plugin:$springboot_version")
        // Kotlin整合SpringBoot的默认无参构造函数，默认把所有的类设置open类插件
        classpath("org.jetbrains.kotlin:kotlin-noarg:$kotlin_version") // 无参插件
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version") // 全开放插件
    }
}
```

其中（以下解释源自《Kotlin极简教程》）：
- org.jetbrains.kotlin:kotlin-noarg是无参（no-arg）编译器插件，它为具有特定注解的类生成一个额外的零参数构造函数。 这个生成的构造函数是合成的，因此不能从 Java 或 Kotlin 中直接调用，但可以使用反射调用。 这样我们就可以使用 Java Persistence API（JPA）实例化 data 类。

- org.jetbrains.kotlin:kotlin-allopen 是全开放编译器插件。我们使用Kotlin 调用Java的Spring AOP框架和库，需要类为 open（可被继承实现），而Kotlin 类和函数都是默认 final 的，这样我们需要为每个类和函数前面加上open修饰符。这样的代码写起来很费事。还好，我们有all-open 编译器插件。它会适配 Kotlin 以满足这些框架的需求，并使用指定的注解标注类而其成员无需显式使用 open 关键字打开。 例如，当我们使用 Spring 时，就不需要打开所有的类，跟我们在Java中写代码一样，只需要用相应的注解标注即可，如 @Configuration 或 @Service。


讲白了，引入这两个特定的插件的目的就是为了方便我们向写SpringBt代码一样来写Kotlin程序！

---

## 配置application.properties

这里面主要是跟Mysql数据库相关的一些配置：

```
spring.datasource.url = jdbc:mysql://localhost:3306/easykotlin
spring.datasource.username = root
spring.datasource.password = 你的Mysql密码
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.jpa.database = MYSQL
spring.datasource.testWhileIdle = true
spring.datasource.validationQuery = SELECT 1
spring.jpa.show-sql = true
spring.jpa.hibernate.ddl-auto = update
spring.jpa.hibernate.naming-strategy = org.hibernate.cfg.ImprovedNamingStrategy
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQL5Dialect

server.port=7000
```

---

## 正式编写工程

我们需要去数据库中查询东西，所以二话不说，写个访问数据库的标准代码层：
- controller
- entity
- repository
- service

![整体代码框架](http://upload-images.jianshu.io/upload_images/9824247-e8cd8481ead01eeb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

各部分代码如下：

- People.kt

```
@Entity
class People(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        val id: Long?,
        val firstName: String?,
        val lastName: String?,
        val gender: String?,
        val age: Int?,
        val gmtCreated: Date,
        val gmtModified: Date
) {
    override fun toString(): String {
        return "People(id=$id, firstName='$firstName', lastName='$lastName', gender='$gender', age=$age, gmtCreated=$gmtCreated, gmtModified=$gmtModified)"
    }
}
```

- PeopleRepository.kt

```
interface PeopleRepository : CrudRepository<People, Long> {
    fun findByLastName(lastName: String): List<People>?
}
```

- PeopleService.kt

```
@Service
class PeopleService : PeopleRepository {

    @Autowired
    val peopleRepository: PeopleRepository? = null

    override fun findByLastName(lastName: String): List<People>? {
        return peopleRepository?.findByLastName(lastName)
    }

    override fun <S : People?> save(entity: S): S? {
        return peopleRepository?.save(entity)
    }

    override fun <S : People?> save(entities: MutableIterable<S>?): MutableIterable<S>? {
        return peopleRepository?.save(entities)
    }

    override fun delete(entities: MutableIterable<People>?) {
    }

    override fun delete(entity: People?) {
    }

    override fun delete(id: Long?) {
    }

    override fun findAll(ids: MutableIterable<Long>?): MutableIterable<People>? {
        return peopleRepository?.findAll(ids)
    }

    override fun findAll(): MutableIterable<People>? {
        return peopleRepository?.findAll()
    }

    override fun exists(id: Long?): Boolean {
        return peopleRepository?.exists(id)!!
    }

    override fun count(): Long {
        return peopleRepository?.count()!!
    }

    override fun findOne(id: Long?): People? {
        return peopleRepository?.findOne(id)
    }

    override fun deleteAll() {
    }
}
```

- PeopleController.kt

```
@Controller
class PeopleController {
    @Autowired
    val peopleService: PeopleService? = null

    @GetMapping(value = "/hello")
    @ResponseBody
    fun hello(@RequestParam(value = "lastName") lastName: String): Any {
        val peoples = peopleService?.findByLastName(lastName)
        val map = HashMap<Any, Any>()
        map.put("hello", peoples!!)
        return map
    }
}
```

可见有了无参、全开放组件加持后，写代码和写Java的代码基本没区别了

---

## 实际实验

首先需要去Mysql中建好数据库，并插入一些数据：

![数据库预览](http://upload-images.jianshu.io/upload_images/9824247-1f59d8e3ae409028.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

然后启动工程，访问：
http://localhost:7000/hello?lastName=wang

可以看到数据成功被取回：

![成功获取到数据](http://upload-images.jianshu.io/upload_images/9824247-6bc738ccd94b1e88.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

## 参考文献

《Kotlin极简教程》

--- 

## 后记

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---
