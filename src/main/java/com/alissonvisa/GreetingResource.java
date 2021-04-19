package com.alissonvisa;

import com.alissonvisa.messaging.*;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/hello-resteasy")
public class GreetingResource {

    @Inject
    private CommandGateway commandGateway;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }

    @POST
    @Path("/create-person")
    @Produces(MediaType.TEXT_PLAIN)
    public void createPerson() {
        commandGateway.send(new CreatePersonCommand(new PersonPayload("Pedro")));

    }

    @PATCH
    @Path("/person/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public void updatePerson(@PathParam("id") String id, @QueryParam("name") String name) {
        commandGateway.send(new UpdatePersonNameCommand(new PersonNamePayload(new ObjectId(id), name)));

    }
}