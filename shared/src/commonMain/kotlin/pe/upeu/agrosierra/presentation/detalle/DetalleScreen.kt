package pe.upeu.agrosierra.presentation.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.upeu.agrosierra.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(loteId: Long, volver: () -> Unit) {
    val viewModel: DetalleViewModel = koinViewModel(
        key = "lote-" + loteId,
        parameters = { parametersOf(loteId) }
    )
    val ui by viewModel.ui.collectAsState()
    var editando by remember { mutableStateOf<Labor?>(null) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    val puedeCrear = ui.sesion?.puede(Privilegios.LABOR_CREAR) == true
    val puedeEditar = ui.sesion?.puede(Privilegios.LABOR_EDITAR) == true
    val puedeEliminar = ui.sesion?.puede(Privilegios.LABOR_ELIMINAR) == true
    val puedeCerrar = ui.sesion?.puede(Privilegios.CAMPANIA_CERRAR) == true
    val colores = MaterialTheme.colorScheme

    LaunchedEffect(ui.cerrarFormulario) {
        if (ui.cerrarFormulario) {
            mostrarFormulario = false
            editando = null
            viewModel.formularioCerrado()
        }
    }

    Scaffold(
        containerColor = colores.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colores.primary,
                    titleContentColor = colores.onPrimary,
                    navigationIconContentColor = colores.onPrimary,
                    actionIconContentColor = colores.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.Default.ArrowBack, "Volver al padrón")
                    }
                },
                title = {
                    Column {
                        Text(
                            ui.lote?.nombre ?: "Detalle del lote",
                            style = MaterialTheme.typography.titleLarge
                        )
                        ui.lote?.let {
                            Text(
                                it.codigo + " · " + it.valle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colores.onPrimary.copy(alpha = 0.78f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::sincronizar,
                        enabled = !ui.procesando && !ui.recursoNoEncontrado
                    ) {
                        Icon(Icons.Default.Sync, "Sincronizar")
                    }
                }
            )
        },
        floatingActionButton = {
            if (
                puedeCrear &&
                ui.lote?.campaniaCerrada == false &&
                !ui.recursoNoEncontrado
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editando = null
                        viewModel.prepararFormulario()
                        mostrarFormulario = true
                    },
                    icon = {
                        Icon(Icons.Default.Add, contentDescription = null)
                    },
                    text = { Text("Nueva labor") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                ui.cargando -> EstadoCargaDetalle()
                ui.recursoNoEncontrado -> EstadoNoEncontrado(
                    mensaje = ui.error ?: "El lote ya no existe",
                    volver = volver
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 16.dp,
                        end = 16.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ui.lote?.let { lote ->
                            ResumenLote(
                                lote = lote,
                                puedeCerrar = puedeCerrar,
                                procesando = ui.procesando,
                                cerrarCampania = viewModel::cerrarCampania
                            )
                        }
                    }

                    ui.mensaje?.let { mensaje ->
                        item {
                            Aviso(
                                texto = mensaje,
                                error = false,
                                cerrar = viewModel::limpiarAviso
                            )
                        }
                    }
                    ui.error?.let { error ->
                        item {
                            Aviso(
                                texto = error,
                                error = true,
                                cerrar = viewModel::limpiarAviso
                            )
                        }
                    }

                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Historial de labores",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    ui.labores.size.toString() +
                                        " registro(s) en este lote",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colores.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = colores.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Icon(
                                    Icons.Default.FormatListBulleted,
                                    contentDescription = null,
                                    modifier = Modifier.padding(9.dp),
                                    tint = colores.primary
                                )
                            }
                        }
                    }

                    if (ui.labores.isEmpty()) {
                        item { EstadoSinLabores() }
                    } else {
                        items(ui.labores, key = { it.id }) { labor ->
                            LaborCard(
                                labor = labor,
                                editar = if (
                                    puedeEditar && labor.puedeEditar
                                ) ({
                                    editando = labor
                                    viewModel.prepararFormulario()
                                    mostrarFormulario = true
                                }) else null,
                                eliminar = if (puedeEliminar) ({
                                    viewModel.eliminar(labor)
                                }) else null
                            )
                        }
                    }
                }
            }

            if (ui.procesando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter)
                )
            }
        }
    }

    if (mostrarFormulario) {
        FormularioLabor(
            inicial = editando,
            maxFumigacion = ui.lote?.let {
                it.dosisMaximaPorHectarea * it.hectareas
            },
            procesando = ui.procesando,
            erroresServidor = ui.erroresCampos,
            errorGeneral = ui.error,
            cerrar = {
                mostrarFormulario = false
                viewModel.formularioCerrado()
            },
            guardar = { tipo, fecha, cantidad, observacion, jornales ->
                val original = editando
                if (original == null) {
                    viewModel.registrar(
                        tipo, fecha, cantidad, observacion, jornales
                    )
                } else {
                    viewModel.actualizar(
                        original, tipo, fecha, cantidad, observacion, jornales
                    )
                }
            }
        )
    }
}

