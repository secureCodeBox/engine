package io.securecodebox.scanprocess.zap.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TransformZapFindingsToZapTargetsListenerTest {
    TransformZapFindingsToZapTargetsListener underTest=new TransformZapFindingsToZapTargetsListener();
    @Test
    public void shouldProperlyTransformFindingsToTargets() {
        try {
            List<Finding> findings = new LinkedList<>();

            Finding finding1 = new Finding();
            finding1.setLocation("http://bodgeit:8080/bodgeit/");
            finding1.addAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            findings.add(finding1);

            List<Target> targets = new LinkedList<>();
            Target target = new Target();
            target.getAttributes().put("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            targets.add(target);

            underTest.transform(targets, findings);

            assertEquals(1, targets.size());
            assertTrue(targets.get(0).getAttributes().containsKey("ZAP_SITEMAP"));

            List<Object> sitemapEntries = (List<Object>) targets.get(0).getAttributes().get("ZAP_SITEMAP");
            assertEquals(1, sitemapEntries.size());
        } catch (Exception e) {
            fail("Should not throw exceptions");
        }
    }

    @Test
    public void shouldProperlyTransformFindingsToTargetsWhenMultipleTargetsWereScanned() {
        try {
            List<Finding> findings = new LinkedList<>();

            Finding finding1 = new Finding();
            finding1.setLocation("http://bodgeit:8080/bodgeit/");
            finding1.addAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            findings.add(finding1);

            Finding finding2 = new Finding();
            finding2.setLocation("http://bodgeit:8080/bodgeit/search.jsp");
            finding2.addAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            findings.add(finding2);

            Finding finding3 = new Finding();
            finding3.setLocation("http://juice-shop:3000/#/search");
            finding3.addAttribute("ZAP_BASE_URL", "http://juice-shop:3000/");

            findings.add(finding3);

            List<Target> targets = new LinkedList<>();
            Target target1 = new Target();
            target1.getAttributes().put("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            targets.add(target1);

            Target target2 = new Target();
            target2.setLocation("http://juice-shop:3000/");
            target2.getAttributes().put("ZAP_BASE_URL", "http://juice-shop:3000/");

            targets.add(target2);

            underTest.transform(targets, findings);

            assertEquals(2, targets.size());

            List<Object> sitemapBodgeit = (List<Object>) targets.get(0).getAttributes().get("ZAP_SITEMAP");
            List<Object> sitemapJuiceShop = (List<Object>) targets.get(1).getAttributes().get("ZAP_SITEMAP");

            assertEquals(2, sitemapBodgeit.size());
            assertEquals(1, sitemapJuiceShop.size());

        } catch (Exception e) {
            fail("Should not throw exceptions");
        }
    }
}
