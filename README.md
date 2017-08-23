# activiti-cloud-starters

Starters for Activiti Cloud Based apps. 

## Debugging Integration tests
Most of the integration tests inside this project are relying on docker containers 
(Keycloak and RabbitMQ) which are automatically launched by Maven. If you want to
 debug an integration test using your IDE you need to either run manually the containers
 either use remote debug. These are the steps for remote debug:
 1. add debug option to the Failsafe plugin in the Maven command: 
 
     `mvn clean verify -Dmaven.failsafe.debug`
 1. wait for the message `Listening for transport dt_socket at address: 5005`
 1. attach a remote debug from the IDE