package com.github.rishikeshr;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class AwsMqPublisherApplication {

	private static String WIRE_LEVEL_ENDPOINT;
	private static String ACTIVE_MQ_USERNAME;
	private static String ACTIVE_MQ_PASSWORD;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(AwsMqPublisherApplication.class, args);
		
		WIRE_LEVEL_ENDPOINT = System.getenv("WIRE_LEVEL_ENDPOINT");
		ACTIVE_MQ_USERNAME = System.getenv("ACTIVE_MQ_USERNAME");
		ACTIVE_MQ_PASSWORD = System.getenv("ACTIVE_MQ_PASSWORD");
		
		final ActiveMQConnectionFactory connectionFactory = createActiveMQConnectionFactory();
		final PooledConnectionFactory pooledConnectionFactory = createPooledConnectionFactory(connectionFactory);
		
		

		try {
			sendMessage(pooledConnectionFactory);
			// receiveMessage(connectionFactory);
			// pooledConnectionFactory.stop();

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void sendMessage(PooledConnectionFactory pooledConnectionFactory) throws JMSException {
		// Establish a connection for the producer.
		final Connection producerConnection = pooledConnectionFactory.createConnection();
		producerConnection.start();

		// Create a session.
		final Session producerSession = producerConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create a queue named "MyQueue".
		final Destination producerDestination = producerSession.createQueue("MyQueue");

		// Create a producer from the session to the queue.
		final MessageProducer producer = producerSession.createProducer(producerDestination);
		producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

		// Create a message.
		final String text = new Date().toString() + "Hello from Amazon MQ!";
		final TextMessage producerMessage = producerSession.createTextMessage(text);

		while (true) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Send the message.
			producer.send(producerMessage);
			System.out.println("Message sent.");
		}
		// Clean up the producer.
//		producer.close();
//		producerSession.close();
//		producerConnection.close();
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
		final Message consumerMessage = consumer.receive(1000);

		// Receive the message when it arrives.
		final TextMessage consumerTextMessage = (TextMessage) consumerMessage;
		System.out.println("Message received: " + consumerTextMessage.getText());

		// Clean up the consumer.
		consumer.close();
		consumerSession.close();
		consumerConnection.close();
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
