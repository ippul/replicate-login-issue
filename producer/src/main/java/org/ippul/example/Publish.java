package org.ippul.example;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import javax.jms.*;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publish {
    private static final Logger LOGGER = LoggerFactory.getLogger(Publish.class);

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
            LOGGER.info("Sending message numebr {} with body {}", count, message);
            producer.send(message);
            LOGGER.info("wait 10s...");
            Thread.sleep(10000l);
        }
        session.close();
        connection.close();
    }
}
