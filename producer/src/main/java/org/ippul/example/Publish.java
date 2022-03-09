package org.ippul.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import javax.jms.*;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
                .serverUrl("https://keycloak:8443/auth") //KEYCLOAK_SERVICE_HOST
                .grantType(OAuth2Constants.PASSWORD)
                .realm("amq-sso-realm")
                .clientId("admin-cli")
                .username("admin")
                .password("Pa$$w0rd")
                .resteasyClient(
                        new ResteasyClientBuilder()
                                .sslContext(getSSLContext("https://keycloak:8443/auth"))
                                .connectionPoolSize(10).build()
                ).build();
        String userId = keycloak.realm("amq-sso-realm")
                .users().search("ippul").get(0).getId();
        List<UserSessionRepresentation> userSessions = keycloak.realm("amq-sso-realm")
                .users().get(userId).getUserSessions();
        LOGGER.info("User ippul > Active session {}", userSessions.size());
    }

    public static SSLContext getSSLContext(String url) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            Certificate[] certs = getCertificates(url);
            for(Certificate cert : certs){
                if(cert instanceof X509Certificate) {
                    X509Certificate certificateToAdd = (X509Certificate) cert;
                    keyStore.setCertificateEntry(certificateToAdd.getSubjectDN().getName().replaceAll("CN=", "").replaceAll("\\*", ""), cert);
                }
            }
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Certificate[] getCertificates(final String host) throws IOException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
        };
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
        }
        URL obj = new URL(host);
        HttpsURLConnection con =  (HttpsURLConnection) obj.openConnection();
        con.connect();
        return con.getServerCertificates();
    }
}
