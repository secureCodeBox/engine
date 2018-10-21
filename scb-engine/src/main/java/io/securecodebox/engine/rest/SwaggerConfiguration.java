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

package io.securecodebox.engine.rest;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 23.04.18
 */
@Configuration
public class SwaggerConfiguration {

    // @formatter:off
    public ApiInfo apiInfo() {
        return new ApiInfoBuilder().
                title("SecureCodeBox API Documentation")
                .description("This Document describes the public API of the SecureCodeBox. It's mostly used for scanners to retrieve scan jobs from the engine and send results to the engine.")
                .contact(new Contact("SecureCodeBox-Team","https://github.com/secureCodeBox", ""))
                .license("Apache 2.0")
                .licenseUrl("https://github.com/secureCodeBox/engine/blob/master/LICENSE.txt")
                .version("1.0")
                .build();
    }

    @Bean
    public Docket serviceApi() {
        return apiDocketBuilder()
                .select()
                .paths(
                        Predicates.not(
                                    PathSelectors.ant("/error")
                        )
                ).build();
    }

    protected Docket apiDocketBuilder() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .globalResponseMessage(RequestMethod.GET, new ArrayList<>())
                .directModelSubstitute(LocalDate.class, String.class)
                .ignoredParameterTypes(Principal.class)
                .useDefaultResponseMessages(false)
                .consumes(Sets.newHashSet("application/json"))
                .produces(Sets.newHashSet("application/json"))
                .securitySchemes(Collections.singletonList(securityScheme()));
    }
    // @formatter:on

    private SecurityScheme securityScheme() {
        return new BasicAuth("basicAuth");
    }
}
