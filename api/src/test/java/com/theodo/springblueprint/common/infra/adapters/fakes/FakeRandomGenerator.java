package com.theodo.springblueprint.common.infra.adapters.fakes;

import com.theodo.springblueprint.common.domain.ports.RandomGeneratorPort;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class FakeRandomGenerator implements RandomGeneratorPort {

    private final AtomicLong atomicLong = new AtomicLong(0);

    @Override
    public UUID uuid() {
        String name = Long.toString(atomicLong.getAndIncrement());
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }
}
