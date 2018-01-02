package org.abrahamalarcon.datastream.controller;

import org.abrahamalarcon.datastream.dom.request.DatastoreMessage;
import org.abrahamalarcon.datastream.dom.response.DatastoreResponse;
import org.abrahamalarcon.datastream.service.DatastoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketController
{
    @Autowired SimpMessageSendingOperations messagingTemplate;
    @Autowired DatastoreService datastoreService;

    @MessageMapping("/notifyme/{clientId}/{eventId}/{uuid}")
    public void subscribeToEvent(@DestinationVariable String clientId,
                                 @DestinationVariable String eventId,
                                 @DestinationVariable String uuid,
                                 @Payload DatastoreMessage message) throws Exception
    {
        String toReply = "/queue/" + clientId + "/" + eventId;
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
