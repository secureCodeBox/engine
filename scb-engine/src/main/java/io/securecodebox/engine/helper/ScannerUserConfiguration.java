package io.securecodebox.engine.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties( prefix = "securecodebox.rest.user" )
public class ScannerUserConfiguration {

    private List<ScannerUser> scanner = new ArrayList<>();

    public static class ScannerUser {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public List<ScannerUser> getScannerUsers() {
        return scanner;
    }

    public void setScanner(List<ScannerUser> scanner) {
        this.scanner = scanner;
    }
}
