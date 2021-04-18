package com.alissonvisa.messaging;

import com.alissonvisa.domain.person.Person;

public class CreatePersonCommand extends ApplicationCommand<Person> {

    public CreatePersonCommand(Person payload) {
        super(payload);
    }

}
