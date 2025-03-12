package com.example.projectwork_android

import Drink
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.rememberImagePainter
import com.example.projectwork_android.ui.theme.ProjectWorkAndroidTheme
import fetchDrinkNames
import fetchIngridients
import searchDrink
import kotlinx.coroutines.launch
import org.json.JSONArray


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjectWorkAndroidTheme {
                //Variabili che utilizzo in più pagine
                val navController = rememberNavController()
                val drinksViewModel: DrinksViewModel = viewModel()
                var map: MutableMap<String,Boolean> = mutableMapOf()

                //Funzione per cambiare Pagina
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "main") {
                        composable("main") { MainPage(navController) }
                        composable("completion") { CompletionPage(navController, drinksViewModel) }
                        composable("inventory") { InventoryPage(navController,map) }
                        composable("search") { SearchPage(navController) }
                        composable("drink/{title}") { backStackEntry ->
                            val title = backStackEntry.arguments?.getString("title")?:"Default Title"
                            RecipePage(navController, title) }

                    }
                }
            }
        }
    }
}



// Il codice NON è Completato,
// è possibile SOLO visualizzare la lista di ingredienti e bevande









//Pagina Principale
@Composable
fun MainPage(navController: NavHostController) {

    Column(
        modifier = Modifier.fillMaxSize().background(Color(79, 149, 157)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(text = "Recipe Tracker", fontSize = 50.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))
        NavigationButton(text = "Lista Completamento", destination = "completion", navController)
        NavigationButton(text = "Inventario Ingredienti", destination = "inventory", navController)
        NavigationButton(text = "Ricette Possibili", destination = "search", navController)
    }
}

//Bottoni di navigazione Generale
@Composable
fun NavigationButton(text: String, destination: String, navController: NavHostController) {
    Button(onClick = { navController.navigate(destination) }, modifier = Modifier.padding(8.dp)) {
        Text(text = text)
    }
}

//Lista che ti elenca tutte le bevande che puoi fare
//Testo Selezionabile per vedere più informazioni della bevanda (Non funziona)
@Composable
fun CompletionPage(navController: NavHostController, viewModel: DrinksViewModel) {
    val drinksList = viewModel.drinksList
    val isLoading = viewModel.isLoading

    LaunchedEffect(Unit) { viewModel.fetchDrinks() }

    PageLayout(title = "Lista di Bevande", navController) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().background(Color(79, 149, 157))) {
                items(drinksList) { drink ->
                    ListDrink(drink, navController)
                }
            }
        }
    }
}

//pagina con la lista di ingredienti che puoi selezionare per indicare queli possiedi (Non finito)
@Composable
fun InventoryPage(navController: NavHostController, map: MutableMap<String, Boolean>) {
    var ingredientList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            ingredientList = fetchIngridients() // Assuming this fetches the ingredient list
            ingredientList.forEach { ingredient ->
                // Initialize all ingredients with a default value (false)
                map[ingredient] = false
            }
            isLoading = false
        }
    }

    PageLayout(title = "Inventario Ingredienti", navController) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().background(Color(79, 149, 157))) {
                items(map.entries.toList()) { entry ->
                    // Pass the onCheckedChange callback to update the map
                    ListItem(
                        text = entry.key,
                        bol = entry.value,
                        onCheckedChange = { isChecked ->
                            map[entry.key] = isChecked
                        }
                    )
                }
            }
        }
    }
}

//Pagina per indicare cosa si può fare con gli ingredienti che si possiedono(Non finito)
@Composable
fun SearchPage(navController: NavHostController) {
    PageLayout(title = "Ricette Possibili", navController) {
        Text(text = "TODO: Implement Search Functionality", color = Color.White)
    }
}

