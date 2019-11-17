package io.sample.todoapp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TodoResourceTest {

    @Test
    @Order(1)
    void testInitialItems() {
        List<Todo> todos =
            given().auth().preemptive().basic("admin", "admin")
                .get("/api").then()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .extract().body().as(getTodoTypeRef());
        Assertions.assertEquals(4, todos.size());

        given().auth().preemptive().basic("admin", "admin")
            .get("/api/1").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .body("title", is("Introduction to Quarkus"))
            .body("completed", is(true));
    }

    @Test
    @Order(2)
    void testAddingAnItem() {
        Todo todo = new Todo();
        todo.title = "testing the application";
        given()
            .auth().preemptive().basic("admin", "admin")
            .body(todo)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .when()
            .post("/api")
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .body("title", is(todo.title))
            .body("completed", is(false))
            .body("id", is(5));

        List<Todo> todos = given().auth().preemptive().basic("admin", "admin")
            .get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .extract().body().as(getTodoTypeRef());
        Assertions.assertEquals(5, todos.size());

    }

    @Test
    @Order(3)
    void testUpdatingAnItem() {
        Todo todo = new Todo();
        todo.title ="testing the application (updated)";
        todo.completed = true;
        given()
            .auth().preemptive().basic("admin", "admin")
            .body(todo)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .when()
            .patch("/api/{id}", 5)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .body("title", is(todo.title))
            .body("completed", is(true))
            .body("id", is(5));
    }

    @Test
    @Order(4)
    void testDeletingAnItem() {
        given()
            .auth().preemptive().basic("admin", "admin")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .when()
            .delete("/api/{id}", 5)
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        List<Todo> todos = given().auth().preemptive().basic("admin", "admin")
            .get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .extract().body().as(getTodoTypeRef());
        Assertions.assertEquals(4, todos.size());
    }

    @Test
    @Order(5)
    void testDeleteCompleted() {
        given().auth().preemptive().basic("admin", "admin")
            .delete("/api")
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        List<Todo> todos = given().auth().preemptive().basic("admin", "admin")
            .get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .extract().body().as(getTodoTypeRef());
        Assertions.assertEquals(3, todos.size());
    }

    private TypeRef<List<Todo>> getTodoTypeRef() {
        return new TypeRef<List<Todo>>() {
            // Kept empty on purpose
        };
    }

}
