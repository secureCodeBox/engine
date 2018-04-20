package io.securecodebox.scanprocess;

import io.securecodebox.persistence.PersistenceProvider;
import io.securecodebox.sdk.ScanProcessEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;

@ScanProcessEntryPoint
public class ZapScanProcessConfiguration {

    ZapScanProcessConfiguration(@Autowired PersistenceProvider provider) {
    }
}
