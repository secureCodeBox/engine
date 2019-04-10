/*
 *
 *  SecureCodeBox (SCB)
 *  Copyright 2015-2018 iteratec GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */
package io.securecodebox.engine.auth;

import io.securecodebox.engine.service.AuthService;
import org.camunda.bpm.engine.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@Order(1231231)
public class CamundaAuthContextSetup extends WebSecurityConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CamundaAuthContextSetup.class);

    @Autowired
    IdentityService identityService;

    @Autowired
    private AuthService authService;

    @Override
    public void configure(HttpSecurity http) {
        LOG.debug("Init: Camunda Auth Context Setup Filter");
        http.antMatcher("/box/**").addFilterAfter(new CamundaAuthContextSetupFilter(), FilterSecurityInterceptor.class);
    }

    private class CamundaAuthContextSetupFilter extends GenericFilterBean {

        /**
         * Sets up the Camunda Authentication Context before the Resource gets executed
         */
        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            identityService.setAuthentication(authService.getAuthentication());

            filterChain.doFilter(servletRequest, servletResponse);
        }

        /**
         * Tears down the Camunda Authentication Context after the Resource got executed
         */
        @Override
        public void destroy(){
            identityService.clearAuthentication();
        }
    }
}
