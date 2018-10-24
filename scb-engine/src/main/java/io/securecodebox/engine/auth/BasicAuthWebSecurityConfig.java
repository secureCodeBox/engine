package io.securecodebox.engine.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "securecodebox.rest.auth", havingValue = "basic auth")
public class BasicAuthWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String SCB_REST_API_URL = "/box";

    @Autowired
    CamundaAuthenticationProvider camundaAuthenticationProvider;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.antMatcher(SCB_REST_API_URL + "/**").authorizeRequests()
            .anyRequest().authenticated()
            .and().httpBasic();
        http.csrf().disable();
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(camundaAuthenticationProvider);
    }
}
