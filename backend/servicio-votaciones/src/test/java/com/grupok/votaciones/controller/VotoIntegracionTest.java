package com.grupok.votaciones.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VotoIntegracionTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void emitirVoto_CuandoDatosSonValidos_DebeRetornar200YContenerNuevoScore() {
        String payloadVotoValido = """
            {
                "respuestaId": 45,
                "valor": 1
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer MOCK_JWT_TOKEN_VALIDO") 
            .body(payloadVotoValido)
        .when()
            .post("/votos")
        .then()
            .statusCode(200) // Cambia a 201 si vuestro controlador devuelve Created
            .contentType(ContentType.JSON)
            .body("nuevoScore", notNullValue()); // Verifica que el JSON de respuesta tiene la clave esperada
    }

    @Test
    void emitirVoto_CuandoFaltanCamposObligatorios_DebeRetornar400BadRequest() {
        String payloadInvalido = "{}";

        given()
            .contentType(ContentType.JSON)
            .body(payloadInvalido)
        .when()
            .post("/votos")
        .then()
            .statusCode(400); // Valida que el controlador intercepte el error y responda Bad Request
    }
}
