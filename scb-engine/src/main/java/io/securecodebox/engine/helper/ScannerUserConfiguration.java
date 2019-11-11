package io.securecodebox.engine.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
@ConfigurationProperties( prefix = "securecodebox.rest.user" )
public class ScannerUserConfiguration {

    private ArrayList<ScannerUser> scanner = new ArrayList<>();

    public static class ScannerUser {
        private String username;
        private String password;

        public ScannerUser() {}

        public ScannerUser(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public ArrayList<ScannerUser> getScannerUsers() {
        return scanner;
    }
}
