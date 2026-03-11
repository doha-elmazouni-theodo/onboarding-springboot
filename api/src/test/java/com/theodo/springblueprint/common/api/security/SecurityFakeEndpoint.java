package com.theodo.springblueprint.common.api.security;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityFakeEndpoint {

    public static final String PERMIT_ALL_POST_ENDPOINT = "/security/public/post";
    public static final String ADMIN_ONLY_POST_ENDPOINT = "/security/admin/get";

    @PostMapping(PERMIT_ALL_POST_ENDPOINT)
    public String permitAll() {
        return "permitAll";
    }

    @PostMapping(ADMIN_ONLY_POST_ENDPOINT)
    @PreAuthorize("hasRole('ADMIN')")
    public String adminOnly() {
        return "adminOnly";
    }
}
