package com.alissonvisa.messaging;

public class CreatePersonCommand extends ApplicationCommand<PersonPayload> {

    public CreatePersonCommand(PersonPayload payload) {
        super(payload);
    }

}
