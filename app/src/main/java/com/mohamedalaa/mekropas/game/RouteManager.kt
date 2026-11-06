package com.mohamedalaa.mekropas.game

data class RouteModel(
    val id: String,
    val title: String,
    val startTerminal: String,
    val endTerminal: String,
    val totalDistanceMeters: Int,
    val stations: List<String>,
    val asphaltColorHex: String = "#2B2D2F"
)

/**
 * Manages Menofia route configurations.
 */
object RouteManager {

    val routes = listOf(
        RouteModel(
            id = "shebin_shohada",
            title = "شبين الكوم ↔ الشهداء",
            startTerminal = "شبين الكوم",
            endTerminal = "الشهداء",
            totalDistanceMeters = 3000,
            stations = listOf(
                "موقف شبين الكوم",
                "قرية البتانون",
                "قرية طملاي",
                "قرية دمليج",
                "موقف منوف",
                "موقف الشهداء"
            )
        ),
        RouteModel(
            id = "shebin_ashmoun",
            title = "شبين الكوم ↔ أشمون",
            startTerminal = "شبين الكوم",
            endTerminal = "أشمون",
            totalDistanceMeters = 4500,
            stations = listOf(
                "موقف شبين الكوم",
                "سيرس الليان",
                "موقف منوف",
                "قرية سنتريس",
                "موقف أشمون"
            )
        ),
        RouteModel(
            id = "shebin_bagour",
            title = "شبين الكوم ↔ الباجور",
            startTerminal = "شبين الكوم",
            endTerminal = "الباجور",
            totalDistanceMeters = 4000,
            stations = listOf(
                "موقف شبين الكوم",
                "قرية ميت خاقان",
                "قرية اسطنها",
                "سبك الضحاك",
                "موقف الباجور"
            )
        )
    )

    fun getRouteById(id: String): RouteModel {
        return routes.find { it.id == id } ?: routes[0]
    }
}
