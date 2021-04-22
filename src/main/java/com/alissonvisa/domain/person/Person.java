package com.alissonvisa.domain.person;

import com.alissonvisa.base.messaging.CommandType;
import com.alissonvisa.base.persistence.ApplicationEntity;
import com.alissonvisa.base.persistence.Stateful;
import com.alissonvisa.domain.person.command.CreateAddressCommand;
import com.alissonvisa.domain.person.command.CreatePersonCommand;
import com.alissonvisa.domain.person.command.DeletePersonCommand;
import com.alissonvisa.domain.person.command.UpdatePersonNameCommand;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.mongodb.panache.MongoEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.bson.types.ObjectId;

import javax.enterprise.event.Observes;
import java.util.ArrayList;
import java.util.List;

@Data
@JBossLog
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@MongoEntity
public class Person extends ApplicationEntity {

    private ObjectId id;
    private String name;
    private String lastName;
    private Short age;
    private List<Address> addresses = new ArrayList<>();

    public void handle(@Observes @CommandType CreatePersonCommand command) {
        this.name = command.getName();
        this.lastName = command.getLastName();
        this.age = command.getAge();
    }

    @Stateful
    public void handle(@Observes @CommandType UpdatePersonNameCommand command) {
        if(command.isLazy()) {
            sleep(3500L);
        }
        this.name = command.getName();
        this.lastName = command.getLastName();
    }

    private void sleep(Long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Stateful
    public void handle(@Observes @CommandType CreateAddressCommand command) {
        this.addresses.add(Address.builder()
                .addressId(ObjectId.get())
                .street(command.getStreet())
                .number(command.getNumber())
                .addressType(command.getAddressType())
                .city(command.getCity())
                .build());
    }

    @Stateful
    public void handle(@Observes @CommandType DeletePersonCommand command) {
        this.delete();
    }

}
