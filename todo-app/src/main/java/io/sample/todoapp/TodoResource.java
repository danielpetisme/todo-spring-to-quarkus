package io.sample.todoapp;

import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path(value = "/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TodoResource {

    @OPTIONS
    public Response opt() {
        return Response.ok().build();
    }

    @GET
    @RolesAllowed("ROLE_USER")
    public List<Todo> getAll() {
        return Todo.listAll(Sort.by("order"));
    }

    @GET
    @Path("/{id}")
    public Todo getOne(@PathParam Long id) {
        Todo todo = Todo.findById(id);
        if (todo == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", NOT_FOUND);
        }
        return todo;
    }

    @POST
    @Transactional
    @Counted
    public Response create(@Valid Todo todo) {
        todo.persist();
        return Response.status(Response.Status.CREATED).entity(todo).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    @Metered
    public Todo update(@Valid Todo todo, @PathParam Long id) {
        Todo entity = Todo.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Todo with id of " + id + " does not exist.", NOT_FOUND);
        }
        entity.completed = todo.completed;
        entity.order = todo.order;
        entity.title = todo.title;
        entity.url = todo.url;
        entity.persist();
        return entity;
    }

    @DELETE
    @Transactional
    @RolesAllowed("ROLE_ADMIN")
    public Response deleteCompleted() {
        Todo.deleteCompleted();
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteOne(@PathParam Long id) {
        Todo entity = Todo.find(id)
            .orElseThrow(
                () -> new WebApplicationException("Todo with id of " + id + " does not exist.", NOT_FOUND));
        entity.delete();
        return Response.noContent().build();
    }
}
