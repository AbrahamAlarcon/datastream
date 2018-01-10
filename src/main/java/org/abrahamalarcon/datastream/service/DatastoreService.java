package org.abrahamalarcon.datastream.service;

import org.abrahamalarcon.datastream.dao.WeatherDAO;
import org.abrahamalarcon.datastream.dom.DatastoreMessage;
import org.abrahamalarcon.datastream.dom.response.DatastoreResponse;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

@Service
public class DatastoreService extends BaseService
{
	@Autowired WeatherDAO weatherDAO;

	public DatastoreResponse pull(DatastoreMessage message, String event) throws URISyntaxException
    {
		DatastoreResponse response = new DatastoreResponse();

        if(StringUtils.isBlank(event))
        {
            addFieldError(response, "event", "Event cannot be empty");
            return response;
        }

		if(StringUtils.isBlank(message.getCity()))
		{
			addFieldError(response, "city", "City cannot be empty");
			return response;
		}

		inputValidation(response, message, event);

		if(!response.isFailure())
        {
            String url = "/" + event;
            url += "/q";

            if(message.getCountry() != null)
            {
                url+= "/" + message.getCountry();
            }
            if(message.getCity() != null)
            {
                url+= "/" + message.getCity();
            }

            url += ".json";

            System.out.println("URL " + url);

            response  = weatherDAO.get(url);
        }

		return response;
	}

    void inputValidation(DatastoreResponse response, DatastoreMessage message, String event)
    {
        inputValidation(response, message, new String[] {"country", "city"});
        inputValidation(response, new Object() { public String getEvent(){ return event; }} , new String[] {"event"});
    }
}
