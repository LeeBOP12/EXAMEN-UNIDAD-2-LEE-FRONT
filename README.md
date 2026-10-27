# EXAMEN-U2-LEE-ORBEZO - AgroCampo

Aplicación Android KMP/Compose para gestionar lotes y labores de AgroSierra. La interfaz trabaja offline-first: observa SQLDelight y sincroniza con AgroAPI mediante un único cliente Ktor.

## Configuración

1. Abrir este directorio en Android Studio.
2. Ejecutar **Sync Now**.
3. Iniciar AgroAPI en `http://localhost:8080`.
4. Ejecutar `androidApp` en el emulador.
5. La URL Android es `http://10.0.2.2:8080/api/v1/`.
6. Ingresar con un usuario semilla y la clave `Agro2026*`.

SQLDelight aplica `1.sqm` para actualizar una instalación existente.

## Capas

- `domain/model`: modelos y errores tipados.
- `domain/repository`: contratos.
- `domain/usecase`: login, registro, filtros y sincronización.
- `data/remote`: Ktor, DTO, paginación y traducción HTTP.
- `data/local`: SQLDelight y almacenamiento seguro.
- `data/repository`: sesión y offline-first.
- `presentation`: sesión, padrón, detalle, perfil y navegación.
- `di`: módulos Koin.

## Seguridad y sesión

- El token se guarda con `EncryptedSharedPreferences` y Android Keystore.
- `Authorization: Bearer` se agrega en un único `defaultRequest`.
- Al reabrir, `/usuarios/me` valida la sesión; sin red se conserva el acceso offline.
- Un 401 borra token y sesión; un 403 mantiene la sesión.
- La UI respeta privilegios y el backend conserva la autoridad final.

## Offline-first

1. La UI observa SQLDelight mediante Flow.
2. Un registro offline recibe ID negativo y marca CREAR.
3. Actualizaciones y eliminaciones quedan pendientes.
4. La sincronización sube pendientes antes de descargar.
5. El ID temporal se reemplaza por el definitivo en una transacción.
6. El padrón muestra pendientes por lote desde una consulta observable.
7. Existe sincronización automática y manual.

## Estados y errores

Las pantallas distinguen carga, contenido, vacío, error y sincronización. Se traducen validación, 401, 403, 404, 409, servidor, timeout y falta de red. Los errores de campo se muestran bajo el control correspondiente. Ante 404 se actualiza el padrón y no se ofrece reintentar el recurso.

## Roles

- TECNICO: registra y corrige sus labores autorizadas.
- JEFE_CAMPO: registra, corrige, elimina y cierra campañas.
- ADMINISTRADOR: consulta y gestiona usuarios.
- SUPERVISOR_CALIDAD: consulta y corrige cualquier labor, pero no cierra campañas.

## Solicitudes de cambio

- SC-A: filtro `Solo campañas abiertas`, combinado con valle y búsqueda en un caso de uso local.
- SC-B: soporte del rol SUPERVISOR_CALIDAD mediante privilegios del API.
- SC-C: `cantidadJornales` recorre request, DTO, dominio, SQLDelight, repositorio y UI.
- SC-D: distintivo de pendientes por lote desde una consulta SQLDelight observable.

## commonTest

Se incluyen pruebas para conversión DTO, errores, registro offline y orden de sincronización. El estudiante debe ejecutar y capturar el resultado desde Android Studio.

## Trazabilidad con la rúbrica

| Criterio | Evidencia en el código |
|---|---|
| Cliente Ktor único | `data/remote/ClienteHttp.kt` y `di/Modulos.kt` |
| DTO separados del dominio | `data/remote/dto/Dto.kt` y `domain/model` |
| CRUD y estados UI | `CampoRepositorioOffline.kt`, `DetalleViewModel.kt` y `DetalleScreen.kt` |
| Errores tipados | `ErrorApi.kt`, `MapeoErrores.kt` y estados de presentación |
| Sesión JWT segura | `SesionRepositorioImpl.kt`, `PlataformaLocal.android.kt` y `App.kt` |
| Authorization centralizado | `ClienteHttp.kt` mediante `defaultRequest` |
| Offline-first | `AgroDb.sq`, `BaseLocal.kt`, `CampoRepositorioOffline.kt` y `Sincronizador.kt` |
| SC-A | `FiltrarLotesUseCase.kt` y `LotesViewModel.kt` |
| SC-B | acciones gobernadas por privilegios en detalle y perfil |
| SC-C | DTO, dominio, migración `1.sqm`, repositorio, formulario y tarjeta |
| SC-D | consulta `observarPendientesPorLote` combinada con Flow en `BaseLocal.kt` |
| commonTest | conversión, errores, offline, sincronización y filtros en `shared/src/commonTest` |

Las pruebas deben ejecutarse y capturarse por el estudiante. El README no presenta como exitoso un resultado que todavía no fue ejecutado.
