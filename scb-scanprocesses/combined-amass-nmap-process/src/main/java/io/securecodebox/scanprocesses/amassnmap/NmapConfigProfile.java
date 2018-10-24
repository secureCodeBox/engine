package io.securecodebox.scanprocesses.amassnmap;

public enum NmapConfigProfile {
    HTTP_PORTS("-p 80,8080,443,8443"),
    TOP_100_PORTS("--top-ports 100");

    private final String parameter;

    NmapConfigProfile(final String parameter) {
        this.parameter = parameter;
    }

    public String getParameter() { return parameter; }
}
