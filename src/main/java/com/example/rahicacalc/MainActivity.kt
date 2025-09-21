package com.example.rahicacalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.app.DatePickerDialog
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight




class MainActivity : ComponentActivity() {
    private lateinit var db: AppDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            if (db.alimentoDao().buscarPorNombre("lentejas cocidas") == null) {
                db.alimentoDao().insertarAlimentos(DataAlimentos.lista)
            }
        }
        DataAlimentos.load(this) // Carga desde el JSON
        val alimentos = DataAlimentos.lista

        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "inicio") {
                    composable("inicio") { PantallaInicio(navController) }
                    composable("principal") { PantallaPrincipal(db, navController) }
                    composable("historial") { PantallaHistorial(db) }
                }
            }
        }
    }
}

@Composable
fun PantallaInicio(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Fondo con degradado radial suave
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2E7D32), // Verde intenso (esquina superior izquierda)
                            Color(0xFF81C784), // Verde medio
                            Color(0xFFE8F5E9)  // Verde muy claro (casi blanco)
                        ),
                        start = Offset(0f, 0f), // Esquina superior izquierda
                        end = Offset.Infinite // Diagonal hacia abajo a la derecha
                    )
                )
        )
        {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Tarjeta de bienvenida con icono
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black), // Fondo negro
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.Black) // Asegura fondo negro
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo de la app",
                            modifier = Modifier
                                .size(240.dp)
                                .align(Alignment.CenterHorizontally) // Centrado explícito
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Calcula hidratos y raciones de forma rápida y sencilla.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White, // Texto blanco
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .align(Alignment.CenterHorizontally) // Centrado explícito
                        )
                    }
                }


                BotonGradiente(
                    texto = "Empezar"
                ) {
                    navController.navigate("principal")
                }


                // Aviso legal
                Text(
                    text = "Recuerda usar esta aplicación con precaución. Los cálculos y datos mostrados se basan en " +
                            "valores de referencia y pueden no reflejar con exactitud las necesidades o condiciones " +
                            "particulares de cada usuario. Antes de realizar cambios en su dieta o tratamiento, consulte siempre " +
                            "con un médico o dietista cualificado. El desarrollador no se hace responsable de decisiones tomadas en " +
                            "base a la información aquí contenida. El uso de la aplicación implica la aceptación de estos términos y condiciones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black, // Blanco para que destaque sobre fondo oscuro
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )

                }
            }
        }
    }


