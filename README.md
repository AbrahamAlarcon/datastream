
# Project Name
DataStream
The idea of this project is to show how implementing WebSocket and STOMP we can have WebSocket over HTTP and manage a
in memory Message Broker with the clients to distribute messages using a long-lived connection.

The client can send messages to multiple endpoints using RESTful architecture style to define application destinations,
which will not be opening new connections, but reusing the long-lived one.

Since the solution implements a message broker-based distribution, the client can also subscribe to one topic or queue
and receives new events without the need of applying other patterns such as Long polling or Polling, which saves a lot of
resources at server side limiting them to the ones to keep the connection open.

Additionally this solution can be used to resolve multiple conflicts today and integration issues such as:
   - Multiple clients asking by integrating their APIs, when they could instead call our single API
     and keep the connection open to receive new messages and reply back if needed, so there is no
     need for a request-reponse integration to provide real-time flow.
   - There is one single integration point to maintain and support, since there is one single API.
   - The original model for real-time solutions using Request-Response is upgrated to better model which supports
     real-time but keeps the integration responsability on the service client and no on the service provider, reducing
     the cost of new client onboardings.


# Technology stack
Spring Boot
Spring Validation (Valang)
Spring Cache (ehcache)
Spring Cloud
Hystrix
Spring Websocket
Spring Messaging


# Company Website
https://github.com/AbrahamAlarcon/datastream

# https://www.wunderground.com/weather/api
Key ID
066631e4c9e41f5a


# Test WUnderground directly
$ curl http://api.wunderground.com/api/*****/geolookup/conditions/forecast/q/Chile/Santiago.json

{
  "response": {
  "version":"0.1",
  "termsofService":"http://www.wunderground.com/weather/api/d/terms.html",
  "features": {
  "geolookup": 1
  ,
  "conditions": 1
  ,
  "forecast": 1
  }
        }
                ,       "location": {
                "type":"INTLCITY",
                "country":"CH",
                "country_iso3166":"CL",
                "country_name":"Chile",
                "state":"RM",
                "city":"Santiago",
                "tz_short":"CLST",
                "tz_long":"America/Santiago",
...

# Test
$ curl -i -N -H 'Connection: Upgrade' -H 'Upgrade: websocket' -H 'Host: localhost:8000' -H 'Sec-WebSocket-Version: 13' -H 'Sec-WebSocket-Key: rXktpaT/h9EwW7abT5GFWw' http://localhost:8000/ws

Connect to server: ws://localhost:55920/ws
Subscribe to channel: /queue/client1/forecast

Send message to endpoint: /stream/notifyme/client1/forecast/1
{"country":"Chile","city":"Santiago"}

Waiting new messages....
{"uuid":"1","response":{"version":"0.1","termsofService":"http://www.wunderground.com/weather/api/d/terms.html","features":{"geolookup":0,"conditions":0,"forecast":1}},"location":null,"forecast":{"txt_forecast":{"date":"12:02 PM -03","forecastday":[{"period":0,"icon":"clear","icon_url":"http://icons.wxug.com/i/c/k/clear.gif","title":"Tuesday","fcttext":"Abundant sunshine. High near 85F. Winds SW at 10 to 20 mph.","fcttext_metric":"Lots of sunshine. High around 30C. Winds SW at 15 to 30 km/h.","pop":"0"},{"period":1,"icon":"nt_clear","icon_url":"http://icons.wxug.com/i/c/k/nt_clear.gif","title":"Tuesday Night","fcttext":"A clear sky. Low near 60F. Winds SSE at 10 to 20 mph.","fcttext_metric":"Clear skies. Low around 15C. Winds SSE at 15 to 30 km/h.","pop":"0"},{"period":2,"icon":"clear","icon_url":"http://icons.wxug.com/i/c/k/clear.gif","title":"Wednesday","fcttext":"Mainly sunny. High 88F. Winds SSW at 10 to 20 mph.","fcttext_metric":"A mainly sunny sky. High 31C. Winds SSW at 15 to 30 km/h.","pop":"0"},{"period":3,"icon":"nt_clear","icon_url":"http://icons.wxug.com/i/c/k/nt_clear.gif","title":"Wednesday Night","fcttext":"A mostly clear sky. Low 59F. Winds S at 10 to 20 mph.","fcttext_metric":"Clear skies. Low near 15C. S winds at 15 to 30 km/h, decreasing to 5 to 10 km/h.","pop":"0"},{"period":4,"icon":"clear","icon_url":"http://icons.wxug.com/i/c/k/clear.gif","title":"Thursday","fcttext":"Sunny. High 86F. Winds SSW at 10 to 20 mph.","fcttext_metric":"Sunny skies. High near 30C. Winds SSW at 15 to 30 km/h.","pop":"0"},{"period":5,"icon":"nt_clear","icon_url":"http://icons.wxug.com/i/c/k/nt_clear.gif","title":"Thursday Night","fcttext":"Clear skies. Low 57F. Winds S at 10 to 20 mph.","fcttext_metric":"Clear skies. Low 14C. Winds S at 15 to 30 km/h.","pop":"0"},{"period":6,"icon":"clear","icon_url":"http://icons.wxug.com/i/c/k/clear.gif","title":"Friday","fcttext":"Mainly sunny. High 86F. Winds SSW at 10 to 20 mph.","fcttext_metric":"Sunny. High near 30C. Winds SSW at 15 to 30 km/h.","pop":"0"},{"period":7,"icon":"nt_clear","icon_url":"http://icons.wxug.com/i/c/k/nt_clear.gif","title":"Friday Night","fcttext":"Clear. Low 61F. Winds S at 10 to 20 mph.","fcttext_metric":"Clear skies. Low 16C. Winds S at 15 to 30 km/h.","pop":"0"}]}},"error":null}
