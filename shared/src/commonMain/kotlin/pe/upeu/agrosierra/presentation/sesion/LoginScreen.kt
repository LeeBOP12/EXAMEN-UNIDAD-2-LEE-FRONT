package pe.upeu.agrosierra.presentation.sesion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val ui by viewModel.ui.collectAsState()
    var mostrarClave by remember { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        colores.primary.copy(alpha = 0.18f),
                        colores.background,
                        colores.secondaryContainer.copy(alpha = 0.35f)
                    )
                )
            )
            .padding(horizontal = 22.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier.widthIn(max = 460.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(88.dp),
                    shape = CircleShape,
                    color = colores.primaryContainer,
                    tonalElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = colores.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "AgroCampo",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colores.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Control de labores agrícolas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "AgroSierra · Trabajo conectado y offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colores.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = colores.outline.copy(alpha = 0.18f))

                OutlinedTextField(
                    value = ui.usuario,
                    onValueChange = viewModel::usuario,
                    label = { Text("Usuario") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ui.clave,
                    onValueChange = viewModel::clave,
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { mostrarClave = !mostrarClave }) {
                            Icon(
                                if (mostrarClave) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription =
                                    if (mostrarClave) "Ocultar contraseña"
                                    else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation =
                        if (mostrarClave) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                ui.error?.let {
                    Surface(
                        color = colores.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = colores.error
                            )
                            Text(
                                it,
                                color = colores.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Button(
                    onClick = viewModel::ingresar,
                    enabled = !ui.cargando &&
                        ui.usuario.isNotBlank() &&
                        ui.clave.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (ui.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = colores.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Verificando…")
                    } else {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Ingresar", fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = colores.primaryContainer.copy(alpha = 0.65f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = colores.primary
                        )
                        Column {
                            Text(
                                "Acceso de demostración",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                "tecnico1 / Agro2026*",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
