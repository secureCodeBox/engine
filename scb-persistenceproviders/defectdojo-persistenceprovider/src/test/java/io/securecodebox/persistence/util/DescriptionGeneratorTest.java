package io.securecodebox.persistence.util;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.securitytest.SecurityTest;
import io.securecodebox.persistence.util.DescriptionGenerator;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;


public class DescriptionGeneratorTest {
    DescriptionGenerator descriptionGenerator;
    SecurityTest securityTest;

    @Before
    public void setUp() {
        descriptionGenerator = new DescriptionGenerator();
        descriptionGenerator.setClock(Clock.fixed(Instant.ofEpochSecond(1546876203), ZoneId.of("Europe/Berlin")));
        securityTest = new SecurityTest();
    }

    @Test
    public void generate(){
        securityTest.setName("nmap");
        securityTest.setTarget(new Target());
        securityTest.getTarget().setName("Example Target");
        securityTest.getTarget().setLocation("http://example.target");

        assertEquals("#Nmap Scan  \nTime: 07.01.2019 16:50:03  \nTarget: Example Target \"http://example.target\"", descriptionGenerator.generate(securityTest));
    }

    @Test
    public void nullGenerate(){
        securityTest.setTarget(new Target());

        assertEquals("#Generic Findings Import  \nTime: 07.01.2019 16:50:03  \nTarget: null \"null\"", descriptionGenerator.generate(securityTest));
    }

}
