package org.abrahamalarcon.datastream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.abrahamalarcon.datastream.dom.DatastoreMessage;
import org.abrahamalarcon.datastream.dom.response.DatastoreResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class RESTWebsocketTests
{
    private String URL;
    @Value("${local.server.port}")
    private int port;
    private CompletableFuture<DatastoreResponse> completableFuture;

    @Before
    public void setup()
    {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/ws";
    }

    @Test
    public void testNotifymeEndpoint() throws URISyntaxException, InterruptedException, ExecutionException,
            TimeoutException, JsonProcessingException
    {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        System.out.println("\nConnect to server: " + URL);
        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter(){})
                .get(1, TimeUnit.SECONDS);

        String clientId = "client1";
        String eventId = "forecast";

        String subscribeClientEvents = String.format("/queue/%s/%s", clientId, eventId);
        stompSession.subscribe(subscribeClientEvents, new DatastoreResponseStompFrameHandler());
        System.out.println("\nSubscribe to channel: " + subscribeClientEvents);

        String sendToNotifymeEndpoint = String.format("/app/notifyme/%s/%s/%s", clientId, eventId, "%s");

        DatastoreMessage message1 = new DatastoreMessage();
        message1.setCity("Santiago");
        message1.setCountry("Chile");

        String endpoint = String.format(sendToNotifymeEndpoint, 1);
        stompSession.send(endpoint, message1);
        System.out.println("\nSend message to endpoint: " + endpoint);
        System.out.println("message ....\n" + new ObjectMapper().writeValueAsString(message1));


        System.out.println("\nWaiting new messages....\n");
        DatastoreResponse output = completableFuture.get(10, TimeUnit.SECONDS);
        System.out.println("\n" + new ObjectMapper().writeValueAsString(output));

        Assert.assertNotNull(output);
    }

    private List<Transport> createTransportClient()
    {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }


    private class DatastoreResponseStompFrameHandler implements StompFrameHandler
    {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders)
        {
            return DatastoreResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o)
        {
            completableFuture.complete((DatastoreResponse) o);
        }
    }


}
