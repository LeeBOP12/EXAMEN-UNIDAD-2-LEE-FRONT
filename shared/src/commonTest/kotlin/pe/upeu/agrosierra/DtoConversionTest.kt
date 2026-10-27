package pe.upeu.agrosierra

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import pe.upeu.agrosierra.data.remote.AgroApi
import pe.upeu.agrosierra.data.remote.crearClienteHttp

class DtoConversionTest {
    @Test
    fun consume_pagina_simulada_y_convierte_dto_al_dominio() = runTest {
        val motor = MockEngine {
            respond(
                content = """{
                    "contenido":[{
                        "id":8,"codigo":"VAL-008","nombre":"Pampa","hectareas":4.2,
                        "cultivo":"Papa","valle":"Valle Azul","campaniaCerrada":false,
                        "dosisMaximaPorHectarea":1.5
                    }],
                    "pagina":0,"tamanio":20,"totalElementos":1,"totalPaginas":1,"ultima":true
                }""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearClienteHttp("https://agro.test/api/v1/", { null }, motor)
        val api = AgroApi(cliente) { }

        val lote = api.lotes().single().aDominio()

        assertEquals(8L, lote.id)
        assertEquals("VAL-008", lote.codigo)
        assertEquals(4.2, lote.hectareas)
        assertEquals(false, lote.campaniaCerrada)
        cliente.close()
    }
}
