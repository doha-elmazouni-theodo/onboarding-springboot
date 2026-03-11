package com.theodo.springblueprint.common.api.beans;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InfraBeanConfiguration {

    @Bean
    public Clock utcClock() {
        return Clock.systemUTC();
    }

    // Configure the algorithm used to encrypt the password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
