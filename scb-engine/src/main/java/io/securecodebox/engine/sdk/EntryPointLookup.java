package io.securecodebox.engine.sdk;

import io.securecodebox.sdk.ScannerEntryPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Collects {@link ScannerEntryPoint}s and adds them to component scan.
 *
 * @author rhe (ruediger.heins@iteratec.de)
 * @since 07.02.18
 */
class EntryPointLookup implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger LOG = LoggerFactory.getLogger(EntryPointLookup.class);

    private static final String CLASS_PATTERN = "**/*.class";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
            ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(null);
            Resource[] resources = resourcePatternResolver.getResources(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + CLASS_PATTERN);

            List<Class<?>> registry = collectEntryPoints(resources);
            ((AnnotationConfigEmbeddedWebApplicationContext) applicationContext).
                    register(registry.toArray(new Class<?>[0]));

        } catch (IOException e) {
            LOG.error("Exception  while scanning for EntryPoints.", e);
            e.printStackTrace();
        }
    }

    private List<Class<?>> collectEntryPoints(Resource[] resources) throws IOException {
        List<Class<?>> registry = new LinkedList<>();
        for (Resource resource : resources) {
            if (!isCommonLibPackage(resource.getURL().getPath())) {
                Class<?> aClass = lookupEntryPoint(resource);
                if (aClass != null) {
                    registry.add(aClass);
                }
            }
        }
        return registry;
    }

    private Class<?> lookupEntryPoint(Resource resource) throws IOException {
        try {
            EntryPointVisitor entryPointVisitor = new EntryPointVisitor();
            new ClassReader(resource.getInputStream()).accept(entryPointVisitor, ClassReader.SKIP_CODE);
            if (entryPointVisitor.isEntryPoint) {
                return Class.forName(convertFromPathToClass(entryPointVisitor.className));
            }
        } catch (Exception e) {
            // Caches Exception here as it just should skips the class on error...
            LOG.warn("Exception while scanning for EntryPoints. Processing {} failed", resource.getURL().getPath(), e);
        }
        return null;
    }

    private String convertFromPathToClass(String path) {
        return path.replace("/", ".");
    }

    private boolean isCommonLibPackage(String path) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("isCommonLibPackage: {}", path);
        }
        return Stream.of("!/java/", "!/com/sun/", "!/sun/", "/org/glassfish/", "!/org/testng/", "!/org/junit/",
                "!/javax/", "/org/apache/", "!/org/springframework/", "!/org/camunda/bpm/engine")
                .anyMatch(path::contains);
    }

    /**
     * Class visitor collecting ScannerEntryPoints
     */
    private class EntryPointVisitor extends ClassVisitor {

        String className;
        boolean isEntryPoint;

        public EntryPointVisitor() {
            super(Opcodes.ASM6);
            isEntryPoint = false;

        }

        public void visit(int version, int access, String name, String signature, String superName,
                String[] interfaces) {
            className = name;
        }

        public AnnotationVisitor visitAnnotation(String name, boolean var2) {

            if (name.contains(ScannerEntryPoint.class.getCanonicalName().replace(".", "/"))) {
                isEntryPoint = true;
            }
            return null;
        }
    }
}
