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
class ControladoresIntegracionTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }
    
    @Test
    void publicarPregunta_CaminoFeliz_DebeRetornar201() {
        String payloadNuevaPregunta = """
            {
                "titulo": "Error en mapeo JPA JOINED",
                "contenido": "No se heredan de forma correcta los atributos de la clase abstracta Publicacion.",
                "etiquetaIds": [1, 2, 3]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer MOCK_JWT_TOKEN") // Simula el pase por el API Gateway
            .body(payloadNuevaPregunta)
        .when()
            .post("/preguntas")
        .then()
            .statusCode(201) // 201 Created según el flujo del CU3
            .body("id", notNullValue())
            .body("titulo", equalTo("Error en mapeo JPA JOINED"));
    }

    @Test
    void publicarPregunta_CaminoInvalido_DatosVacios_DebeRetornar400() {
        String payloadInvalido = """
            {
                "titulo": "",
                "contenido": "Corto",
                "etiquetaIds": []
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payloadInvalido)
        .when()
            .post("/preguntas")
        .then()
            .statusCode(400); // Bad Request gatillado por Jakarta Validation (@NotNull/@Size)
    }

    @Test
    void emitirVoto_CaminoFeliz_DebeRetornar200() {
        String payloadVoto = """
            {
                "respuestaId": 45,
                "valor": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer MOCK_JWT_TOKEN")
            .body(payloadVoto)
        .when()
            .post("/votos")
        .then()
            .statusCode(200) // 200 OK reflejando la respuesta síncrona del CU1
            .body("nuevoScore", notNullValue());
    }
}
