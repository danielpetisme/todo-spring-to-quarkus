package io.sample.todoapp;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@Testcontainers
@SpringBootTest(classes = {TodoApplication.class, TestSecurityConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
public class AccountResourceTest {

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
    FilterChainProxy springSecurityFilterChain;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.standaloneSetup(
            MockMvcBuilders.standaloneSetup(new AccountResource())
                .apply(springSecurity(springSecurityFilterChain))
        );
    }

    @Test
    @Order(1)
    @WithMockUser(username = "johndoe")
    void testAccountName() {
        given()
            .when()
            .get("/api/me")
            .then()
            .statusCode(HttpStatus.SC_OK)
//            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .body(is("johndoe"));
    }
}
