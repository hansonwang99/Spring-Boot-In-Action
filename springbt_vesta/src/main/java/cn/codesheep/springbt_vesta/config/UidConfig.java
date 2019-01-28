package cn.codesheep.springbt_vesta.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource( locations = { "classpath:ext/vesta/vesta-rest-main.xml" } )
public class UidConfig {

}
