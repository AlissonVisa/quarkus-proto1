package com.alissonvisa.domain.person.command;

import com.alissonvisa.base.messaging.ApplicationCommand;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class DeletePersonCommand extends ApplicationCommand {

    private ObjectId id;

}
