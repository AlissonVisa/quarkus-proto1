package com.alissonvisa.domain.person.command;

import com.alissonvisa.base.messaging.ApplicationCommand;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class CreatePersonCommand extends ApplicationCommand {

    @NonNull
    private String name;

}
