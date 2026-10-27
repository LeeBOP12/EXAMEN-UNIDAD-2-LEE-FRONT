package pe.upeu.agrosierra.presentation.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pe.upeu.agrosierra.domain.usecase.CerrarSesionUseCase
import pe.upeu.agrosierra.domain.repository.SesionRepositorio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(volver: () -> Unit) {
    val sesiones = koinInject<SesionRepositorio>()
    val cerrar = koinInject<CerrarSesionUseCase>()
    val sesion by sesiones.sesion.collectAsState(null)
    var confirmacion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val colores = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colores.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colores.primary,
                    titleContentColor = colores.onPrimary,
                    navigationIconContentColor = colores.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = volver) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                title = {
                    Column {
                        Text(
                            "Mi perfil",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Sesión y privilegios",
                            style = MaterialTheme.typography.bodySmall,
                            color = colores.onPrimary.copy(alpha = 0.78f)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sesion?.let { actual ->
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor =
                                colores.primaryContainer.copy(alpha = 0.72f)
                        )
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(92.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = colores.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        actual.nombre
                                            .trim()
                                            .split(" ")
                                            .take(2)
                                            .mapNotNull { it.firstOrNull() }
                                            .joinToString("")
                                            .uppercase(),
                                        style = MaterialTheme.typography
                                            .headlineMedium,
                                        color = colores.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                actual.nombre,
                                style = MaterialTheme.typography
                                    .headlineMedium
                            )
                            Text(
                                "@" + actual.usuario,
                                color = colores.onSurfaceVariant
                            )
                            Surface(
                                color = colores.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 7.dp
                                    ),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = colores.secondary
                                    )
                                    Text(
                                        actual.rol.replace("_", " "),
                                        style = MaterialTheme.typography
                                            .labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Privilegios asignados",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Estas acciones son informativas; AgroAPI valida " +
                                "cada operación nuevamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colores.onSurfaceVariant
                        )
                    }
                }

                if (actual.privilegios.isEmpty()) {
                    item {
                        Surface(
                            color = colores.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                "Este usuario no tiene privilegios asignados.",
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    }
                } else {
                    actual.privilegios.sorted().forEach { permiso ->
                        item(key = permiso) {
                            PrivilegioCard(permiso)
                        }
                    }
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = colores.outline.copy(alpha = 0.2f)
                    )
                    OutlinedButton(
                        onClick = { confirmacion = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = colores.error
                        )
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(Modifier.width(9.dp))
                        Text("Cerrar sesión")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Al cerrar sesión se eliminará el token cifrado " +
                            "almacenado en el dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colores.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (confirmacion) {
        AlertDialog(
            onDismissRequest = { confirmacion = false },
            icon = {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    tint = colores.error
                )
            },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text(
                    "Se eliminará el token seguro y volverás a la " +
                        "pantalla de acceso."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmacion = false
                        scope.launch { cerrar() }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colores.error,
                        contentColor = colores.onError
                    )
                ) {
                    Text("Cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PrivilegioCard(permiso: String) {
    val colores = MaterialTheme.colorScheme
    val (icono, titulo) = when (permiso) {
        "LOTE_LEER" -> Icons.Default.Map to "Consultar lotes"
        "LABOR_LEER" -> Icons.Default.Visibility to "Consultar labores"
        "LABOR_CREAR" -> Icons.Default.AddCircle to "Registrar labores"
        "LABOR_EDITAR" -> Icons.Default.Edit to "Corregir labores"
        "LABOR_ELIMINAR" -> Icons.Default.Delete to "Eliminar labores"
        "CAMPANIA_CERRAR" -> Icons.Default.Lock to "Cerrar campañas"
        "USUARIO_GESTIONAR" ->
            Icons.Default.ManageAccounts to "Gestionar usuarios"
        else -> Icons.Default.Security to permiso.replace("_", " ")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colores.surface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = MaterialTheme.shapes.small,
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
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    permiso,
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Permitido",
                tint = colores.primary
            )
        }
    }
}
