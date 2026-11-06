package com.mohamedalaa.mekropas.game

data class VanModel(
    val id: String,
    val name: String,
    val description: String,
    val maxPassengers: Int,
    val speedMultiplier: Float,
    val costEgp: Int,
    val primaryColorHex: String,
    val stripeColorHex: String
)

object GarageManager {

    val vans = listOf(
        VanModel(
            id = "suzuki_8",
            name = "سوزوكي 8 راكب (تناية)",
            description = "ميكروباص سوزوكي سريع ومرن لشوارع القرى الضيقة.",
            maxPassengers = 8,
            speedMultiplier = 1.0f,
            costEgp = 0,
            primaryColorHex = "#FFFFFF",
            stripeColorHex = "#1E88E5"
        ),
        VanModel(
            id = "toyota_hiace",
            name = "تويوتا هايس 14 راكب",
            description = "الميكروباص المصري الأيقوني للخطوط الطويلة والسريعة.",
            maxPassengers = 14,
            speedMultiplier = 1.25f,
            costEgp = 1500,
            primaryColorHex = "#FAFAFA",
            stripeColorHex = "#E53935"
        ),
        VanModel(
            id = "kia_besta",
            name = "كيا بيستا VIP 18 راكب",
            description = "ميكروباص فاخر وسريع بإنارة LED وسعة ركاب ضخمة.",
            maxPassengers = 18,
            speedMultiplier = 1.45f,
            costEgp = 3000,
            primaryColorHex = "#E0F2FE",
            stripeColorHex = "#FF8F00"
        )
    )

    fun getVanById(id: String): VanModel {
        return vans.find { it.id == id } ?: vans[0]
    }
}
