package es.codeurjc.web.nitflex.rest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import es.codeurjc.web.nitflex.model.User;
import es.codeurjc.web.nitflex.repository.UserRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class APIRestTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() throws Exception {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
        RestAssured.basePath = "/api";
        testUser = new User("testuser", "testuser@example.com");
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    public void tearDown() throws Exception {
        userRepository.deleteAll();
    }

    // T4 EJ1
    @Test
    public void whenCreateFilmNoImage_thenGetFilmById() {
        // Define new movie without image
        String filmJson = """
                    {
                        "title": "Black Hawk Down",
                        "synopsis": "Octubre de 1993. Soldados americanos de élite son enviados a Mogadiscio.",
                        "releaseYear": 2001,
                        "ageRating": "+18"
                    }
                """;

        // Create the movie and get the ID
        Integer filmId = given()
                .contentType(ContentType.JSON)
                .body(filmJson)
                .when()
                .post("/films/")
                .then()
                .statusCode(201)
                .body("title", equalTo("Black Hawk Down")) // Verify Title
                .body("synopsis", containsString("Soldados americanos de élite")) // Verify synopsis
                .body("releaseYear", equalTo(2001))
                .body("ageRating", equalTo("+18"))
                .extract()
                .path("id"); // Get the ID

        // Get the movie and verify the data
        given()
                .pathParam("id", filmId)
                .when()
                .get("/films/{id}")
                .then()
                .statusCode(200) // Verificamos que se recupera con éxito (200)
                .body("title", equalTo("Black Hawk Down"))
                .body("synopsis", containsString("Soldados americanos de élite"))
                .body("releaseYear", equalTo(2001))
                .body("ageRating", equalTo("+18"));
    }
    @Test
    public void whenCreateFilmWithoutTitle_thenErrorMessage() {
        String filmJson = """
                    {
                        "synopsis": "Una historia sin título.",
                        "releaseYear": 2000,
                        "ageRating": "+12"
                    }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(filmJson)
                .when()
                .post("/films/")
                .then()
                .statusCode(400)  // Verificamos que se devuelve el código 400 (Bad Request)
                .body(equalTo("The title is empty"));  // Verificamos que el mensaje de error sea el esperado
    }

    @Test
    public void whenCreateAndDeleteFilm_thenFilmIsNotAvailable() {
        // Paso 1: Creación de película en formato JSON
        String filmJson = """
                    {
                        "title": "Jumanji",
                        "synopsis": "En los años 60 un niño descubre Jumanji, un misterioso juego de mesa. Al empezar una partida, desaparece tras caer en una determinada casilla. Tres décadas después, dos chavales reanudan la partida interrumpida. A medida que progresa el juego, animales selváticos de todo tipo salen del tablero invadiendo el mundo real. Para detener el caos deberán llevar la partida hasta el final, sobreviviendo a numerosos peligros; les ayudará el niño, ya crecido, que quedó atrapado en Jumanji",
                        "releaseYear": 2023,
                        "ageRating": "+7"
                    }
                """;

        // Uso el padre de int y long Integer para evitar problemas con los tipos
        Integer filmId = given()
                .contentType(ContentType.JSON)
                .body(filmJson)
                .when()
                .post("/films/")
                .then()
                .statusCode(201) // Verificamos que se creó correctamente con el código 201
                .body("title", equalTo("Jumanji")) // Verificamos que el título coincide
                .body("synopsis", containsString("un misterioso juego de mesa")) // Verificar parte de la sinopsis
                .body("releaseYear", equalTo(2023))
                .body("ageRating", equalTo("+7"))
                .extract()
                .path("id"); // Extraemos el ID de la película creada

        // Paso 2: Eliminar la película
        given()
                .pathParam("id", filmId)
                .when()
                .delete("/films/{id}")
                .then()
                .statusCode(204); // Verificamos que se eliminó (204)

        // Paso 3: Consultar la película y verificar que no existe
        given()
                .pathParam("id", filmId)
                .when()
                .get("/films/{id}")
                .then()
                .statusCode(404); // Verificamos que no se encuentra (404)
    }
@Test
public void whenCreateAndEditFilm_thenTitleIsUpdated() {
    // Paso 1: Creación de película en formato JSON
    String filmJson = """
                {
                    "title": "Jumanji",
                    "synopsis": "En los años 60 un niño descubre Jumanji, un misterioso juego de mesa. Al empezar una partida, desaparece tras caer en una determinada casilla. Tres décadas después, dos chavales reanudan la partida interrumpida. A medida que progresa el juego, animales selváticos de todo tipo salen del tablero invadiendo el mundo real. Para detener el caos deberán llevar la partida hasta el final, sobreviviendo a numerosos peligros; les ayudará el niño, ya crecido, que quedó atrapado en Jumanji",
                    "releaseYear": 2023,
                    "ageRating": "+7"
                }
            """;

    // Crear la película y obtener el ID
    Integer filmId = given()
            .contentType(ContentType.JSON)
            .body(filmJson)
            .when()
            .post("/films/")
            .then()
            .statusCode(201) // Verificamos que se creó correctamente con el código 201
            .body("title", equalTo("Jumanji")) // Verificamos que el título coincide
            .body("synopsis", containsString("un misterioso juego de mesa")) // Verificar parte de la sinopsis
            .body("releaseYear", equalTo(2023))
            .body("ageRating", equalTo("+7"))
            .extract()
            .path("id"); // Extraemos el ID de la película creada

    // Paso 2: Editar la película para añadir "- parte 2" en el título
    String updatedFilmJson = """
                {
                    "title": "Jumanji - parte 2",
                    "synopsis": "En los años 60 un niño descubre Jumanji, un misterioso juego de mesa. Al empezar una partida, desaparece tras caer en una determinada casilla. Tres décadas después, dos chavales reanudan la partida interrumpida. A medida que progresa el juego, animales selváticos de todo tipo salen del tablero invadiendo el mundo real. Para detener el caos deberán llevar la partida hasta el final, sobreviviendo a numerosos peligros; les ayudará el niño, ya crecido, que quedó atrapado en Jumanji",
                    "releaseYear": 2023,
                    "ageRating": "+7"
                }
            """;

    given()
            .contentType(ContentType.JSON)
            .body(updatedFilmJson)
            .pathParam("id", filmId)
            .when()
            .put("/films/{id}")
            .then()
            .statusCode(200) // Verificamos que se actualizó correctamente con el código 200
            .body("title", equalTo("Jumanji - parte 2")) // Verificamos que el título se ha actualizado
            .body("synopsis", containsString("un misterioso juego de mesa")) // Verificar parte de la sinopsis
            .body("releaseYear", equalTo(2023))
            .body("ageRating", equalTo("+7"));

    // Paso 3: Obtener la película y verificar que el título se ha actualizado
    given()
            .pathParam("id", filmId)
            .when()
            .get("/films/{id}")
            .then()
            .statusCode(200) // Verificamos que se recupera con éxito (200)
            .body("title", equalTo("Jumanji - parte 2")) // Verificamos que el título se ha actualizado
            .body("synopsis", containsString("un misterioso juego de mesa")) // Verificar parte de la sinopsis
            .body("releaseYear", equalTo(2023))
            .body("ageRating", equalTo("+7"));
    }
}