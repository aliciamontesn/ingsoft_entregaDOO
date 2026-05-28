package com.grupok.publicaciones.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PreguntaIntegracionTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void publicarPregunta_CuandoDatosSonValidos_DebeRetornar201Created() {
        String payloadPreguntaValida = """
            {
                "titulo": "Error en herencia JPA con estrategia JOINED",
                "contenido": "No se están persistiendo correctamente los atributos específicos en la tabla hijo de PostgreSQL.",
                "etiquetaIds": [1, 2]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer MOCK_JWT_TOKEN_VALIDO") 
            .body(payloadPreguntaValida)
        .when()
            .post("/preguntas")
        .then()
            .statusCode(201) 
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("titulo", equalTo("Error en herencia JPA con estrategia JOINED"));
    }

    @Test
    void publicarPregunta_CuandoTituloOContenidoSonVacios_DebeRetornar400BadRequest() {
        String payloadPreguntaInvalida = """
            {
                "titulo": "",
                "contenido": "Muy corto",
                "etiquetaIds": []
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payloadPreguntaInvalida)
        .when()
            .post("/preguntas")
        .then()
            .statusCode(400);
    }
}
