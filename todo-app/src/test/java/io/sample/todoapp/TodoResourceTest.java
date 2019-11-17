package io.sample.todoapp;

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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import javax.validation.constraints.NotNull;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.is;

@SpringBootTest(classes = TodoApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TodoResourceTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private Validator validator;

    static {
        RestAssured.requestSpecification = new RequestSpecBuilder()
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .build();
    }

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(
            MockMvcBuilders.standaloneSetup(new TodoResource(todoRepository))
                .setMessageConverters(jacksonMessageConverter)
                .setValidator(validator)
        );
    }

    @Test
    @Order(1)
    void testInitialItems() {
        List<Todo> todos = get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .extract().body().as(getTodoTypeRef());
        assertEquals(4, todos.size());

        get("/api/1").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body("title", is("Introduction to Quarkus"))
            .body("completed", is(true));
    }

    @Test
    @Order(2)
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
    void testDeleteCompleted() {
        delete("/api")
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        List<Todo> todos = get("/api").then()
            .statusCode(HttpStatus.SC_OK)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .extract().body().as(getTodoTypeRef());
        assertEquals(3, todos.size());
    }

    @NotNull
    private TypeRef<List<Todo>> getTodoTypeRef() {
        return new TypeRef<List<Todo>>() {
            // Kept empty on purpose
        };
    }

}
