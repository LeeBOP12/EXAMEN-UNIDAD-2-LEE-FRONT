package pe.upeu.agrosierra.presentation.listado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import pe.upeu.agrosierra.data.local.formatearInstante
import pe.upeu.agrosierra.domain.model.Lote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotesScreen(
    abrirLote: (Long) -> Unit,
    abrirPerfil: () -> Unit,
    viewModel: LotesViewModel = koinViewModel()
) {
    val ui by viewModel.ui.collectAsState()
    val colores = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colores.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colores.primary,
                    titleContentColor = colores.onPrimary,
                    actionIconContentColor = colores.onPrimary
                ),
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = MaterialTheme.shapes.small,
                            color = colores.onPrimary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Agriculture,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "AgroCampo",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                "Padrón de lotes",
                                style = MaterialTheme.typography.bodySmall,
                                color = colores.onPrimary.copy(alpha = 0.78f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::sincronizar,
                        enabled = !ui.sincronizando
                    ) {
                        Icon(Icons.Default.Sync, "Sincronizar")
                    }
                    IconButton(onClick = abrirPerfil) {
                        Icon(Icons.Default.AccountCircle, "Mi perfil")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = colores.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Campañas agrícolas",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                if (ui.sincronizando) "Actualizando información…"
                                else ui.ultimaSincronizacion?.let {
                                    "Actualizado: " + formatearInstante(it)
                                } ?: "Pendiente de primera sincronización",
                                style = MaterialTheme.typography.bodySmall,
                                color = colores.onSurfaceVariant
                            )
                        }
                        if (ui.sincronizando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = ui.busqueda,
                        onValueChange = viewModel::buscar,
                        placeholder = { Text("Código, nombre o cultivo") },
                        label = { Text("Buscar lote") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (ui.busqueda.isNotBlank()) {
                                IconButton(onClick = { viewModel.buscar("") }) {
                                    Icon(Icons.Default.Close, "Limpiar búsqueda")
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = ui.soloAbiertas,
                            onClick = viewModel::alternarSoloAbiertas,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            label = { Text("Solo abiertas") }
                        )
                        FilterChip(
                            selected = ui.valle.isBlank(),
                            onClick = { viewModel.filtrarValle("") },
                            label = { Text("Todos los valles") }
                        )
                        ui.vallesDisponibles.forEach { valle ->
                            FilterChip(
                                selected = ui.valle == valle,
                                onClick = { viewModel.filtrarValle(valle) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                label = { Text(valle) }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ui.resumenSincronizacion?.let {
                    Surface(
                        color = colores.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = colores.primary
                            )
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colores.onPrimaryContainer
                            )
                        }
                    }
                }

                ui.error?.let {
                    Surface(
                        color = colores.errorContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = colores.error
                            )
                            Text(
                                it,
                                modifier = Modifier.weight(1f),
                                color = colores.onErrorContainer
                            )
                            TextButton(onClick = viewModel::sincronizar) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                when {
                    ui.cargando -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    ui.vacio -> EstadoVacio()
                    else -> {
                        Text(
                            ui.lotes.size.toString() + " lote(s) encontrados",
                            style = MaterialTheme.typography.labelLarge,
                            color = colores.onSurfaceVariant
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(ui.lotes, key = { it.id }) { lote ->
                                LoteCard(lote) { abrirLote(lote.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio() {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(82.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = colores.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    tint = colores.primary
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("No encontramos lotes", style = MaterialTheme.typography.titleLarge)
        Text(
            "Prueba cambiando la búsqueda o los filtros.",
            color = colores.onSurfaceVariant
        )
    }
}

@Composable
private fun LoteCard(lote: Lote, abrir: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = abrir),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(7.dp)
                    .heightIn(min = 154.dp)
                    .background(
                        if (lote.campaniaCerrada) colores.outline
                        else colores.primary
                    )
            )
            Column(
                Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Abrir lote",
                        tint = colores.onSurfaceVariant
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Grass,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = colores.secondary
                    )
                    Text(
                        lote.cultivo + " · " + lote.hectareas + " ha",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                        tint = colores.secondary
                    )
                    Text(
                        lote.valle,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (lote.campaniaCerrada)
                            colores.surfaceVariant
                        else colores.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            if (lote.campaniaCerrada) "Campaña cerrada"
                            else "Campaña activa",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 6.dp
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (lote.pendientesSincronizar > 0) {
                        Surface(
                            color = colores.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
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
                                    lote.pendientesSincronizar.toString() +
                                        " pendiente(s)",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
