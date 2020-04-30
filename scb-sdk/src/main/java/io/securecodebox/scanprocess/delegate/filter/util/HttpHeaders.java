package io.securecodebox.scanprocess.delegate.filter.util;

import io.securecodebox.model.findings.Finding;

import java.util.HashMap;
import java.util.Map;

public final class HttpHeaders {

    private final HashMap<String, String> headers = new HashMap<>();

    public HttpHeaders(String rawHeaders) {
        final String[] lines = rawHeaders.split ("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];

            // double-newline = end of http header section (ignore if first line; nmap bug)
            if (line.length() == 0) {
                if (i == 0) continue;
                else break;
            }

            int colonPos = line.indexOf(':');
            if (colonPos < 0) continue;
            final String
                    k = line.substring(0, colonPos).trim(),
                    v = line.substring(colonPos + 1).trim();
            headers.put(k, v);
        }
    }

    public boolean has (String key) {
        return headers.containsKey(key);
    }

    public String get (String key) {
        return headers.get(key);
    }

    public static boolean headersPresentInFinding (Finding finding) {
        Map<String, Object> attributes = finding.getAttributes();
        if (attributes == null) return false;
        Object scripts = attributes.get("scripts");
        if (scripts == null || !(scripts instanceof Map)) return false;
        return ((Map)scripts).containsKey("http-headers");
    }

    public static HttpHeaders fromFinding (Finding finding) {
        if (!headersPresentInFinding(finding)) return null;
        return new HttpHeaders(((Map<String,String>)finding.getAttributes().get("scripts")).get("http-headers"));
    }

}
