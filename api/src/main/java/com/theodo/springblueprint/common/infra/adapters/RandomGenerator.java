package com.theodo.springblueprint.common.infra.adapters;

import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RandomGenerator implements RandomGeneratorPort {

    @Override
    public UUID uuid() {
        return UUID.randomUUID();
    }
}
