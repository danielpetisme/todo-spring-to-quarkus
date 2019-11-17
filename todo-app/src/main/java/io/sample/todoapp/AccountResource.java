package io.sample.todoapp;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api/me")
@Authenticated
public class AccountResource {

    @Inject SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("ROLE_USER")
    public String me() {
        return identity.getPrincipal().getName();
    }
}
