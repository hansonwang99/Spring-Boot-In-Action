# 《基于Spring Security和 JWT的权限系统设计》

---

![Profile](https://upload-images.jianshu.io/upload_images/9824247-a96eacce43460601.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

## 写在前面

- **关于 Spring Security**
Web系统的认证和权限模块也算是一个系统的基础设施了，几乎任何的互联网服务都会涉及到这方面的要求。在Java EE领域，成熟的安全框架解决方案一般有 Apache Shiro、Spring Security等两种技术选型。Apache Shiro简单易用也算是一大优势，但其功能还是远不如 Spring Security强大。Spring Security可以为 Spring 应用提供声明式的安全访问控制，起通过提供一系列可以在 Spring应用上下文中可配置的Bean，并利用 Spring IoC和 AOP等功能特性来为应用系统提供声明式的安全访问控制功能，减少了诸多重复工作。

- **关于JWT**
JSON Web Token (JWT)，是在网络应用间传递信息的一种基于 JSON的开放标准（(RFC 7519)，用于作为JSON对象在不同系统之间进行安全地信息传输。主要使用场景一般是用来在 身份提供者和服务提供者间传递被认证的用户身份信息。关于JWT的科普，可以看看阮一峰老师的《JSON Web Token 入门教程》。

本文则结合 Spring Security和 JWT两大利器来打造一个简易的权限系统。

本文实验环境如下：
- Spring Boot版本：`2.0.6.RELEASE`
- IDE：`IntelliJ IDEA 2018.2.4`

>另外本文实验代码置于文尾，需要自取。

> 可 **长按** 或 **扫描** 下面的 **小心心** 来订阅作者公众号 **CodeSheep**，获取更多 **务实、能看懂、可复现的** 原创文 ↓↓↓

![CodeSheep · 程序羊](https://user-gold-cdn.xitu.io/2018/8/9/1651c0ef66e4923f?w=270&h=270&f=png&s=102007)



---

## 设计用户和角色

本文实验为了简化考虑，准备做如下设计：

- 设计一个最简角色表`role`，包括`角色ID`和`角色名称`

![角色表](https://upload-images.jianshu.io/upload_images/9824247-d24b696799ced559.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 设计一个最简用户表`user`，包括`用户ID`，`用户名`，`密码`

![用户表](https://upload-images.jianshu.io/upload_images/9824247-a56eb9ed64bee618.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 再设计一个用户和角色一对多的关联表`user_roles`
一个用户可以拥有多个角色
![用户-角色对应表](https://upload-images.jianshu.io/upload_images/9824247-d14f07e1ae45e6fc.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

## 创建 Spring Security和 JWT加持的 Web工程

- **`pom.xml` 中引入 Spring Security和 JWT所必需的依赖**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>

<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt</artifactId>
	<version>0.9.0</version>
</dependency>
```

- **项目配置文件中加入数据库和 JPA等需要的配置**

```
server.port=9991

spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://121.196.XXX.XXX:3306/spring_security_jwt?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=root
spring.datasource.password=XXXXXX

logging.level.org.springframework.security=info

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jackson.serialization.indent_output=true
```

- **创建用户、角色实体**

**用户实体 User**：

```java
/**
 * @ www.codesheep.cn
 * 20190312
 */
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private String password;

    @ManyToMany(cascade = {CascadeType.REFRESH},fetch = FetchType.EAGER)
    private List<Role> roles;

    ...

    // 下面为实现UserDetails而需要的重写方法！
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add( new SimpleGrantedAuthority( role.getName() ) );
        }
        return authorities;
    }
    
    ...
}
```

此处所创建的 User类继承了 Spring Security的 UserDetails接口，从而成为了一个符合 Security安全的用户，即通过继承 UserDetails，即可实现 Security中相关的安全功能。

**角色实体 Role：**

```java
/**
 * @ www.codesheep.cn
 * 20190312
 */
@Entity
public class Role {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
  
    ... // 省略 getter和 setter
}
```


- **创建JWT工具类**

主要用于对 JWT Token进行各项操作，比如生成Token、验证Token、刷新Token等

```java
/**
 * @ www.codesheep.cn
 * 20190312
 */
@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -5625635588908941275L;

    private static final String CLAIM_KEY_USERNAME = "sub";
    private static final String CLAIM_KEY_CREATED = "created";

    public String generateToken(UserDetails userDetails) {
        ...
    }

    String generateToken(Map<String, Object> claims) {
        ...
    }

    public String refreshToken(String token) {
        ...
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        ...
    }

    ... // 省略部分工具函数
}
```

- **创建Token过滤器，用于每次外部对接口请求时的Token处理**

```java
/**
 * @ www.codesheep.cn
 * 20190312
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal ( HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader( Const.HEADER_STRING );
        if (authHeader != null && authHeader.startsWith( Const.TOKEN_PREFIX )) {
            final String authToken = authHeader.substring( Const.TOKEN_PREFIX.length() );
            String username = jwtTokenUtil.getUsernameFromToken(authToken);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                	if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(
                                request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
            }
        }
        chain.doFilter(request, response);
    }
}
```

- **Service业务编写**

主要包括用户登录和注册两个主要的业务

```java
public interface AuthService {
    User register( User userToAdd );
    String login( String username, String password );
}
```

```java
/**
 * @ www.codesheep.cn
 * 20190312
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    // 登录
    @Override
    public String login( String username, String password ) {
        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken( username, password );
        final Authentication authentication = authenticationManager.authenticate(upToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetails userDetails = userDetailsService.loadUserByUsername( username );
        final String token = jwtTokenUtil.generateToken(userDetails);
        return token;
    }

    // 注册
    @Override
    public User register( User userToAdd ) {
        final String username = userToAdd.getUsername();
        if( userRepository.findByUsername(username)!=null ) {
            return null;
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        final String rawPassword = userToAdd.getPassword();
        userToAdd.setPassword( encoder.encode(rawPassword) );
        return userRepository.save(userToAdd);
    }
}
```

- **Spring Security配置类编写（非常重要）**

这是一个高度综合的配置类，主要是通过重写 `WebSecurityConfigurerAdapter` 的部分 `configure`配置，来实现用户自定义的部分。

```java
/**
 * @ www.codesheep.cn
 * 20190312
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    @Bean
    public JwtTokenFilter authenticationTokenFilterBean() throws Exception {
        return new JwtTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure( AuthenticationManagerBuilder auth ) throws Exception {
        auth.userDetailsService( userService ).passwordEncoder( new BCryptPasswordEncoder() );
    }

    @Override
    protected void configure( HttpSecurity httpSecurity ) throws Exception {
        httpSecurity.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // OPTIONS请求全部放行
                .antMatchers(HttpMethod.POST, "/authentication/**").permitAll()  //登录和注册的接口放行，其他接口全部接受验证
                .antMatchers(HttpMethod.POST).authenticated()
                .antMatchers(HttpMethod.PUT).authenticated()
                .antMatchers(HttpMethod.DELETE).authenticated()
                .antMatchers(HttpMethod.GET).authenticated();

        // 使用前文自定义的 Token过滤器
        httpSecurity
                .addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        httpSecurity.headers().cacheControl();
    }
}
```

- **编写测试 Controller**

登录和注册的 Controller：
```
/**
 * @ www.codesheep.cn
 * 20190312
 */
@RestController
public class JwtAuthController {
    @Autowired
    private AuthService authService;

    // 登录
    @RequestMapping(value = "/authentication/login", method = RequestMethod.POST)
    public String createToken( String username,String password ) throws AuthenticationException {
        return authService.login( username, password ); // 登录成功会返回JWT Token给用户
    }

    // 注册
    @RequestMapping(value = "/authentication/register", method = RequestMethod.POST)
    public User register( @RequestBody User addedUser ) throws AuthenticationException {
        return authService.register(addedUser);
    }
}
```

再编写一个测试权限的 Controller：

```
/**
 * @ www.codesheep.cn
 * 20190312
 */
@RestController
public class TestController {

    // 测试普通权限
    @PreAuthorize("hasAuthority('ROLE_NORMAL')")
    @RequestMapping( value="/normal/test", method = RequestMethod.GET )
    public String test1() {
        return "ROLE_NORMAL /normal/test接口调用成功！";
    }

    // 测试管理员权限
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @RequestMapping( value = "/admin/test", method = RequestMethod.GET )
    public String test2() {
        return "ROLE_ADMIN /admin/test接口调用成功！";
    }
}
```

这里给出两个测试接口用于测试权限相关问题，其中接口 `/normal/test`需要用户具备普通角色（`ROLE_NORMAL`）即可访问，而接口`/admin/test`则需要用户具备管理员角色（`ROLE_ADMIN`）才可以访问。

接下来启动工程，实验测试看看效果

 ---

## 实验验证

- 在文章开头我们即在用户表 `user`中插入了一条用户名为 `codesheep`的记录，并在用户-角色表 `user_roles`中给用户 `codesheep`分配了普通角色（`ROLE_NORMAL`）和管理员角色（`ROLE_ADMIN`）

- 接下来进行用户登录，并获得后台向用户颁发的JWT Token

![用户登录并获得JWT Token](https://upload-images.jianshu.io/upload_images/9824247-a91abd5685e10920.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

- 接下来访问权限测试接口

不带 Token直接访问需要普通角色（`ROLE_NORMAL`）的接口 `/normal/test`会直接提示访问不通：

![不带token访问是不通的](https://upload-images.jianshu.io/upload_images/9824247-18af842fd4faeb45.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

而带 Token访问需要普通角色（`ROLE_NORMAL`）的接口 `/normal/test`才会调用成功：

![带token访问OK](https://upload-images.jianshu.io/upload_images/9824247-22a1212aac6ab270.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


同理由于目前用户具备管理员角色，因此访问需要管理员角色（`ROLE_ADMIN`）的接口 `/admin/test`也能成功：

![访问需要管理员角色的接口OK](https://upload-images.jianshu.io/upload_images/9824247-a56685c151d809cf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

接下里我们从用户-角色表里将用户`codesheep`的管理员权限删除掉，再访问接口 `/admin/test`，会发现由于没有权限，访问被拒绝了：

![由于权限不够而被拒绝](https://upload-images.jianshu.io/upload_images/9824247-4e284e1e759d5df8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

经过一系列的实验过程，也达到了我们的预期！

---

## 写在最后

本文涉及的东西还是蛮多的，最后我们也将本文的实验源码放在 [Github上](https://github.com/hansonwang99/Spring-Boot-In-Action/tree/master/springbt_security_jwt)，需要的可以自取：[源码下载地址](https://github.com/hansonwang99/Spring-Boot-In-Action/tree/master/springbt_security_jwt)

> 由于能力有限，若有错误或者不当之处，还请大家批评指正，一起学习交流！

- My Personal Blog：[CodeSheep  程序羊](https://www.codesheep.cn/)

---

---




