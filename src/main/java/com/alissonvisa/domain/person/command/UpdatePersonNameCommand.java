package com.alissonvisa.domain.person.command;

import com.alissonvisa.base.messaging.ApplicationCommand;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@Getter
@Builder
@RequiredArgsConstructor
public class UpdatePersonNameCommand extends ApplicationCommand {

    private final ObjectId id;
    private final String name;

}
