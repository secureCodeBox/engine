package io.securecodebox.scanprocess.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Rüdiger Heins - iteratec GmbH
 * @since 07.02.18
 */
@Component
public class TestIoBean {
    private static final Logger LOG = LoggerFactory.getLogger(TestIoBean.class);

    public TestIoBean() {
        LOG.info("TestIoBean was successfully registered");
    }
}
