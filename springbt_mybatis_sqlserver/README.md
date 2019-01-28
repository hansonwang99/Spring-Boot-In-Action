
# 《Spring Boot集成 MyBatis和 SQL Server实践》

---

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)

---


---

## 概 述

Spring Boot工程集成 MyBatis来实现 MySQL访问的示例我们见过很多，而最近用到了微软的 SQL Server数据库，于是本文则给出一个完整的 **Spring Boot + MyBatis + SQL Server** 的工程示例。

>**注：** 本文首发于  [**My Personal Blog：CodeSheep·程序羊**](https://www.codesheep.cn)，欢迎光临 [**小站**](https://www.codesheep.cn)


---
## 工程搭建

- 新建 Spring Boot工程
- `pom.xml` 中添加 MyBatis和 SQL Server相关的依赖

```
<!--for mybatis-->
<dependency>
	<groupId>org.mybatis.spring.boot</groupId>
	<artifactId>mybatis-spring-boot-starter</artifactId>
	<version>1.3.2</version>
</dependency>

<!--for SqlServer-->
<dependency>
	<groupId>com.microsoft.sqlserver</groupId>
	<artifactId>sqljdbc4</artifactId>
	<version>4.0</version>
</dependency>
```

- 配置 `application.properties`

这里同样主要是对于 MyBatis 和 SQL Server连接相关的配置

```
server.port=89

# mybatis 配置
mybatis.type-aliases-package=cn.codesheep.springbt_mybatis_sqlserver.entity
mybatis.mapper-locations=classpath:mapper/*.xml
mybatis.configuration.map-underscore-to-camel-case=true

## -------------------------------------------------

## SqlServer 配置
spring.datasource.url=jdbc:sqlserver://xxxx:1433;databasename=MingLi
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
spring.datasource.username=xxxx
spring.datasource.password=xxxx
```

---

## 建立 SQL Server数据表和实体类

- 首先在 SQL Server数据库中新建数据表 `user_test`作为测试用表

```
DROP TABLE [demo].[user_test]
GO
CREATE TABLE [dbo].[user_test] (
[user_id] int NOT NULL ,
[user_name] varchar(50) NOT NULL ,
[sex] tinyint NOT NULL ,
[created_time] varchar(50) NOT NULL 
)

GO
```

-  然后在我们的工程中对应建立的 `User`实体类

其字段和实际数据表的字段一一对应

```
public class User {

    private Long userId;
    private String userName;
    private Boolean sex;
    private String createdTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getSex() {
        return sex;
    }

    public void setSex(Boolean sex) {
        this.sex = sex;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
}
```

---

## Mybatis Mapper映射配置

- MyBatis映射配置的 XML文件如下：

```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="cn.codesheep.springbt_mybatis_sqlserver.mapper.UserMapper">

    <resultMap id="userMap" type="cn.codesheep.springbt_mybatis_sqlserver.entity.User">
        <id property="userId" column="user_id" javaType="java.lang.Long"></id>
        <result property="userName" column="user_name" javaType="java.lang.String"></result>
        <result property="sex" column="sex" javaType="java.lang.Boolean"></result>
        <result property="createdTime" column="created_time" javaType="java.lang.String"></result>
    </resultMap>

    <select id="getAllUsers" resultMap="userMap">
        select * from user_test
    </select>

    <insert id="addUser" parameterType="cn.codesheep.springbt_mybatis_sqlserver.entity.User">
        insert into user_test ( user_id, user_name, sex, created_time ) values ( #{userId}, #{userName}, #{sex}, #{createdTime} )
    </insert>

    <delete id="deleteUser" parameterType="cn.codesheep.springbt_mybatis_sqlserver.entity.User">
        delete from user_test where user_name = #{userName}
    </delete>

</mapper>
```

- 与此同时，这里也给出对应 XML的 DAO接口

```
public interface UserMapper {
    List<User> getAllUsers();
    int addUser( User user );
    int deleteUser( User user );
}
```

为了试验起见，这里给出了 **增 / 删 / 查** 三个数据库操作动作。


---

## 编写 Service 和测试Controller

- 上面这些准备工作完成之后，接下来编写数据库 CRUD的 Service类

```
@Service
@Primary
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Override
    public int addUser(User user) {
        SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        user.setCreatedTime( form.format(new Date()) );
        return userMapper.addUser( user );
    }

    @Override
    public int deleteUser(User user) {
        return userMapper.deleteUser( user );
    }
}
```

这里的 Service功能同样主要关于数据表的 **增 / 删 / 查** 三个数据库操作动作。

- 对照着上面的Service，我们编写一个对应接口测试的Controller

```
@RestController
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/getAllUser", method = RequestMethod.GET)
    public List<User> getAllUser() {
        return userService.getAllUsers();
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.POST)
    public int addUser( @RequestBody User user ) {
        return userService.addUser( user );
    }

    @RequestMapping(value = "/deleteUser", method = RequestMethod.POST)
    public int deleteUser( @RequestBody User user ) {
        return userService.deleteUser( user );
    }

}
```

---
## 实验测试

- 插入数据

依次用 POSTMAN通过 `Post /addUser`接口插入三条数据：

```
{"userId":1,"userName":"刘能","sex":true}
{"userId":2,"userName":"赵四","sex":false}
{"userId":3,"userName":"王大拿","sex":true}
```

插入完成后去 SQL Server数据库里看一下数据插入情况如下：

![去SQL Server数据库里看一下数据插入情况](https://upload-images.jianshu.io/upload_images/9824247-f8f062d6050a0d5b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 查询数据

调用 `Get /getAllUser`接口，获取刚插入的几条数据

![查询数据](https://upload-images.jianshu.io/upload_images/9824247-9220a0bc2146a30e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 删除数据

调用 `Post /deleteUser `接口，可以通过用户名来删除对应的用户

![image.png](https://upload-images.jianshu.io/upload_images/9824247-3afb1fbd47878acd.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


---

## 后 记
 
> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](http://www.codesheep.cn/)

---
