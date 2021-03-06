package com.alissonvisa.domain.person.command;

import com.alissonvisa.base.messaging.ApplicationCommand;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePersonCommand extends ApplicationCommand {

    private String name;
    private String lastName;
    private Short age;

}
