package com.alissonvisa.domain.person.command;

import com.alissonvisa.base.messaging.ApplicationCommand;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;

@Getter
@Builder
public class UpdatePersonNameCommand extends ApplicationCommand {

    private final ObjectId id;
    private final String name;
    private final String lastName;
    private final Boolean lazy;

}
