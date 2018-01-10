package org.abrahamalarcon.datastream.controller;

import org.abrahamalarcon.datastream.dom.DatastoreMessage;
import org.abrahamalarcon.datastream.dom.response.DatastoreResponse;
import org.abrahamalarcon.datastream.service.DatastoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.logging.Logger;

@Controller
public class EventController
{
    private static Logger logger = Logger.getLogger(EventController.class.getName());

    @Autowired SimpMessageSendingOperations messagingTemplate;
    @Autowired DatastoreService datastoreService;

    /**
     * Any client can send a new event into the subscription queue
     * @param clientId
     * @param eventId
     * @param uuid
     * @param message
     * @throws Exception
     */
    @MessageMapping("/newevent/{clientId}/{eventId}/{uuid}")
    public void sendNew(@DestinationVariable String clientId,
                                 @DestinationVariable String eventId,
                                 @DestinationVariable String uuid,
                                 @Payload DatastoreMessage message) throws Exception
    {
        String toReply = String.format("/queue/subscription/%s/%s", clientId, eventId);
        try
        {
            DatastoreResponse response = datastoreService.pull(message, eventId);
            response.setUuid(uuid);
            messagingTemplate.convertAndSend(toReply, response);
        }
        catch(Exception e)
        {
            Object obj = new Object(){ public String getMessage(){return e.getMessage();} public String getUuid(){ return uuid;}};
            messagingTemplate.convertAndSend(toReply, obj);
        }
    }

}
