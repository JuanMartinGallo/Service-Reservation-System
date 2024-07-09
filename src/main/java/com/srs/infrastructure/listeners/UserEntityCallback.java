package com.srs.infrastructure.listeners;

import com.srs.domain.models.User;
import jakarta.validation.constraints.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Component
public class UserEntityCallback implements BeforeConvertCallback<User> {

    @Override
    public Publisher<User> onBeforeConvert(@NotNull User user, @NotNull SqlIdentifier table) {
        if (user.getDateCreated() == null) {
            user.setDateCreated(OffsetDateTime.now());
        }
        user.setLastUpdated(OffsetDateTime.now());
        return Mono.just(user);
    }
}