@Composable
fun PantallaPrincipal(db: AppDatabase, navController: NavHostController) {
    var nombrePlato by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf(TextFieldValue("")) }
    var gramos by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(listOf<Alimento>()) }

    var ingredientes by remember { mutableStateOf(listOf<IngredienteCalculado>()) }

    var totalHidratos by remember { mutableDoubleStateOf(0.0) }
    var totalRaciones by remember { mutableDoubleStateOf(0.0) }

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Fondo con degradado radial
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2E7D32), // Verde intenso
                            Color(0xFF81C784), // Verde medio
                            Color(0xFFE8F5E9)  // Verde muy claro
                        ),
                        start = Offset(0f, 0f),
                        end = Offset.Infinite
                    )
                )
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Text(
                text = "🍽 Nuevo plato",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black
            )


            // Nombre del plato
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                OutlinedTextField(
                    value = nombrePlato,
                    onValueChange = { nombrePlato = it },
                    label = { Text("Nombre del plato") },
                    placeholder = { Text("Ej: Ensalada de pasta") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }

            // Datos del ingrediente
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = {
                            nombre = it
                            sugerencias = if (it.text.isNotBlank()) {
                                DataAlimentos.lista.filter { alimento ->
                                    alimento.nombre.contains(it.text, ignoreCase = true)
                                }
                            } else emptyList()
                        },
                        label = { Text("Nombre del alimento") },
                        placeholder = { Text("Ej: nueces") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (sugerencias.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                items(sugerencias) { alimento ->
                                    Text(
                                        text = alimento.nombre,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                nombre = TextFieldValue(alimento.nombre)
                                                sugerencias = emptyList()
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gramos,
                        onValueChange = { gramos = it },
                        label = { Text("Cantidad en gramos") },
                        placeholder = { Text("Ej: 100") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    // Botón original (lógica intacta)
                    BotonGradiente("Añadir ingrediente") {
                        scope.launch {
                            val clave = nombre.text.trim()
                            val alimento = DataAlimentos.lista
                                .firstOrNull { it.nombre.equals(clave, ignoreCase = true) }
                            if (alimento != null) {
                                val gramosDouble = gramos.toDoubleOrNull() ?: 0.0
                                val hidratos = calcularHidratos(gramosDouble, alimento.hidratosPor100g)
                                val raciones = calcularRaciones(hidratos)

                                ingredientes = ingredientes + IngredienteCalculado(
                                    nombre = alimento.nombre,
                                    gramos = gramosDouble,
                                    hidratos = hidratos,
                                    raciones = raciones
                                )

                                nombre = TextFieldValue("")
                                gramos = ""
                            }
                        }
                    }
                }
            }

            // Lista de ingredientes
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Ingredientes añadidos",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(Modifier.height(6.dp))

                    if (ingredientes.isEmpty()) {
                        Text(
                            "Aún no has añadido ingredientes",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(ingredientes) { ing ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(ing.nombre, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${ing.gramos} g • ${"%.2f".format(ing.hidratos)} g HC",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Totales
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BotonGradiente("Calcular totales") {
                        totalHidratos = ingredientes.sumOf { it.hidratos }
                        totalRaciones = ingredientes.sumOf { it.raciones }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Total hidratos: %.2f g".format(totalHidratos),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Total raciones: %.2f".format(totalRaciones),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Guardar (misma lógica)

            BotonGradiente("Guardar plato") {
                if (ingredientes.isEmpty()) {
                    Log.w("DEBUG", "No hay ingredientes para guardar")
                    return@BotonGradiente
                }
                if (nombrePlato.isBlank()) {
                    Log.w("DEBUG", "El nombre del plato está vacío")
                    return@BotonGradiente
                }

                val fechaHora = java.text.SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())

                Log.d("DEBUG", "Guardando plato: $nombrePlato | ${ingredientes.size} ingredientes | $fechaHora")
                ingredientes.forEach { ing ->
                    Log.d("DEBUG", "  - ${ing.nombre} | ${ing.gramos} g | ${"%.2f".format(ing.hidratos)} hidratos")
                }

                scope.launch {
                    try {
                        ingredientes.forEach { ing ->
                            db.historialDao().insertar(
                                HistorialEntry(
                                    fechaHora = fechaHora,
                                    nombrePlato = nombrePlato,
                                    alimento = ing.nombre,
                                    gramos = ing.gramos,
                                    hidratos = ing.hidratos,
                                    raciones = ing.raciones
                                )
                            )
                        }
                        Log.d("DEBUG", "Guardado OK")

                        ingredientes = emptyList()
                        totalHidratos = 0.0
                        totalRaciones = 0.0
                        nombrePlato = ""
                    } catch (e: Exception) {
                        Log.e("DEBUG", "ERROR guardando plato", e)
                    }
                }
            }

            // Navegación
            BotonGradiente(
                texto = "Ver historial"
            ) {
                navController.navigate("historial")
            }

        }
    }
}

data class IngredienteCalculado(
    val nombre: String,
    val gramos: Double,
    val hidratos: Double,
    val raciones: Double
)




@Composable
fun PantallaHistorial(db: AppDatabase) {
    var fechaSeleccionada by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendario = Calendar.getInstance()

    val platos by if (fechaSeleccionada.isBlank()) {
        db.historialDao().obtenerPlatos().collectAsState(initial = emptyList())
    } else {
        db.historialDao().obtenerPlatosPorFecha(fechaSeleccionada).collectAsState(initial = emptyList())
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2E7D32), // Verde intenso
                        Color(0xFF81C784), // Verde medio
                        Color(0xFFE8F5E9)  // Verde muy claro
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📅 Historial de platos",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )


            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonGradiente(
                    texto = if (fechaSeleccionada.isBlank()) "Seleccionar fecha" else "Fecha: $fechaSeleccionada"
                ) {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val dia = dayOfMonth.toString().padStart(2, '0')
                            val mes = (month + 1).toString().padStart(2, '0')
                            fechaSeleccionada = "$dia/$mes"
                        },
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                if (fechaSeleccionada.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    BotonGradiente(
                        texto = "Quitar filtro"
                    ) {
                        fechaSeleccionada = ""
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (platos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay platos guardados todavía 🍽️",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(platos) { plato ->
                        var mostrarIngredientes by remember { mutableStateOf(false) }

                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = plato.nombrePlato,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(onClick = { mostrarIngredientes = !mostrarIngredientes }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.List,
                                            contentDescription = "Ver ingredientes",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = plato.fechaHora,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "🥖 Hidratos totales: %.2f g".format(plato.totalHidratos),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "⚖️ Raciones totales: %.2f".format(plato.totalRaciones),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                if (mostrarIngredientes) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Ingredientes:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    plato.ingredientes.forEach { ing ->
                                        Text(
                                            text = "- ${ing.nombre}: ${ing.gramos} g | ${"%.2f".format(ing.hidratos)} hidratos | ${"%.2f".format(ing.raciones)} raciones",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotonGradiente(texto: String, onClick: () -> Unit) {
    // Animación de elevación al presionar
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(if (pressed) 2.dp else 8.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()  // Ahora ocupa todo el ancho
            .height(50.dp)
            .shadow(elevation, RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}