@Composable
private fun EstadoCargaDetalle() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(14.dp))
        Text("Cargando lote y labores…")
    }
}

@Composable
private fun EstadoNoEncontrado(mensaje: String, volver: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(82.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = colores.errorContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = colores.error
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Recurso no encontrado", style = MaterialTheme.typography.titleLarge)
        Text(
            mensaje,
            color = colores.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Button(onClick = volver) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Volver al padrón actualizado")
        }
    }
}

@Composable
private fun ResumenLote(
    lote: Lote,
    puedeCerrar: Boolean,
    procesando: Boolean,
    cerrarCampania: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = colores.primaryContainer.copy(alpha = 0.72f)
        )
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        lote.codigo,
                        style = MaterialTheme.typography.labelLarge,
                        color = colores.primary
                    )
                    Text(
                        lote.nombre,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Surface(
                    color = if (lote.campaniaCerrada)
                        colores.surfaceVariant
                    else colores.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        if (lote.campaniaCerrada) "Cerrada" else "Activa",
                        modifier = Modifier.padding(
                            horizontal = 11.dp,
                            vertical = 7.dp
                        ),
                        color = if (lote.campaniaCerrada)
                            colores.onSurfaceVariant
                        else colores.onPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatoLote(
                    icono = Icons.Default.Grass,
                    etiqueta = "Cultivo",
                    valor = lote.cultivo,
                    modifier = Modifier.weight(1f)
                )
                DatoLote(
                    icono = Icons.Default.Landscape,
                    etiqueta = "Superficie",
                    valor = lote.hectareas.toString() + " ha",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatoLote(
                    icono = Icons.Default.LocationOn,
                    etiqueta = "Valle",
                    valor = lote.valle,
                    modifier = Modifier.weight(1f)
                )
                DatoLote(
                    icono = Icons.Default.Science,
                    etiqueta = "Dosis máxima",
                    valor = lote.dosisMaximaPorHectarea.toString() + "/ha",
                    modifier = Modifier.weight(1f)
                )
            }

            if (puedeCerrar && !lote.campaniaCerrada) {
                OutlinedButton(
                    onClick = cerrarCampania,
                    enabled = !procesando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar campaña")
                }
            }
        }
    }
}

