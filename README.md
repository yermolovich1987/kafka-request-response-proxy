# kafka-request-response-proxy
Demo of the Kafka request-response gateway.

This project demonstrates the possibility to build a request response 
flow in combination of REST API + kafka.

Kafka messages is sent in Avro format.

The two flows are implemented in POC purpose:
1) Fire and forget flow with immediate return of the response to user. In this case, 
   user request is not waiting for the response from another service. Instead, the response
   is sent later to the alternative queue specified by manual reply topic header.
   
2) The request-reply flow where response to the caller is not returned until the 
   external service did not finish the processing and return a response to the reply 
   Kafka channel.
   
To tri
   