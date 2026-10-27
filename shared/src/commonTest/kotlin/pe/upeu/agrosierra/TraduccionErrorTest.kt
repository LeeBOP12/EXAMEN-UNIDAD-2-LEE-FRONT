package pe.upeu.agrosierra

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import pe.upeu.agrosierra.data.remote.dto.ErrorDto
import pe.upeu.agrosierra.data.remote.traducirError
import pe.upeu.agrosierra.domain.model.AgroException

class TraduccionErrorTest {
    @Test
    fun traduce_error_400_y_conserva_validacion_de_cantidad() {
        val error = ErrorDto(
            status = 400,
            message = "Dosis excedida",
            validationErrors = mapOf("cantidad" to "Máximo 9.0")
        )

        val traducido = traducirError(400, error)

        val validacion = assertIs<AgroException.Validacion>(traducido)
        assertEquals("Máximo 9.0", validacion.campos["cantidad"])
    }
}
