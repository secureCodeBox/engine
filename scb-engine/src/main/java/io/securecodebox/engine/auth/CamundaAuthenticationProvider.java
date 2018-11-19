package io.securecodebox.engine.auth;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CamundaAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    ProcessEngine engine;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String username = auth.getName();
        String password = auth.getCredentials().toString();


        boolean authenticated = engine.getIdentityService().checkPassword(username,password);

        if (authenticated) {
            // Set current camunda authentication
            User user = engine.getIdentityService().createUserQuery().userId(username).singleResult();
            List<String> groupIds = engine.getIdentityService()
                    .createGroupQuery()
                    .groupMember(username)
                    .list()
                    .stream()
                    .map(Group::getId)
                    .collect(Collectors.toList());
            engine.getIdentityService().setAuthentication(user.getId(), groupIds);

            return new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
        } else {
            throw new BadCredentialsException("Authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}
