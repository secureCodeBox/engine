package io.securecodebox.engine.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


@Configuration
@EnableWebSecurity
public class BasicAuthAuthProviderWebSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    CamundaAuthenticationProvider camundaAuthenticationProvider;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.antMatcher("/box/**").authorizeRequests()
            .anyRequest().authenticated()
            .and().httpBasic();
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(camundaAuthenticationProvider);
    }
}
