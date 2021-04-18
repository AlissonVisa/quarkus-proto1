package com.alissonvisa;

import com.alissonvisa.domain.person.Person;
import com.alissonvisa.messaging.CommandGateway;
import com.alissonvisa.messaging.CreatePersonCommand;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello-resteasy")
public class GreetingResource {

    @Inject
    private CommandGateway commandGateway;

    @Inject
    private Instance<Person> persons;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }

    @POST
    @Path("/create-person")
    @Produces(MediaType.TEXT_PLAIN)
    public void createPerson() {
        commandGateway.send(new CreatePersonCommand(new Person("Pedro")));
        for (Person person: persons) {
            System.out.println(person.toString());
        }
    }
}