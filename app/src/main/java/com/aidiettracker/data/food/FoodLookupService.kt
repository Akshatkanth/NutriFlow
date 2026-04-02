package com.aidiettracker.data.food

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.roundToInt

data class FoodNutrition(
    val name: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val defaultServingGrams: Double,
    val aliases: List<String>
)

data class NutritionResult(
    val displayName: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val source: String
)

object FoodLookupService {
    private val fallbackCache = mutableMapOf<String, FoodNutrition>()
    private val nonVegKeywords = listOf(
        "chicken", "mutton", "fish", "egg", "prawn", "shrimp", "meat", "lal maas", "murgh", "biryani chicken"
    )

    private val localFoods = listOf(
        FoodNutrition("Roti", 297.0, 9.0, 46.0, 7.0, 35.0, listOf("chapati", "phulka", "roti", "roti", "chapatti")),
        FoodNutrition("Steamed Rice", 130.0, 2.7, 28.0, 0.3, 150.0, listOf("rice", "chawal", "bhaat", "cooked rice")),
        FoodNutrition("Dal Tadka", 120.0, 6.0, 15.0, 3.0, 150.0, listOf("dal", "daal", "dal fry", "lentil curry", "dal tadka")),
        FoodNutrition("Rajma", 140.0, 7.5, 20.0, 3.0, 150.0, listOf("kidney beans curry", "rajma masala", "rajma chawal", "rajma")),
        FoodNutrition("Chole", 160.0, 8.0, 24.0, 4.0, 150.0, listOf("chana masala", "chole masala", "chana", "chole")),
        FoodNutrition("Paneer Bhurji", 265.0, 14.0, 8.0, 19.0, 120.0, listOf("paneer", "paneer bhurji", "paneer bhurjee", "cottage cheese scramble")),
        FoodNutrition("Poha", 180.0, 3.0, 32.0, 4.0, 120.0, listOf("kanda poha", "aval upma", "pohha", "pohe", "poha")),
        FoodNutrition("Upma", 170.0, 4.5, 27.0, 5.0, 120.0, listOf("rava upma", "suji upma", "semolina upma", "upma")),
        FoodNutrition("Idli", 146.0, 4.5, 30.0, 0.7, 45.0, listOf("idly", "idli", "idli sambar", "steam idli")),
        FoodNutrition("Dosa", 168.0, 4.0, 28.0, 4.0, 100.0, listOf("masala dosa", "plain dosa", "dosai", "dosa")),
        FoodNutrition("Sambar", 70.0, 3.0, 10.0, 2.0, 150.0, listOf("sambhar", "sambar", "sambhar rice", "south indian sambar")),
        FoodNutrition("Curd", 98.0, 3.5, 4.7, 4.3, 100.0, listOf("dahi", "yogurt", "curd", "thayir", "dadhi")),
        FoodNutrition("Chicken Curry", 195.0, 20.0, 4.0, 10.0, 150.0, listOf("chicken gravy", "murgh curry", "chicken masala", "chicken curry")),
        FoodNutrition("Egg Omelette", 154.0, 11.0, 1.5, 11.0, 60.0, listOf("omelette", "omelet", "anda bhurji", "egg omelette")),
        FoodNutrition("Aloo Paratha", 260.0, 6.0, 38.0, 9.0, 120.0, listOf("aloo paratha", "alo paratha", "potato paratha")),
        FoodNutrition("Khichdi", 150.0, 4.0, 28.0, 3.0, 150.0, listOf("khichadi", "kichdi", "moong dal khichdi", "khichdi")),
        FoodNutrition("Veg Biryani", 180.0, 4.0, 31.0, 5.0, 180.0, listOf("vegetable biryani", "veg biryani", "pulao", "veg pulao")),
        FoodNutrition("Chicken Biryani", 230.0, 12.0, 26.0, 8.0, 180.0, listOf("murgh biryani", "chicken biryani", "biryani")),
        FoodNutrition("Pav Bhaji", 210.0, 5.0, 30.0, 8.0, 180.0, listOf("pao bhaji", "pav bhaji", "pau bhaji")),
        FoodNutrition("Vada Pav", 220.0, 6.0, 28.0, 9.0, 130.0, listOf("vada pav", "wada pav", "batata vada")),
        FoodNutrition("Dhokla", 160.0, 6.0, 28.0, 3.0, 100.0, listOf("dhokla", "khaman", "khaman dhokla")),
        FoodNutrition("Thepla", 240.0, 7.0, 35.0, 9.0, 100.0, listOf("thepala", "methi thepla", "gujarati thepla")),
        FoodNutrition("Sprouts Salad", 110.0, 8.0, 16.0, 2.0, 150.0, listOf("moong sprouts", "sprouts chaat", "sprouted salad")),
        FoodNutrition("Banana", 89.0, 1.1, 23.0, 0.3, 118.0, listOf("kela", "banana", "plantain")),
        FoodNutrition("Apple", 52.0, 0.3, 14.0, 0.2, 100.0, listOf("seb", "apple")),
        FoodNutrition("Mango Lassi", 140.0, 4.0, 22.0, 4.0, 200.0, listOf("lassi", "sweet lassi", "mango lassi")),
        FoodNutrition("Buttermilk", 35.0, 1.5, 4.5, 1.0, 200.0, listOf("chaas", "chhach", "buttermilk", "mattha")),
        FoodNutrition("Milk", 61.0, 3.2, 4.8, 3.3, 100.0, listOf("doodh", "milk", "buffalo milk")),
        FoodNutrition("Tea with Milk", 45.0, 1.0, 7.0, 1.5, 100.0, listOf("chai", "tea", "masala chai", "chai with milk")),
        FoodNutrition("Puri Bhaji", 290.0, 6.0, 35.0, 14.0, 150.0, listOf("poori bhaji", "puri sabzi", "bhaji puri")),
        FoodNutrition("Sabudana Khichdi", 180.0, 3.0, 32.0, 4.0, 150.0, listOf("sabudana khichadi", "sago khichdi")),
        FoodNutrition("Uttapam", 170.0, 4.0, 26.0, 5.0, 120.0, listOf("uttapam", "uttapa", "onion uttapam")),
        FoodNutrition("Aloo Gobi", 120.0, 3.0, 15.0, 5.0, 150.0, listOf("aloo gobi", "alu gobi", "potato cauliflower")),
        FoodNutrition("Bhindi Masala", 110.0, 2.5, 10.0, 7.0, 150.0, listOf("bhindi", "okra masala", "ladyfinger sabzi")),
        FoodNutrition("Mixed Veg Sabzi", 95.0, 3.0, 12.0, 4.0, 150.0, listOf("sabzi", "sabji", "veg sabzi", "mixed vegetable")),
        FoodNutrition("Dal Baati Churma", 320.0, 9.0, 48.0, 10.0, 220.0, listOf("baati churma", "dal bati", "dal baati", "rajasthani thali")),
        FoodNutrition("Gatte Ki Sabzi", 165.0, 6.0, 12.0, 10.0, 150.0, listOf("gatte", "gatta curry", "gatte ki sabzi", "gatte ki sabji")),
        FoodNutrition("Ker Sangri", 145.0, 4.0, 14.0, 8.0, 120.0, listOf("kersangri", "ker sangri sabzi", "ker sangri", "rajasthani ker sangri")),
        FoodNutrition("Bajre Ki Roti", 220.0, 6.5, 42.0, 4.0, 45.0, listOf("bajra roti", "missi roti", "pearl millet roti", "bajre ki roti")),
        FoodNutrition("Pyaaz Kachori", 290.0, 6.0, 34.0, 14.0, 90.0, listOf("pyaz kachori", "pyaaz kachori", "onion kachori")),
        FoodNutrition("Mirchi Vada", 240.0, 5.0, 28.0, 12.0, 90.0, listOf("mirchi bada", "mirchi vada", "mirchi bada")),
        FoodNutrition("Papad Ki Sabzi", 140.0, 4.5, 10.0, 9.0, 150.0, listOf("papad sabzi", "papad ki sabji", "papad curry")),
        FoodNutrition("Lal Maas", 270.0, 24.0, 4.0, 16.0, 150.0, listOf("laal maas", "lal mass", "rajasthani mutton curry")),
        FoodNutrition("Mawa Kachori", 310.0, 5.0, 32.0, 18.0, 80.0, listOf("khoya kachori", "mawa kachori", "mawa bada")),
        FoodNutrition("Ghevar", 340.0, 4.0, 42.0, 16.0, 100.0, listOf("ghewar", "rabdi ghevar", "rajasthani ghevar")),
        FoodNutrition("Malpua", 310.0, 5.0, 40.0, 14.0, 90.0, listOf("malpua", "malpoa", "rajasthani malpua")),
        FoodNutrition("Churma Laddu", 360.0, 6.0, 46.0, 16.0, 70.0, listOf("churma", "churma ladoo", "rajasthani churma"))
    )

