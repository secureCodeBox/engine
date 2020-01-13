package io.securecodebox.engine.helper;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;

@Validated
@Configuration
@ConfigurationProperties(prefix = "securecodebox")
public class AuthConfiguration {

    private List<UserConfiguration> users = new ArrayList<>();

    private List<GroupConfiguration> groups = new ArrayList<>();

    private List<TenantConfiguration> tenants = new ArrayList<>();

    public static class UserConfiguration {
        @NotEmpty
        // See: https://docs.camunda.org/manual/7.11/update/minor/79-to-710/#whitelist-pattern-for-user-group-and-tenant-ids
        // Minus the camunda admin part. As scanner users are never the camunda admin
        @Pattern(regexp = "[a-zA-Z0-9]+")
        private String id;
        @NotEmpty
        private String password;
        @Email
        private String email;
        @NotEmpty
        private String firstname = "Technical-User";
        @NotEmpty
        private String lastname = "Scanner-User";
        private List<String> groups = new ArrayList<>();
        private List<String> tenants = new ArrayList<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }

        public List<String> getGroups() {
            return groups;
        }

        public void setGroups(List<String> groups) {
            this.groups = groups;
        }

        public List<String> getTenants() {
            return tenants;
        }

        public void setTenants(List<String> tenants) {
            this.tenants = tenants;
        }
    }

    public static class GroupConfiguration {
        @NotEmpty
        // See: https://docs.camunda.org/manual/7.11/update/minor/79-to-710/#whitelist-pattern-for-user-group-and-tenant-ids
        // Minus the camunda admin part. As scanner users are never the camunda admin
        @Pattern(regexp = "[a-zA-Z0-9]+")
        private String id;

        @NotEmpty
        private String name;

        private List<GroupAuthorizations> authorizations = new ArrayList<>();

        public static class GroupAuthorizations {
            @NotEmpty
            private String resource;

            private List<String> permissions = new ArrayList<>();

            public String getResource() {
                return resource;
            }

            public void setResource(String resource) {
                this.resource = resource;
            }

            public List<String> getPermissions() {
                return permissions;
            }

            public void setPermissions(List<String> permissions) {
                this.permissions = permissions;
            }
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<GroupAuthorizations> getAuthorizations() {
            return authorizations;
        }

        public void setAuthorizations(List<GroupAuthorizations> authorizations) {
            this.authorizations = authorizations;
        }
    }

    public static class TenantConfiguration {
        @NotEmpty
        // See: https://docs.camunda.org/manual/7.11/update/minor/79-to-710/#whitelist-pattern-for-user-group-and-tenant-ids
        // Minus the camunda admin part. As scanner users are never the camunda admin
        @Pattern(regexp = "[a-zA-Z0-9]+")
        private String id;
        @NotEmpty
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public List<UserConfiguration> getUsers() {
        return users;
    }

    public void setScanner(List<UserConfiguration> users) {
        this.users = users;
    }

    public void setUsers(List<UserConfiguration> users) {
        this.users = users;
    }

    public List<GroupConfiguration> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupConfiguration> groups) {
        this.groups = groups;
    }

    public List<TenantConfiguration> getTenants() {
        return tenants;
    }

    public void setTenants(List<TenantConfiguration> tenants) {
        this.tenants = tenants;
    }
}
