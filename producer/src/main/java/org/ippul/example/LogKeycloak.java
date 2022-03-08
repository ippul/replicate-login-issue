package org.ippul.example;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserSessionRepresentation;

import java.util.List;

public class LogKeycloak {

    public static void main(String[] args) {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl("http://" + System.getenv("KEYCLOAK_SERVICE_HOST") + ":"+System.getenv("KEYCLOAK_SERVICE_PORT")+ "/auth") //KEYCLOAK_SERVICE_HOST
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
        for(UserSessionRepresentation userSession:userSessions){

        }
    }
}
