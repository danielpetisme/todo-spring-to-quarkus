package io.sample.todoapp;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.validation.constraints.NotNull;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.get;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


@Testcontainers
@SpringBootTest(classes = {TodoApplication.class, TestSecurityConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles({"test"})
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration")
public class TodoResourceIT {

    @Container
    public static final PostgreSQLContainer DATABASE = new PostgreSQLContainer<>()
        .withDatabaseName("todo")
        .withUsername("todo")
        .withPassword("todo")
        .withExposedPorts(5432)
        .withCreateContainerCmdModifier(cmd ->
            cmd
                .withHostName("localhost")
                .withPortBindings(new PortBinding(Ports.Binding.bindPort(5432), new ExposedPort(5432)))
        );

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
            MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
        );

        RestAssured.requestSpecification = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .build();
    }

    @Test
    @Order(1)
    @WithMockUser(authorities = {"ROLE_USER"})
    void testInitialItems() {
        List<Todo> todos =
            given()
                .when()
                .get("/api")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .extract().body().as(getTodoTypeRef());
        assertEquals(4, todos.size());

        get("/api/1")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body("title", is("Introduction to Quarkus"))
            .body("completed", is(true));
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = {"ROLE_USER"})
    void testAddingAnItem() {
        Todo todo = new Todo();
        todo.setTitle("testing the application");

        given()
            .body(todo)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/api")
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body("title", is(todo.getTitle()))
            .body("completed", is(false))
            .body("id", is(5));

        List<Todo> todos = get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .extract().body().as(getTodoTypeRef());
        assertEquals(5, todos.size());

    }

    @Test
    @Order(3)
    @WithMockUser(authorities = {"ROLE_USER"})
    void testUpdatingAnItem() {
        Todo todo = new Todo();
        todo.setTitle("testing the application (updated)");
        todo.setCompleted(true);
        given()
            .body(todo)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .when()
            .patch("/api/{id}", 5)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body("title", is(todo.getTitle()))
            .body("completed", is(true))
            .body("id", is(5));
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = {"ROLE_USER"})
    void testDeletingAnItem() {
        given()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .when()
            .delete("/api/{id}", 5)
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        List<Todo> todos = get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .extract().body().as(getTodoTypeRef());
        assertEquals(4, todos.size());
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = {"ROLE_USER", "ROLE_ADMIN"})
    void testDeleteCompleted() {
        given()
            .when()
            .delete("/api")
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        List<Todo> todos = get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .extract().body().as(getTodoTypeRef());
        assertEquals(3, todos.size());
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = {"ROLE_USER"})
    void testDeleteCompletedForbiddenForUser() {
        given()
            .when()
            .delete("/api")
            .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @NotNull
    private TypeRef<List<Todo>> getTodoTypeRef() {
        return new TypeRef<List<Todo>>() {
            // Kept empty on purpose
        };
    }

}
