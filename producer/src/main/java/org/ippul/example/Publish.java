package org.ippul.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserSessionRepresentation;

public class Publish {
    private static final Logger LOGGER = LogManager.getLogger(Publish.class);

    public static void main(String[] args) throws Exception {
        final ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://ex-aao-hdls-svc:61616", "ippul", "Pa$$w0rd");
        final Connection connection = connectionFactory.createConnection();
        connection.start();
        final Session session  = connection.createSession();
        final Destination destination = session.createQueue("queue1");
        final MessageProducer producer = session.createProducer(destination);
        for(int count = 0; count < 6; count ++) {
            final String messageBody = "Test JMS Message " + UUID.randomUUID().toString();
            final TextMessage message = session.createTextMessage(messageBody);
            LOGGER.info("Sending message number {} with body {}", count, messageBody);
            producer.send(message);
            LOGGER.info("wait 10 seconds...");
            logRHSSOSession();
            Thread.sleep(10000l);
        }
        session.close();
        connection.close();
    }

    private static void logRHSSOSession(){
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
        LOGGER.info("User ippul > Active session {}", userSessions.size());
    }
}
