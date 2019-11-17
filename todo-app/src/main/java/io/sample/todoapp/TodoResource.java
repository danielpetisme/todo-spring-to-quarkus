package io.sample.todoapp;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.springframework.data.domain.Sort;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
public class TodoResource {

    private final TodoRepository todoRepository;

    @Inject
    public TodoResource(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @OPTIONS
    public Response opt() {
        return Response.ok().build();
    }

    @GET
    @RolesAllowed({ "ROLE_USER" })
    public List<Todo> getAll() {
        return todoRepository.findAll(Sort.by("order"));
    }

    @GET
    @Path("/{id}")
    public Todo getOne(@PathParam Long id) {
        return todoRepository.findById(id)
            .orElseThrow(
                () -> new WebApplicationException("Todo with id of " + id + " does not exist.", NOT_FOUND));
    }

    @POST
    @Transactional
    public Response create(@Valid Todo todo) {
        Todo result = todoRepository.save(todo);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Todo update(@Valid Todo todo, @PathParam Long id) {
        Todo entity = todoRepository.findById(id)
            .orElseThrow(
                () -> new WebApplicationException("Todo with id of " + id + " does not exist.", NOT_FOUND));
        entity.setId(id);
        entity.setCompleted(todo.isCompleted());
        entity.setOrder(todo.getOrder());
        entity.setTitle(todo.getTitle());
        entity.setUrl(todo.getUrl());
        return todoRepository.save(entity);
    }

    @DELETE
    @Transactional
    public Response deleteCompleted() {
        todoRepository.deleteCompleted();
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteOne(@PathParam Long id) {
        Todo entity = todoRepository.findById(id)
            .orElseThrow(
                () -> new WebApplicationException("Todo with id of " + id + " does not exist.", NOT_FOUND));
        todoRepository.delete(entity);
        return Response.noContent().build();
    }
}
