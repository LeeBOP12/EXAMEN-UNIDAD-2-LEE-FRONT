package pe.upeu.agrosierra.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val VerdeBosque = Color(0xFF245B3B)
private val VerdeHoja = Color(0xFF3F7652)
private val VerdeSuave = Color(0xFFDDEBDF)
private val Tierra = Color(0xFF8A5C32)
private val Dorado = Color(0xFFC8892D)
private val FondoClaro = Color(0xFFF7F9F3)
private val SuperficieClara = Color(0xFFFFFEF8)
private val TextoOscuro = Color(0xFF172019)

private val Claro = lightColorScheme(
    primary = VerdeBosque,
    onPrimary = Color.White,
    primaryContainer = VerdeSuave,
    onPrimaryContainer = Color(0xFF0B2B19),
    secondary = Tierra,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDDBE),
    onSecondaryContainer = Color(0xFF2D1604),
    tertiary = Dorado,
    onTertiary = Color.White,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro,
    surfaceVariant = Color(0xFFE4E9E1),
    onSurfaceVariant = Color(0xFF424942),
    outline = Color(0xFF737A72),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFFFDAD6)
)

private val Oscuro = darkColorScheme(
    primary = Color(0xFFA4D5AE),
    onPrimary = Color(0xFF0C3921),
    primaryContainer = Color(0xFF174B2E),
    onPrimaryContainer = Color(0xFFC0F1CA),
    secondary = Color(0xFFF1BC8C),
    onSecondary = Color(0xFF4C2A0D),
    secondaryContainer = Color(0xFF684121),
    tertiary = Color(0xFFFFB95F),
    background = Color(0xFF101510),
    onBackground = Color(0xFFE1E9DF),
    surface = Color(0xFF171C17),
    onSurface = Color(0xFFE1E9DF),
    surfaceVariant = Color(0xFF3F4840),
    onSurfaceVariant = Color(0xFFBFC9BF),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A)
)

private val AgroTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 23.sp
    ),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    )
)

private val AgroShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AgroTema(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        typography = AgroTypography,
        shapes = AgroShapes,
        content = contenido
    )
}
