package cn.codesheep.springbt_mybatis_sqlserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.codesheep.springbt_mybatis_sqlserver")
public class SpringbtMybatisSqlserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbtMybatisSqlserverApplication.class, args);
    }
}
