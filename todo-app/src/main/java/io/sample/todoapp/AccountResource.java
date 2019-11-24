package io.sample.todoapp;

import org.springframework.http.MediaType;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;

@RestController
@RequestMapping(value = "/api/me")
public class AccountResource {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    @RolesAllowed({ "ROLE_USER" })
    public String me(Principal principal) {
        return principal.getName();
    }
}
