package org.abrahamalarcon.datastream.controller;

import org.abrahamalarcon.datastream.dom.SubscriptionMessage;
import org.abrahamalarcon.datastream.service.DatastoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class StreamingController
{
    private static Logger logger = Logger.getLogger(StreamingController.class.getName());

    @Autowired SimpMessageSendingOperations messagingTemplate;
    @Autowired DatastoreService datastoreService;

    @Value("${exposed.route.hostname}")
    private String host;

    @Value("${exposed.route.port}")
    private int port;

    private ListenableFuture<StompSession> connect()
    {
        Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
        List<Transport> transports = Collections.singletonList(webSocketTransport);

        SockJsClient sockJsClient = new SockJsClient(transports);
        sockJsClient.setMessageCodec(new Jackson2SockJsMessageCodec());

        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);

        String url = "ws://{host}:{port}/ws";
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

        return stompClient.connect(url, headers, new MyHandler(), host, port);
    }

    private class MyHandler extends StompSessionHandlerAdapter
    {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders)
        {
            logger.info("Now connected");
        }
    }

    /**
     * Client must create a subscription, indicating the output required (graphql)
     */
    @MessageMapping("/subscription/{clientId}/{eventId}")
    public void subscribeToEvent(@DestinationVariable String clientId,
                                 @DestinationVariable String eventId,
                                 @Payload SubscriptionMessage message) throws Exception
    {
        String toReply = String.format("/stream/in/%s/%s", clientId, eventId);
        String toSubscribe = String.format("/queue/subscription/%s/%s", clientId, eventId);
        try
        {
            //subscribe to toSubscribe and listen, on message send it to toReply queue
            connect().get().subscribe(toSubscribe, new StompFrameHandler()
            {
                public Type getPayloadType(StompHeaders stompHeaders)
                {
                    return byte[].class;
                }
                public void handleFrame(StompHeaders stompHeaders, Object o)
                {
                    logger.info("Received " + new String((byte[]) o));
                    messagingTemplate.convertAndSend(toReply, new String((byte[]) o));
                }
            });

        }
        catch(Exception e)
        {
            Object obj = new Object(){ public String getMessage(){return e.getMessage();}};
            messagingTemplate.convertAndSend(toReply, obj);
        }
    }

}
