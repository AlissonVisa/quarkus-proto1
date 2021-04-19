package com.alissonvisa.messaging;

public class UpdatePersonNameCommand extends ApplicationCommand<PersonNamePayload> {

    public UpdatePersonNameCommand(PersonNamePayload payload) {
        super(payload);
    }

}
