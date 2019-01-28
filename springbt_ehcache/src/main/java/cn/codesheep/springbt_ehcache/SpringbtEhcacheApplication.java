package cn.codesheep.springbt_ehcache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("cn.codesheep.springbt_ehcache")
@EnableCaching
public class SpringbtEhcacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbtEhcacheApplication.class, args);
    }
}
