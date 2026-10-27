package com.williammarrugo.fiao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.williammarrugo.fiao.ui.navegacion.FiaoNavHost
import com.williammarrugo.fiao.ui.theme.FiaoTheme

/**
 * El punto de entrada de la app.
 *
 * Fiao es una app de una sola Activity: todas las pantallas son
 * composables y la navegación entre ellas la maneja Navigation Compose.
 * Es el modelo recomendado hoy en Android.
 *
 * Importante para el día 6: cuando el celular rota, el sistema destruye
 * esta Activity y la vuelve a crear, y onCreate corre de nuevo. Por eso
 * el estado de las pantallas no puede vivir aquí.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FiaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FiaoNavHost()
                }
            }
        }
    }
}
