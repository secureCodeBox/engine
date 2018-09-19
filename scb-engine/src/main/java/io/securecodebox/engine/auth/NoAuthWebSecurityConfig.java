package io.securecodebox.engine.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "securecodebox.rest.auth", havingValue = "none")
public class NoAuthWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.authorizeRequests()
            .anyRequest().permitAll()
            .and().httpBasic().disable();
        http.csrf().disable();
    }
}
