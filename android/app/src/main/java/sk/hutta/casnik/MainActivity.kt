package sk.hutta.casnik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import sk.hutta.casnik.ui.theme.InteraktivnyCasnikTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InteraktivnyCasnikTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PingScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InteraktivnyCasnikTheme {
        Greeting("Android")
    }
}

@Composable
fun PingScreen(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("Pripájam sa") }

    LaunchedEffect(Unit) {
        text = try {
            "Server odpovedal: " + serverApi.ping()
        } catch (e: Exception) {
            "Chyba: ${e.message}"
        }
    }

    Text(text = text, modifier = modifier)
}