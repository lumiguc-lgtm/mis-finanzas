package pe.misfinanzas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pe.misfinanzas.app.ui.AppFinanzas
import pe.misfinanzas.app.ui.theme.TemaFinanzas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemaFinanzas { AppFinanzas() }
        }
    }
}