@Composable
private fun DatoLote(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = colores.surface.copy(alpha = 0.72f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier.padding(11.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = colores.primary,
                modifier = Modifier.size(21.dp)
            )
            Column {
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant
                )
                Text(valor, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EstadoSinLabores() {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = colores.surfaceVariant.copy(alpha = 0.55f),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EventNote,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint = colores.primary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Todavía no hay labores",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Registra la primera labor de esta campaña.",
                color = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Aviso(texto: String, error: Boolean, cerrar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Surface(
        color = if (error) colores.errorContainer
        else colores.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (error) Icons.Default.ErrorOutline
                else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (error) colores.error else colores.primary
            )
            Text(texto, Modifier.weight(1f))
            IconButton(onClick = cerrar) {
                Icon(Icons.Default.Close, "Cerrar aviso")
            }
        }
    }
}

@Composable
private fun LaborCard(
    labor: Labor,
    editar: (() -> Unit)?,
    eliminar: (() -> Unit)?
) {
    val colores = MaterialTheme.colorScheme
    val icono = when (labor.tipo) {
        TipoLabor.RIEGO -> Icons.Default.WaterDrop
        TipoLabor.FUMIGACION -> Icons.Default.PestControl
        TipoLabor.COSECHA -> Icons.Default.Agriculture
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = colores.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icono,
                                contentDescription = null,
                                tint = colores.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            labor.tipo.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            labor.fecha,
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onSurfaceVariant
                        )
                    }
                }
                if (labor.pendiente) {
                    Surface(
                        color = colores.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colores.secondary
                            )
                            Text(
                                "Pendiente",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = colores.outline.copy(alpha = 0.14f))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DatoLabor(
                    "Cantidad",
                    labor.cantidad.toString(),
                    Modifier.weight(1f)
                )
                DatoLabor(
                    "Jornales",
                    labor.cantidadJornales.toString(),
                    Modifier.weight(1f)
                )
            }

            if (labor.observacion.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = colores.onSurfaceVariant
                    )
                    Text(
                        labor.observacion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = colores.onSurfaceVariant
                )
                Text(
                    "Responsable: " + labor.responsable,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant
                )
            }

            if (editar != null || eliminar != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    editar?.let {
                        TextButton(onClick = it) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Editar")
                        }
                    }
                    eliminar?.let {
                        TextButton(
                            onClick = it,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colores.error
                            )
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoLabor(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        color = colores.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                etiqueta,
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant
            )
            Text(valor, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun FormularioLabor(
    inicial: Labor?,
    maxFumigacion: Double?,
    procesando: Boolean,
    erroresServidor: Map<String, String>,
    errorGeneral: String?,
    cerrar: () -> Unit,
    guardar: (TipoLabor, String, Double, String, Int) -> Unit
) {
    var tipo by remember { mutableStateOf(inicial?.tipo ?: TipoLabor.RIEGO) }
    var fecha by remember { mutableStateOf(inicial?.fecha ?: "") }
    var cantidad by remember {
        mutableStateOf(inicial?.cantidad?.toString() ?: "")
    }
    var jornales by remember {
        mutableStateOf(inicial?.cantidadJornales?.toString() ?: "1")
    }
    var observacion by remember {
        mutableStateOf(inicial?.observacion ?: "")
    }
    var errorLocal by remember { mutableStateOf<String?>(null) }
    var errorCantidadLocal by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!procesando) cerrar() },
        title = {
            Text(if (inicial == null) "Registrar labor" else "Corregir labor")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Tipo de labor",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    TipoLabor.entries.forEach {
                        FilterChip(
                            selected = tipo == it,
                            onClick = { tipo = it },
                            leadingIcon = {
                                Icon(
                                    when (it) {
                                        TipoLabor.RIEGO ->
                                            Icons.Default.WaterDrop
                                        TipoLabor.FUMIGACION ->
                                            Icons.Default.PestControl
                                        TipoLabor.COSECHA ->
                                            Icons.Default.Agriculture
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp)
                                )
                            },
                            label = {
                                Text(
                                    it.name.lowercase()
                                        .replaceFirstChar { char ->
                                            char.uppercase()
                                        }
                                )
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    leadingIcon = {
                        Icon(Icons.Default.CalendarMonth, null)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    isError = erroresServidor["fecha"] != null,
                    supportingText = {
                        erroresServidor["fecha"]?.let { Text(it) }
                    }
                )
                val errorCantidad =
                    erroresServidor["cantidad"] ?: errorCantidadLocal
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = {
                        cantidad = it
                        errorCantidadLocal = null
                    },
                    label = { Text("Cantidad aplicada") },
                    leadingIcon = {
                        Icon(Icons.Default.Scale, null)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorCantidad != null,
                    supportingText = {
                        errorCantidad?.let { Text(it) }
                    }
                )
                OutlinedTextField(
                    value = jornales,
                    onValueChange = { jornales = it },
                    label = { Text("Cantidad de jornales") },
                    leadingIcon = {
                        Icon(Icons.Default.Groups, null)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    isError = erroresServidor["cantidadJornales"] != null,
                    supportingText = {
                        erroresServidor["cantidadJornales"]?.let { Text(it) }
                    }
                )
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it },
                    label = { Text("Observación") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, null)
                    },
                    minLines = 2,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
                errorLocal?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (erroresServidor.isEmpty()) {
                    errorGeneral?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !procesando,
                onClick = {
                    val numero = cantidad.toDoubleOrNull()
                    val numeroJornales = jornales.toIntOrNull()
                    when {
                        fecha.isBlank() || numero == null || numero <= 0 ->
                            errorLocal =
                                "Completa una fecha y una cantidad válida"
                        numeroJornales == null || numeroJornales <= 0 ->
                            errorLocal =
                                "La cantidad de jornales debe ser mayor que cero"
                        tipo == TipoLabor.FUMIGACION &&
                            maxFumigacion != null &&
                            numero > maxFumigacion ->
                            errorCantidadLocal =
                                "Máximo permitido: " + maxFumigacion
                        else -> guardar(
                            tipo,
                            fecha,
                            numero,
                            observacion,
                            numeroJornales
                        )
                    }
                }
            ) {
                Text(if (procesando) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = cerrar, enabled = !procesando) {
                Text("Cancelar")
            }
        }
    )
}
