package io.sample.todoapp.health;

import io.sample.todoapp.Todo;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Readiness
public class MyBusinessCheck implements HealthCheck {
    @Override
    public HealthCheckResponse call() {
        long size = Todo.count("completed", false);
        return HealthCheckResponse.named("my-business-health-check").withData("size", size).up().build();
    }
}
