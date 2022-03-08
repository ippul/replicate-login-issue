package org.ippul.example;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserSessionRepresentation;

import java.util.List;

public class Example {
    public static void main(String[] args) {
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl("https://keycloak-example-1.apps-crc.testing/auth/") //KEYCLOAK_SERVICE_HOST
                    .grantType(OAuth2Constants.PASSWORD)
                    .realm("amq-sso-realm")
                    .clientId("admin-cli")
                    .username("admin")
                    .password("Pa$$w0rd")
                    .resteasyClient(
                            new ResteasyClientBuilder()
                                    .connectionPoolSize(10).build()
                    ).build();
            keycloak.tokenManager().getAccessToken();
            List<UserSessionRepresentation> userSessions = keycloak.realm("amq-sso-realm")
                    .users().get("ippul").getUserSessions();
    }
}
