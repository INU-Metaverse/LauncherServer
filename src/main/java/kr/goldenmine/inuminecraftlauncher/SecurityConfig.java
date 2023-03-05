package kr.goldenmine.inuminecraftlauncher;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/account").permitAll()
                .antMatchers("/auth").permitAll()
                .antMatchers("/html").permitAll()
                .antMatchers("/statistics").hasIpAddress("localhost")
                .antMatchers("/file/upload").hasIpAddress("localhost")
                .antMatchers("/file/download").permitAll()
//                .antMatchers("/file/uploadFile").hasIpAddress("localhost")
                .and()
                .csrf().disable()
                .cors().disable();

        return http.build();
    }

}