package io.securecodebox.scanprocess.zap.listener;

import io.securecodebox.model.execution.Target;
import io.securecodebox.model.findings.Finding;
import io.securecodebox.scanprocess.zap.model.ZapSitemapEntry;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.mockito.Mock;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class TransformZapFindingsToZapTargetsListenerTest {

    @Mock
    DelegateExecution deli;

    @Test
    public void shouldProperlyTransformFindingsToTargets() {
        try {
            List<Finding> findings = new LinkedList<>();

            Finding finding1 = new Finding();
            finding1.setLocation("http://bodgeit:8080/bodgeit/");
            finding1.addAttribute("method", "POST");
            finding1.addAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            findings.add(finding1);

            List<Target> targets = new LinkedList<>();
            Target target = new Target();
            target.setLocation("http://bodgeit:8080/bodgeit/");
            target.getAttributes().put("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            targets.add(target);

            TransformZapFindingsToZapTargetsListener.transform(targets, findings);

            assertEquals(1, targets.size());
            assertTrue(targets.get(0).getAttributes().containsKey("ZAP_SITEMAP"));

            List<ZapSitemapEntry> sitemapEntries = (List<ZapSitemapEntry>) targets.get(0).getAttributes().get("ZAP_SITEMAP");
            assertEquals(1, sitemapEntries.size());

            ZapSitemapEntry entry = sitemapEntries.get(0);
            assertEquals("http://bodgeit:8080/bodgeit/", entry.location);
            assertEquals("POST", entry.method);
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
            finding1.addAttribute("method", "POST");
            finding1.addAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            findings.add(finding1);

            Finding finding2 = new Finding();
            finding2.setLocation("http://bodgeit:8080/bodgeit/search.jsp");
            finding2.addAttribute("method", "GET");
            finding2.addAttribute("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            findings.add(finding2);

            Finding finding3 = new Finding();
            finding3.setLocation("http://juice-shop:3000/#/search");
            finding3.addAttribute("method", "GET");
            finding3.addAttribute("ZAP_BASE_URL", "http://juice-shop:3000/");

            findings.add(finding3);

            List<Target> targets = new LinkedList<>();
            Target target1 = new Target();
            target1.setLocation("http://bodgeit:8080/bodgeit/");
            target1.getAttributes().put("ZAP_BASE_URL", "http://bodgeit:8080/bodgeit/");

            targets.add(target1);

            Target target2 = new Target();
            target2.setLocation("http://juice-shop:3000/");
            target2.getAttributes().put("ZAP_BASE_URL", "http://juice-shop:3000/");

            targets.add(target2);

            TransformZapFindingsToZapTargetsListener.transform(targets, findings);

            assertEquals(2, targets.size());

            List<ZapSitemapEntry> sitemapBodgeit = (List<ZapSitemapEntry>) targets.get(0).getAttributes().get("ZAP_SITEMAP");
            List<ZapSitemapEntry> sitemapJuiceShop = (List<ZapSitemapEntry>) targets.get(1).getAttributes().get("ZAP_SITEMAP");

            assertEquals(2, sitemapBodgeit.size());
            assertEquals(1, sitemapJuiceShop.size());

            ZapSitemapEntry entryBodgeit1 = sitemapBodgeit.get(0);
            ZapSitemapEntry entryBodgeit2 = sitemapBodgeit.get(1);

            ZapSitemapEntry entryJuiceShop = sitemapJuiceShop.get(0);

            assertEquals("http://bodgeit:8080/bodgeit/", entryBodgeit1.location);
            assertEquals("POST", entryBodgeit1.method);

            assertEquals("http://bodgeit:8080/bodgeit/search.jsp", entryBodgeit2.location);
            assertEquals("GET", entryBodgeit2.method);

            assertEquals("http://juice-shop:3000/#/search", entryJuiceShop.location);
            assertEquals("GET", entryJuiceShop.method);

        } catch (Exception e) {
            fail("Should not throw exceptions");
        }
    }
}