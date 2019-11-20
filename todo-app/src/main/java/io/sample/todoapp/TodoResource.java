package io.sample.todoapp;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping(value = "/api")
public class TodoResource {

  private final TodoRepository todoRepository;

  public TodoResource(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  @RequestMapping(method=RequestMethod.OPTIONS)
  public ResponseEntity opt() {
    return ResponseEntity.ok().build();
  }

  @GetMapping
  @ResponseBody
  @RolesAllowed({"ROLE_USER"})
  public List<Todo> getAll() {
    return todoRepository.findAll(Sort.by("order"));
  }

  @GetMapping("/{id}")
  @ResponseBody
  @RolesAllowed({"ROLE_USER"})
  public Todo getOne(@PathVariable Long id) {
    return todoRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with id of " + id + " does not exist."));
  }

  @PostMapping
  @Transactional
  @RolesAllowed({"ROLE_USER"})    
  public ResponseEntity<Todo> create(@Valid @RequestBody Todo todo) throws URISyntaxException {
    Todo result = todoRepository.save(todo);
    return ResponseEntity.created(new URI("/api/" + result.getId())).body(result);
  }

  @PatchMapping("/{id}")
  @Transactional
  @RolesAllowed({"ROLE_USER"})    
  public ResponseEntity<Todo> update(@Valid @RequestBody Todo todo, @PathVariable("id") Long id) {
    Todo entity = todoRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with id of " + id + " does not exist."));
      entity.setId(id);
      entity.setCompleted(todo.isCompleted());
      entity.setOrder(todo.getOrder());
      entity.setTitle(todo.getTitle());
      entity.setUrl(todo.getUrl());
      Todo result = todoRepository.save(entity);
      return ResponseEntity.ok(result);
  }

  @DeleteMapping
  @Transactional
  @RolesAllowed({"ROLE_ADMIN"})    
  public ResponseEntity<Void> deleteCompleted() {
    todoRepository.deleteCompleted();
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Transactional
  @RolesAllowed({"ROLE_USER"})    
  public ResponseEntity<Void> deleteOne(@PathVariable("id") Long id) {
    Todo entity = todoRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo with id of " + id + " does not exist."));
    todoRepository.delete(entity);
    return ResponseEntity.noContent().build();
  }
}
