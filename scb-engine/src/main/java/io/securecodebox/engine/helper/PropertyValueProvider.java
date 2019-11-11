package io.securecodebox.engine.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.stereotype.Component;

/**
 * Provides application property values, e.g. for referencing them in scan process definitions
 *
 * @author Victor-Philipp Negoescu - iteratec GmbH
 * @since 28.05.2018
 */
@Component("propertyValueProvider")
public class PropertyValueProvider {
    /**
     * Default business context
     */
    @Value("${securecodebox.default.context}")
    private String defaultContext;

    /**
     * Default target name
     */
    @Value("${securecodebox.default.target.name}")
    private String defaultTargetName;

    /**
     * Default target hostname / ip address
     */
    @Value("${securecodebox.default.target.location}")
    private String defaultTargetLocation;

    @Value("${securecodebox.rest.user.scanner-default.user-id}")
    private String defaultUserScannerId;

    @Value("${securecodebox.rest.user.scanner-default.password}")
    private String defaultUserScannerPassword;

    /**
     * Default target access URI
     */
    @Value("${securecodebox.default.target.uri}")
    private String defaultTargetUri;

    public String getDefaultContext() {
        return defaultContext;
    }

    public String getDefaultTargetName() {
        return defaultTargetName;
    }

    public String getDefaultTargetLocation() {
        return defaultTargetLocation;
    }

    public String getDefaultTargetUri() {
        return defaultTargetUri;
    }

    @DeprecatedConfigurationProperty(
            replacement = "securecodebox.rest.user.scanner",
            reason = "Using the new property multiple users can be configured, which is more flexible than only being able to create one."
    )
    String getDefaultUserScannerId() {
        return defaultUserScannerId;
    }

    @DeprecatedConfigurationProperty(
            replacement = "securecodebox.rest.user.scanner",
            reason = "Using the new property multiple users can be configured, which is more flexible than only being able to create one."
    )
    String getDefaultUserScannerPassword() {
        return defaultUserScannerPassword;
    }
}