//Pagina variabile che mostra le informaz<ioni delle bevande
@Composable
fun RecipePage(navController: NavHostController, title: String) {
    // Make sure to initialize the state properly inside the Composable function
    var drink by remember { mutableStateOf<Drink?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Launch the async task inside LaunchedEffect, which is part of Composable context
    LaunchedEffect(Unit) {
        // Assuming searchDrink() is a suspend function that fetches the drink data asynchronously
        drink = searchDrink(title)
        isLoading = false
    }

    PageLayout(title = title, navController) {
        if (isLoading) {
            LoadingIndicator()
        } else {
            drink?.let {

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Image(painter = rememberImagePainter(it.strDrinkThumb), contentDescription = "Drink Image")
                    }
                    item {
                        Text(text = it.strInstructions ?: "Instructions not available", color = Color.White)
                    }

                    item {
                        Text(text = it.strIngredient1 ?: "Instructions not available", color = Color.White)
                        Text(text = it.strMeasure1 ?: "Instructions not available", color = Color.White)
                    }
                    if (it.strIngredient2 != null){
                        item {
                            Text(text = it.strIngredient2, color = Color.White)
                            Text(text = it.strMeasure2 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient3 != null){
                        item {
                            Text(text = it.strIngredient3, color = Color.White)
                            Text(text = it.strMeasure3 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient4 != null){
                        item {
                            Text(text = it.strIngredient4, color = Color.White)
                            Text(text = it.strMeasure4 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient2 != null){
                        item {
                            Text(text = it.strIngredient5 ?: "Instructions not available", color = Color.White)
                            Text(text = it.strMeasure5 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient6 != null){
                        item {
                            Text(text = it.strIngredient6, color = Color.White)
                            Text(text = it.strMeasure6 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient7 != null){
                        item {
                            Text(text = it.strIngredient7, color = Color.White)
                            Text(text = it.strMeasure7 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient8 != null){
                        item {
                            Text(text = it.strIngredient8, color = Color.White)
                            Text(text = it.strMeasure8 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient9 != null){
                        item {
                            Text(text = it.strIngredient9, color = Color.White)
                            Text(text = it.strMeasure9 ?: "Instructions not available", color = Color.White)
                        }
                    }
                    if (it.strIngredient10 != null){
                        item {
                            Text(text = it.strIngredient10, color = Color.White)
                            Text(text = it.strMeasure10 ?: "Instructions not available", color = Color.White)
                        }
                    }

                }
            }
        }
    }
}

//layout generale delle pagine, bottone per tornare indietro, titolo e il resto del contenuto
@Composable
fun PageLayout(title: String, navController: NavHostController, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(Color(32, 87, 129)).padding(16.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { navController.navigateUp() },
                    shape = CircleShape,
                    modifier = Modifier.size(50.dp))
                {Text(text = "<")}
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

//Oggetto ingrediente con checkbox per indicare cosa si possiede
@Composable
fun ListItem(text: String, bol: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            enabled = true,
            checked = bol,
            onCheckedChange = onCheckedChange, // Call the passed function to update the value in the map
        )
        Text(text = text, color = Color.White, modifier = Modifier.padding(start = 8.dp))
    }
}


//Oggetto Visualizzabile lista con testo interattivo che porta alla pagina della bevanda
@Composable
fun ListDrink(text: String, navController: NavHostController) {
    Text(text = text, color = Color.White, modifier = Modifier.padding(8.dp).clickable { navController.navigate("drink"){ } })
}

//indicatore di caricamento perchè la pagina delle bevande ci impiega un pochino a fare il get
@Composable
fun LoadingIndicator() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Tempo Estimato 1 min", color = Color.White)
    }
}


//ViewModel che ho utilizzato per salvare La lista di bevande e non doverla Richiedere Dinuovo
//uscendo e rientrando dalla pagina
class DrinksViewModel : ViewModel() {
    var drinksList by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun fetchDrinks() {
        if (drinksList.isNotEmpty()) return
        viewModelScope.launch {
            drinksList = fetchDrinkNames()
            isLoading = false
        }
    }
}


//Funzioni Per salvare le informazioni, Quali CheckBox Si ha selezionato

fun saveArray(context: Context, key: String, list: List<String>) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val jsonArray = JSONArray(list)
    editor.putString(key, jsonArray.toString())
    editor.apply()
}

fun getArray(context: Context, key: String): List<String> {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    val jsonString = sharedPreferences.getString(key, null) ?: return emptyList()

    val jsonArray = JSONArray(jsonString)
    val list = mutableListOf<String>()

    for (i in 0 until jsonArray.length()) {
        list.add(jsonArray.getString(i))
    }

    return list
}