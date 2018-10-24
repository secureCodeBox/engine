package io.securecodebox.engine.auth;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;


import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CamundaAuthenticationProviderTest {

    @InjectMocks
    CamundaAuthenticationProvider classUnderTest;

    @Mock
    ProcessEngine processEngine;

    @Mock
    IdentityService identityService;

    @Mock
    Authentication authDummy;

    @Test
    public void shouldAuthenticateIfCredentialsAreValid() {
        given(authDummy.getName()).willReturn("username");
        given(authDummy.getCredentials()).willReturn("correct-password");
        given(processEngine.getIdentityService()).willReturn(identityService);
        given(identityService.checkPassword("username","correct-password")).willReturn(true);

        Authentication result = classUnderTest.authenticate(authDummy);

        verify(identityService,times(1)).checkPassword("username","correct-password");
        assertTrue(result.isAuthenticated());
    }

    @Test
    public void shouldAuthenticateIfCredentialsAreInvalid() {
        given(authDummy.getName()).willReturn("username");
        given(authDummy.getCredentials()).willReturn("wrong-password");
        given(processEngine.getIdentityService()).willReturn(identityService);
        given(identityService.checkPassword("username","wrong-password")).willReturn(false);

        final Throwable exception = catchThrowable(() -> classUnderTest.authenticate(authDummy));

        verify(identityService,times(1)).checkPassword("username","wrong-password");
        assertThat(exception).isInstanceOf(BadCredentialsException.class);
    }
}
