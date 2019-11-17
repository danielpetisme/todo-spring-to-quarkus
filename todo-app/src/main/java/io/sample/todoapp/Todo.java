package io.sample.todoapp;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Entity
public class Todo extends PanacheEntity {

    @NotBlank
    @Column(unique = true)
    public String title;

    public boolean completed;

    @Column(name = "ordering")
    public int order;

    public String url;

    public static void deleteCompleted() {
        delete("completed", true);
    }

    public static Optional<Todo> find(Long id) {
        Todo todo = findById(id);
        return Optional.ofNullable(todo);
    }

    @Override
    public String toString() {
        return "Todo{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", completed=" + completed +
            ", order=" + order +
            ", url='" + url + '\'' +
            '}';
    }
}
