package org.abrahamalarcon.datastream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class WebsocketTests
{
    @Value("${local.server.port}") private int port;
    private String URL;
    BlockingQueue<String> blockingQueue;
    WebSocketStompClient stompClient;

    @Before
    public void setup()
    {
        URL = "ws://localhost:" + port + "/ws";
        blockingQueue = new LinkedBlockingDeque<>();
        stompClient = new WebSocketStompClient(new SockJsClient(
                Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    public void testReceiveAMessageFromTheServer_Queue() throws Exception
    {
        StompSession session = stompClient.connect(
                URL,
                new StompSessionHandlerAdapter(){})
                .get(1, TimeUnit.SECONDS);

        String simpleBroker = "/queue/test1";
        System.out.println("\nConnect to server: " + URL);
        System.out.println("Subscribe to channel: " + simpleBroker);

        session.subscribe(simpleBroker, new DefaultStompFrameHandler());

        String message = "MESSAGE TEST QUEUE";
        session.send(simpleBroker, message.getBytes());
        System.out.println("\nSend message .... \n" + message);

        String outputMessage = blockingQueue.poll(1, TimeUnit.SECONDS);
        System.out.println("\nOutput message .... \n" + outputMessage + "\n");

        Assert.assertEquals(message, outputMessage);
    }

    @Test
    public void testReceiveAMessageFromTheServer_Topic() throws Exception
    {
        StompSession session = stompClient.connect(
                URL,
                new StompSessionHandlerAdapter() {})
                .get(1, TimeUnit.SECONDS);

        String simpleBroker = "/topic/test2";
        System.out.println("\nConnect to server: " + URL);
        System.out.println("Subscribe to channel: " + simpleBroker);

        session.subscribe(simpleBroker, new DefaultStompFrameHandler());

        String message = "MESSAGE TEST TOPIC";
        session.send(simpleBroker, message.getBytes());
        System.out.println("\nSend message .... \n" + message);

        String outputMessage = blockingQueue.poll(1, TimeUnit.SECONDS);
        System.out.println("\nOutput message .... \n" + outputMessage + "\n");

        Assert.assertEquals(message, outputMessage);
    }

    class DefaultStompFrameHandler implements StompFrameHandler
    {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders)
        {
            return byte[].class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o)
        {
            blockingQueue.offer(new String((byte[]) o));
        }
    }

}