    fun defaultSuggestions(): List<String> = localFoods.map { it.name }

    fun searchSuggestions(query: String): List<String> {
        val normalized = normalize(query)
        if (normalized.isBlank()) {
            return defaultSuggestions()
        }

        return localFoods
            .map { food -> food to foodMatchScore(food, normalized) }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
            .map { it.first.name }
            .distinct()
            .take(8)
    }

    private fun foodMatchScore(food: FoodNutrition, normalizedQuery: String): Double {
        val candidateText = listOf(food.name) + food.aliases
        val candidateTokens = candidateText.joinToString(" ") { normalize(it) }
            .split(" ")
            .filter { it.isNotBlank() }
            .toSet()
        val queryTokens = normalizedQuery
            .split(" ")
            .filter { it.isNotBlank() }
            .toSet()

        if (queryTokens.isEmpty() || candidateTokens.isEmpty()) return 0.0

        val overlap = candidateTokens.intersect(queryTokens).size.toDouble()
        val tokenScore = overlap / queryTokens.size.coerceAtLeast(1)
        val containsScore = if (candidateText.any { normalize(it).contains(normalizedQuery) }) 1.0 else 0.0
        return maxOf(tokenScore, containsScore)
    }

    private fun normalizedQueryVariants(query: String): List<String> {
        val normalized = normalize(query)
        if (normalized.isBlank()) return emptyList()

        val stopWords = setOf("with", "and", "or", "fresh", "boiled", "fried", "dry", "masala", "curry", "gravy")
        val tokens = normalized.split(" ").filter { it.isNotBlank() }
        val stripped = tokens.filterNot { it in stopWords }.joinToString(" ").trim()

        return listOf(normalized, stripped)
            .flatMap { variant ->
                val variantTokens = variant.split(" ").filter { it.isNotBlank() }
                listOf(
                    variant,
                    variantTokens.take(2).joinToString(" "),
                    variantTokens.firstOrNull().orEmpty()
                )
            }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun resolveNutrition(foodQuery: String, quantity: Double, unit: String): NutritionResult? {
        val cacheKey = buildCacheKey(foodQuery, quantity, unit)
        fallbackCache[cacheKey]?.let { return computeWithQuantity(it, quantity, unit, "cache") }

        val localFood = findLocalFood(foodQuery)
        if (localFood != null) {
            val result = compute(localFood, quantityToGrams(quantity, unit, localFood.defaultServingGrams), "local")
            return result
        }

        val onlineFood = fetchFromOpenFoodFacts(foodQuery)
        if (onlineFood != null) {
            fallbackCache[cacheKey] = onlineFood
            return computeWithQuantity(onlineFood, quantity, unit, "online")
        }

        val fuzzyFood = findLocalFood(foodQuery)
        if (fuzzyFood != null) {
            return computeWithQuantity(fuzzyFood, quantity, unit, "estimated")
        }

        return null
    }

    private fun computeWithQuantity(food: FoodNutrition, quantity: Double, unit: String, source: String): NutritionResult {
        val grams = quantityToGrams(quantity, unit, food.defaultServingGrams)
        return compute(food, grams, source)
    }

    private fun compute(food: FoodNutrition, grams: Double, source: String): NutritionResult {
        val factor = (grams / 100.0).coerceAtLeast(0.0)
        return NutritionResult(
            displayName = food.name,
            calories = (food.caloriesPer100g * factor).roundToInt(),
            protein = (food.proteinPer100g * factor).toFloat(),
            carbs = (food.carbsPer100g * factor).toFloat(),
            fat = (food.fatPer100g * factor).toFloat(),
            source = source
        )
    }

    private fun findLocalFood(query: String): FoodNutrition? {
        val normalized = normalize(query)
        if (normalized.isBlank()) return null

        localFoods.firstOrNull { normalize(it.name) == normalized }?.let { return it }
        localFoods.firstOrNull { food -> food.aliases.any { alias -> normalize(alias) == normalized } }?.let { return it }

        localFoods.firstOrNull { food ->
            normalize(food.name).contains(normalized) || food.aliases.any { normalize(it).contains(normalized) }
        }?.let { return it }

        return localFoods
            .map { food -> food to foodMatchScore(food, normalized) }
            .maxByOrNull { it.second }
            ?.takeIf { it.second >= 0.34 }
            ?.first
    }

    private fun quantityToGrams(quantity: Double, unit: String, servingGrams: Double): Double {
        val safeQuantity = quantity.coerceAtLeast(0.0)
        return when (unit.trim().lowercase(Locale.getDefault())) {
            "g", "gram", "grams" -> safeQuantity
            "serving", "servings" -> safeQuantity * servingGrams
            "roti", "roti(s)" -> safeQuantity * 35.0
            "bowl", "bowls" -> safeQuantity * 150.0
            "piece", "pieces" -> safeQuantity * 50.0
            else -> safeQuantity
        }
    }

    private fun fetchFromOpenFoodFacts(query: String): FoodNutrition? {
        for (variant in normalizedQueryVariants(query)) {
            val encoded = URLEncoder.encode(variant, "UTF-8")
            val url = URL("https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=10")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "AI-Diet-Tracker/1.0")
            }

            val result = runCatching {
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(body)
                val products = root.optJSONArray("products") ?: return@runCatching null

                for (i in 0 until products.length()) {
                    val product = products.optJSONObject(i) ?: continue
                    val nutriments = product.optJSONObject("nutriments") ?: continue
                    val calories = nutriments.optDouble("energy-kcal_100g", -1.0)
                    val protein = nutriments.optDouble("proteins_100g", -1.0)
                    val carbs = nutriments.optDouble("carbohydrates_100g", -1.0)
                    val fat = nutriments.optDouble("fat_100g", -1.0)

                    if (calories <= 0 || protein < 0 || carbs < 0 || fat < 0) continue

                    val name = product.optString("product_name", variant).ifBlank { variant }
                    return@runCatching FoodNutrition(
                        name = name,
                        caloriesPer100g = calories,
                        proteinPer100g = protein,
                        carbsPer100g = carbs,
                        fatPer100g = fat,
                        defaultServingGrams = 100.0,
                        aliases = emptyList()
                    )
                }
                null
            }.getOrNull()

            connection.disconnect()
            if (result != null) return result
        }

        return null
    }

    private fun buildCacheKey(query: String, quantity: Double, unit: String): String {
        return "${normalize(query)}|${quantity}|${normalize(unit)}"
    }

    fun isVegetarianFoodName(name: String): Boolean {
        val normalized = normalize(name)
        return nonVegKeywords.none { keyword -> normalized.contains(normalize(keyword)) }
    }

    private fun normalize(input: String): String {
        return input
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9 ]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}

