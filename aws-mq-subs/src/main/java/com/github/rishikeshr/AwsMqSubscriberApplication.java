package com.github.rishikeshr;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AwsMqSubscriberApplication {

	private static String WIRE_LEVEL_ENDPOINT;
	private static String ACTIVE_MQ_USERNAME;
	private static String ACTIVE_MQ_PASSWORD;

	public static void main(String[] args) {
		SpringApplication.run(AwsMqSubscriberApplication.class, args);
		WIRE_LEVEL_ENDPOINT = System.getenv("WIRE_LEVEL_ENDPOINT");
		ACTIVE_MQ_USERNAME = System.getenv("ACTIVE_MQ_USERNAME");
		ACTIVE_MQ_PASSWORD = System.getenv("ACTIVE_MQ_PASSWORD");

		final ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
		final PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);

		try {

			receiveMessage(connectionFactory);
			// pooledConnectionFactory.stop();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void receiveMessage(ActiveMQConnectionFactory connectionFactory) throws JMSException {
		// Establish a connection for the consumer.
		// Note: Consumers should not use PooledConnectionFactory.
		final Connection consumerConnection = connectionFactory.createConnection();
		consumerConnection.start();

		// Create a session.
		final Session consumerSession = consumerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create a queue named "MyQueue".
		final Destination consumerDestination = consumerSession.createQueue("MyQueue");

		// Create a message consumer from the session to the queue.
		final MessageConsumer consumer = consumerSession.createConsumer(consumerDestination);

		// Begin to wait for messages.
		Message consumerMessage = consumer.receive(1000);

		TextMessage consumerTextMessage;

		while (true) {
			// Receive the message when it arrives.
			consumerTextMessage = (TextMessage) consumerMessage;
			System.out.println("Message received: " + consumerTextMessage.getText());

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			consumerMessage = consumer.receive(1000);
		}

		// Clean up the consumer.
//		consumer.close();
//		consumerSession.close();
//		consumerConnection.close();
	}

	private static PooledConnectionFactory createPooledConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
		// Create a pooled connection factory.
		final PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
		pooledConnectionFactory.setConnectionFactory(connectionFactory);
		pooledConnectionFactory.setMaxConnections(10);
		return pooledConnectionFactory;
	}

	private static ActiveMQConnectionFactory createActiveMQConnectionFactory() {
		// Create a connection factory.
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(WIRE_LEVEL_ENDPOINT);

		// Pass the username and password.
		connectionFactory.setUserName(ACTIVE_MQ_USERNAME);
		connectionFactory.setPassword(ACTIVE_MQ_PASSWORD);
		return connectionFactory;
	}
}
