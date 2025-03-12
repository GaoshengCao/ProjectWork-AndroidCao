import android.util.Log

import kotlinx.coroutines.delay
import retrofit2.http.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


data class Drink(
    val strDrink: String,
    val strDrinkThumb: String?,
    val strInstructions: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?
)
data class DrinkResponce( val drinks: List<Drink>)

data class Ingridient(val strIngredient1: String)

data class IngridientResponce( val drinks: List<Ingridient>)

data class DrinkName(val strDrink: String)

data class DrinkNameResponce(val drinks: List<DrinkName>)



interface CocktailApiService {

    @GET("search.php")
    suspend fun getDrink(@Query("s") query: String): DrinkResponce

    @GET("list.php")
    suspend fun getIngridients(@Query("i") query: String): IngridientResponce

    @GET("filter.php")
    suspend fun getDrinkByIngridient(@Query("i") query: String) : DrinkNameResponce
}

// Retrofit Instance
object RetrofitInstance {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

    val api: CocktailApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)  // Ensure this URL is correct
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CocktailApiService::class.java)
    }
}

//Funzione per prendedre Tutti gli ingredienti
suspend fun fetchIngridients(): List<String> {
    return try {
        val response = RetrofitInstance.api.getIngridients("list")

        val list = response.drinks.map { it.strIngredient1 }
        list.sorted()
    } catch (e: Exception) {
        Log.e("fetchIngridients", "Error fetching ingredients", e)
        listOf("Error fetching ingredients", e.message.toString())
    }
}

//Funzoine per ottenere il nome di tutte le bevande dagli ingredienti e
suspend fun fetchDrinkNames(): List<String> {
    return try {
        val ingredients = fetchIngridients()
        val drinkNames = mutableSetOf<String>()
        val initialResponse = RetrofitInstance.api.getDrinkByIngridient("Gin")
        drinkNames.addAll(initialResponse.drinks.map { it.strDrink })
        for (ingredient in ingredients) {
            // Prevenire rate Limiting (Controllo DOS)
            delay(500)
            val response = RetrofitInstance.api.getDrinkByIngridient(ingredient)
            drinkNames.addAll(response.drinks.map { it.strDrink })
        }

        drinkNames.toList()
        drinkNames.sorted()
    } catch (e: Exception) {
        Log.e("FetchDrinks", "Error Fetching Drinks", e)
        listOf("Error fetching Drinks", e.message.toString())
    }
}

suspend fun searchDrink(name : String): Drink{
    return try {
        val responce = RetrofitInstance.api.getDrink(name)
        responce.drinks.get(0)
    }catch (e: Exception) {
        return Drink(
            strDrink = e.message.toString(),
            strDrinkThumb = null,
            strInstructions = null,
            strIngredient1 = null,
            strIngredient2 = null,
            strIngredient3 = null,
            strIngredient4 = null,
            strIngredient5 = null,
            strIngredient6 = null,
            strIngredient7 = null,
            strIngredient8 = null,
            strIngredient9 = null,
            strIngredient10 = null,
            strMeasure1 = null,
            strMeasure2 = null,
            strMeasure3 = null,
            strMeasure4 = null,
            strMeasure5 = null,
            strMeasure6 = null,
            strMeasure7 = null,
            strMeasure8 = null,
            strMeasure9 = null,
            strMeasure10 = null
        )
    }
}