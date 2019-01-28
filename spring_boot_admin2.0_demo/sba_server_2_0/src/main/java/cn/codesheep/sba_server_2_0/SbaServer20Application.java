package cn.codesheep.sba_server_2_0;

import de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration;
import de.codecentric.boot.admin.server.config.AdminServerProperties;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@SpringBootApplication
@EnableAdminServer
public class SbaServer20Application {

    public static void main(String[] args) {
        SpringApplication.run(SbaServer20Application.class, args);
    }

    @Profile("insecure")
    @Configuration
    public static class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter{

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests().anyRequest().permitAll().and().csrf().disable();
        }
    }

    @Profile("secure")
    @Configuration
    public static class SecuritySecureConfig extends WebSecurityConfigurerAdapter{

        private String adminContextPath;

        public SecuritySecureConfig( AdminServerProperties adminServerProperties ) {
            this.adminContextPath = adminServerProperties.getContextPath();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
            successHandler.setTargetUrlParameter("redirectTo");

            http.authorizeRequests().antMatchers(adminContextPath+"/assets/**").permitAll()
                    .antMatchers(adminContextPath+"/login").permitAll().anyRequest().authenticated().and().formLogin()
                    .loginPage(adminContextPath+"/login").successHandler(successHandler).and().logout()
                    .logoutUrl(adminContextPath+"/logout").and().httpBasic().and().csrf().disable();
        }
    }
}
