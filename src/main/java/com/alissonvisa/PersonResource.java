package com.alissonvisa;

import com.alissonvisa.base.messaging.CommandGateway;
import com.alissonvisa.domain.person.Person;
import com.alissonvisa.domain.person.command.CreateAddressCommand;
import com.alissonvisa.domain.person.command.CreatePersonCommand;
import com.alissonvisa.domain.person.command.DeletePersonCommand;
import com.alissonvisa.domain.person.command.UpdatePersonNameCommand;
import com.alissonvisa.domain.person.dto.AddressDTO;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/person")
public class PersonResource {

    @Inject
    CommandGateway commandGateway;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> listAll() {
        return Person.findAll().list();
    }

    @POST
    @Path("")
    @Produces(MediaType.TEXT_PLAIN)
    public void createPerson(@QueryParam("name") String name,
                             @QueryParam("lastName") String lastName,
                             @QueryParam("age") Short age) {
        commandGateway.send(CreatePersonCommand.builder()
                .name(name)
                .lastName(lastName)
                .age(age)
                .build());
    }

    @PUT
    @Path("/address")
    @Produces(MediaType.TEXT_PLAIN)
    public void createAddress(AddressDTO address) {
        commandGateway.send(CreateAddressCommand.builder()
                .id(new ObjectId(address.getPersonId()))
                .street(address.getStreet())
                .number(address.getNumber())
                .addressType(address.getAddressType())
                .city(address.getCity())
                .build());
    }

    @PATCH
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public void updatePersonName(@PathParam("id") String id,
                                 @QueryParam("name") String name,
                                 @QueryParam("lastName") String lastName,
                                 @QueryParam("lazy") Boolean lazy) {
        commandGateway.send(UpdatePersonNameCommand.builder()
                .id(new ObjectId(id))
                .name(name)
                .lastName(lastName)
                .lazy(lazy)
                .build());

    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public void updatePersonName(@PathParam("id") String id) {
        commandGateway.send(DeletePersonCommand.builder()
                .id(new ObjectId(id))
                .build());

    }
}