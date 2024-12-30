import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import presentation.ui.theme.Typography
import presentation.ui.theme.md_theme_dark_background
import presentation.ui.theme.md_theme_dark_error
import presentation.ui.theme.md_theme_dark_errorContainer
import presentation.ui.theme.md_theme_dark_inverseOnSurface
import presentation.ui.theme.md_theme_dark_inversePrimary
import presentation.ui.theme.md_theme_dark_inverseSurface
import presentation.ui.theme.md_theme_dark_onBackground
import presentation.ui.theme.md_theme_dark_onError
import presentation.ui.theme.md_theme_dark_onErrorContainer
import presentation.ui.theme.md_theme_dark_onPrimary
import presentation.ui.theme.md_theme_dark_onPrimaryContainer
import presentation.ui.theme.md_theme_dark_onSecondary
import presentation.ui.theme.md_theme_dark_onSecondaryContainer
import presentation.ui.theme.md_theme_dark_onSurface
import presentation.ui.theme.md_theme_dark_onSurfaceVariant
import presentation.ui.theme.md_theme_dark_onTertiary
import presentation.ui.theme.md_theme_dark_onTertiaryContainer
import presentation.ui.theme.md_theme_dark_outline
import presentation.ui.theme.md_theme_dark_outlineVariant
import presentation.ui.theme.md_theme_dark_primary
import presentation.ui.theme.md_theme_dark_primaryContainer
import presentation.ui.theme.md_theme_dark_scrim
import presentation.ui.theme.md_theme_dark_secondary
import presentation.ui.theme.md_theme_dark_secondaryContainer
import presentation.ui.theme.md_theme_dark_surface
import presentation.ui.theme.md_theme_dark_surfaceTint
import presentation.ui.theme.md_theme_dark_surfaceVariant
import presentation.ui.theme.md_theme_dark_tertiary
import presentation.ui.theme.md_theme_dark_tertiaryContainer
import presentation.ui.theme.md_theme_light_background
import presentation.ui.theme.md_theme_light_error
import presentation.ui.theme.md_theme_light_errorContainer
import presentation.ui.theme.md_theme_light_inverseOnSurface
import presentation.ui.theme.md_theme_light_inversePrimary
import presentation.ui.theme.md_theme_light_inverseSurface
import presentation.ui.theme.md_theme_light_onBackground
import presentation.ui.theme.md_theme_light_onError
import presentation.ui.theme.md_theme_light_onErrorContainer
import presentation.ui.theme.md_theme_light_onPrimary
import presentation.ui.theme.md_theme_light_onPrimaryContainer
import presentation.ui.theme.md_theme_light_onSecondary
import presentation.ui.theme.md_theme_light_onSecondaryContainer
import presentation.ui.theme.md_theme_light_onSurface
import presentation.ui.theme.md_theme_light_onSurfaceVariant
import presentation.ui.theme.md_theme_light_onTertiary
import presentation.ui.theme.md_theme_light_onTertiaryContainer
import presentation.ui.theme.md_theme_light_outline
import presentation.ui.theme.md_theme_light_outlineVariant
import presentation.ui.theme.md_theme_light_primary
import presentation.ui.theme.md_theme_light_primaryContainer
import presentation.ui.theme.md_theme_light_scrim
import presentation.ui.theme.md_theme_light_secondary
import presentation.ui.theme.md_theme_light_secondaryContainer
import presentation.ui.theme.md_theme_light_surface
import presentation.ui.theme.md_theme_light_surfaceTint
import presentation.ui.theme.md_theme_light_surfaceVariant
import presentation.ui.theme.md_theme_light_tertiary
import presentation.ui.theme.md_theme_light_tertiaryContainer

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}