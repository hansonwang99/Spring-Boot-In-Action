package cn.codesheep.testidspringbootstarter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"org.zjs.cms","com.baidu.fsg.uid.worker.dao"})
public class TestIdSpringBootStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestIdSpringBootStarterApplication.class, args);
    }

}
