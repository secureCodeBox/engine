package io.securecodebox.scanprocesses.amassnmap.util;

import io.securecodebox.model.findings.Finding;
import io.securecodebox.model.findings.OsiLayer;
import io.securecodebox.model.findings.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public final class HttpHeaderStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(HttpHeaderStrategy.class);

    private final String header;
    private final ArrayList<HttpHeaderStrategyExpression> tests = new ArrayList<>();


    public HttpHeaderStrategy (String header) {
        this.header = header;
    }

    /**
     * Creates a finding if the header is present
     * @return
     */
    public HttpHeaderStrategyExpression ifPresent () {
        final HttpHeaderStrategyExpression expression = new HttpHeaderStrategyExpression(this, value -> value != null);
        tests.add (expression);
        return expression;
    }

    /**
     * Creates a finding if the header is missing
     * @return
     */
    public HttpHeaderStrategyExpression ifMissing () {
        final HttpHeaderStrategyExpression expression = new HttpHeaderStrategyExpression(this, value -> value == null);
        tests.add (expression);
        return expression;
    }

    /**
     * Convenience method, chaining {@link #ifMissing()} and {@link HttpHeaderStrategyExpression#createFinding(Severity, String)}
     * @param severity
     * @return
     */
    public HttpHeaderStrategy ifMissingCreateFinding (Severity severity) {
        return this.ifMissing().createFinding(severity, this.header + " header missing");
    }

    /**
     * Performs a lambda-based check; will not execute if header not present
     * @param test lambda expression function(String headerValue) -> Boolean
     * @return
     */
    public HttpHeaderStrategyExpression ifTrue (Function<String, Boolean> test) {
        final HttpHeaderStrategyExpression expression = new HttpHeaderStrategyExpression(this, value -> value != null && test.apply(value));
        tests.add (expression);
        return expression;
    }


    /**
     * Apply strategy to a finding
     * @param headers
     * @param finding
     * @return
     */
    public ArrayList<Finding> apply (HttpHeaders headers, Finding finding) {
        final ArrayList<Finding> additionalFindings = new ArrayList<>();
        for (HttpHeaderStrategyExpression test : tests) {
            final String value = headers.get(this.header);
            if (test.test.apply(value)) {
                if (test.findingNameAndDescriptionGenerator != null) {
                    test.findingNameAndDescriptionGenerator.accept(value, (name, description)
                            -> additionalFindings.add(createApplicationLevelFinding(finding, name, test.severity, description)));
                } else {
                    test.findingModifier.accept(value, finding);
                }
            }
        }
        return additionalFindings;
    }


    /**
     * Helper class; it is the state of a strategy having a logical expression (condition) but no name/description of
     * the finding to create (if condition resolves to true)
     */
    public final class HttpHeaderStrategyExpression {
        private final Function<String, Boolean> test;
        private final HttpHeaderStrategy strategy;
        private Severity severity = Severity.INFORMATIONAL;
        private BiConsumer<String, BiConsumer<String, String>> findingNameAndDescriptionGenerator = null;
        private BiConsumer<String, Finding> findingModifier = null;

        /**
         * Private constructor only to be called from class HttpHeaderStrategy
         * @param strategy
         * @param test
         */
        private HttpHeaderStrategyExpression (HttpHeaderStrategy strategy, Function<String, Boolean> test) {
            this.strategy = strategy;
            this.test = test;
        }

        /**
         * Shortcut for {@link #createFinding(Severity, String, String)}; name is also used as description
         * @param severity severity of the created finding
         * @param name name and Description of the created finding
         * @return
         */
        public HttpHeaderStrategy createFinding (Severity severity, String name) {
            return this.createFinding(severity, name, name);
        }

        /**
         * Creates a finding with static name / description. If description depends on the value of the header's value
         * please use {@link #createFinding(Severity, String, BiConsumer)}
         * @param severity
         * @param name
         * @param description
         * @return
         */
        public HttpHeaderStrategy createFinding (Severity severity, String name, String description) {
            this.severity = severity;
            this.findingNameAndDescriptionGenerator = (value, callback) -> callback.accept(name, description);
            return strategy;
        }

        /**
         * Similar to {@link #createFinding(Severity, String, String)}, but description is generated through the
         * consumer function and can depend on the value of the header.
         * @param severity severity of the finding
         * @param name name of the finding
         * @param consumer function(String headerValue, function(String findingDescription) -> void) -> void
         * @return
         */
        public HttpHeaderStrategy createFinding (Severity severity, String name, BiConsumer<String, Consumer<String>> consumer) {
            this.severity = severity;
            this.findingNameAndDescriptionGenerator = (value, generateFinding) -> {
                consumer.accept(value, description -> {
                    generateFinding.accept(name, description);
                });
            };
            return this.strategy;
        }

        /**
         * See {@link #createFinding(Severity, String, BiConsumer)}
         * @param severity
         * @param name
         * @param consumer
         * @return
         */
        public HttpHeaderStrategy createFinding (Severity severity, String name, Function<String, String> consumer) {
            this.severity = severity;
            this.findingNameAndDescriptionGenerator = (headerValue, generateFinding) -> {
                generateFinding.accept(name, consumer.apply(headerValue));
            };
            return this.strategy;
        }

        /**
         * Similar to {@link #createFinding(Severity, String, BiConsumer)}, but the consumer generates both the name and
         * the description for the finding.
         * @param severity severity of the finding
         * @param consumer function(String headerValue, function(String findingName, String findingDescription) -> void) -> void
         * @return
         */
        public HttpHeaderStrategy createFinding (Severity severity, BiConsumer<String, BiConsumer<String, String>> consumer) {
            this.severity = severity;
            this.findingNameAndDescriptionGenerator = consumer;
            return strategy;
        }

        public HttpHeaderStrategy modifyFinding (BiConsumer<String, Finding> consumer) {
            this.findingModifier = consumer;
            return this.strategy;
        }

    }


    /**
     * Creates a finding on OsiLayer.APPLICATION, copying most values (all attributes) from a given, existing finding
     * @param copyDetails
     * @param name
     * @param severity
     * @param description
     * @return
     */
    private Finding createApplicationLevelFinding (final Finding copyDetails, final String name, final Severity severity, final String description) {
        final Finding fnd = createCopyOfFinding(copyDetails);
        fnd.setName(name);
        fnd.setCategory("Http Header");
        fnd.setOsiLayer(OsiLayer.APPLICATION);
        fnd.addAttribute("protocol", "http");
        fnd.setDescription(description);
        fnd.setSeverity(severity);
        return fnd;
    }

    /**
     * Creates a copy of a finding, omitting name, category, description, osi-layer and severity
     * @param copyDetails
     * @return
     */
    private Finding createCopyOfFinding (final Finding copyDetails) {
        final Finding fnd = new Finding();
        fnd.setId(UUID.randomUUID());
        fnd.setLocation(copyDetails.getLocation());
        fnd.setReference(copyDetails.getReference());
        copyDetails.getAttributes().keySet().forEach(key -> {
            Object value = copyDetails.getAttributes().get(key);
            if (value instanceof Serializable) {
                fnd.addAttribute(key, (Serializable) value);
            }
        });
        return fnd;
    }

}
