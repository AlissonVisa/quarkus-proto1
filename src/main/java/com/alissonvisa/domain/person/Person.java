package com.alissonvisa.domain.person;

import com.alissonvisa.base.messaging.CommandType;
import com.alissonvisa.base.persistence.ApplicationEntity;
import com.alissonvisa.base.persistence.Stateful;
import com.alissonvisa.domain.person.command.CreatePersonCommand;
import com.alissonvisa.domain.person.command.UpdatePersonNameCommand;
import io.quarkus.mongodb.panache.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jbosslog.JBossLog;
import org.bson.types.ObjectId;

import javax.enterprise.event.Observes;

@Data
@JBossLog
@NoArgsConstructor
@AllArgsConstructor
@MongoEntity
public class Person extends ApplicationEntity {

    private ObjectId id;
    @NonNull
    private String name;

    public void handle(@Observes @CommandType CreatePersonCommand command) {
        this.name = command.getName();
        this.persist();
    }

    @Stateful
    public void handle(@Observes @CommandType UpdatePersonNameCommand command) {
        this.name = command.getName();
        this.update();
    }

    @Override
    public Person find(ObjectId id) {
        return Person.findById(id);
    }
}
