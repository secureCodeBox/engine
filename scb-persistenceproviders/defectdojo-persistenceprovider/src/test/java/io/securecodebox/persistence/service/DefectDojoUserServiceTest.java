package io.securecodebox.persistence.service;

import io.securecodebox.model.rest.Report;
import io.securecodebox.persistence.DefectDojoPersistenceProvider;
import io.securecodebox.persistence.exceptions.DefectDojoProductNotFound;
import io.securecodebox.persistence.exceptions.DefectDojoUserNotFound;
import io.securecodebox.persistence.models.DefectDojoMetaFields;
import io.securecodebox.persistence.models.DefectDojoResponse;
import io.securecodebox.persistence.models.EngagementResponse;
import io.securecodebox.persistence.models.ToolType;
import io.securecodebox.persistence.util.DescriptionGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DefectDojoUserServiceTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    DefectDojoUserService defectDojoUserService;

    @Mock
    DescriptionGenerator descriptionGenerator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        descriptionGenerator.setClock(Clock.fixed(Instant.ofEpochSecond(1546876203), ZoneId.systemDefault()));
    }


    @Test(expected = DefectDojoUserNotFound.class)
    public void failsIfUserCouldNotBeFound(){
        when(defectDojoUserService.getUserId(any())).thenThrow(new DefectDojoUserNotFound(""));

        defectDojoUserService.getUserId("This name does not exist");
    }

}
