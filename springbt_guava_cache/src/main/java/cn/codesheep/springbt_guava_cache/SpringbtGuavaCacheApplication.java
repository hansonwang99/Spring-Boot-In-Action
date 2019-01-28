package cn.codesheep.springbt_guava_cache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@MapperScan("cn.codesheep.springbt_guava_cache")
@EnableCaching
public class SpringbtGuavaCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbtGuavaCacheApplication.class, args);
    }

}

