package br.com.example.jadson.monitoringdemo.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.Charset;
import java.util.Base64;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests().requestMatchers(EndpointRequest.toAnyEndpoint())
            .authenticated()
            .and()
            .httpBasic();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Generate the basic authentication header
     * @param args
     */
    public static void main(String[] args) {

        // $2a$10$KiJV109h9BQtFxwK.928ke9QKxRSuRwzHoCCsV4VceWeIXU8BU9Oa
        System.out.println(new BCryptPasswordEncoder().encode("prometheus"));

        //Basic cHJvbWV0aGV1czpwcm9tZXRoZXVz
        String auth = "prometheus" + ":" + "prometheus";
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(Charset.forName("US-ASCII")) );

        String authHeader = "Basic " + new String( encodedAuth );
        System.out.println("Authorization "+authHeader);

    }



}
