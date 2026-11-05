package com.mohamedalaa.mekropas.game

import android.graphics.Canvas
import kotlin.random.Random

/**
 * Manages periodic roadside passenger pickup station spawning, alignment detection,
 * commuter collection, fare accumulation, and floating fare animation popups.
 */
class StationManager {

    val activeStations = mutableListOf<PassengerStation>()
    val farePopups = mutableListOf<FarePopup>()

    var totalFareEgp: Int = 0
        private set
    var totalPassengers: Int = 0
        private set
    var lastCollectedStationName: String = ""
        private set

    private var distanceSinceLastStation = 0f
    private val stationSpawnDistance = 850f // Distance in scroll pixels between stations

    private val stationNames = listOf(
        "موقف شبين الكوم",
        "قرية البتانون",
        "قرية طملاي",
        "قرية دمليج",
        "موقف منوف",
        "موقف الشهداء"
    )
    private var stationIndex = 0

    fun update(road: Road, player: PlayerMicrobus) {
        if (road.width <= 0f || road.height <= 0f) return

        // 1. Station Spawning Logic
        distanceSinceLastStation += road.scrollSpeed
        if (distanceSinceLastStation >= stationSpawnDistance) {
            distanceSinceLastStation = 0f
            spawnStation(road)
        }

        // 2. Update Active Stations & Check Passenger Collection
        val laneWidth = road.roadWidth / road.laneCount
        val rightLaneBoundary = road.roadRight - laneWidth

        val iterator = activeStations.iterator()
        while (iterator.hasNext()) {
            val station = iterator.next()
            station.update(road.scrollSpeed)

            // Check if player microbus is aligned with station zone
            if (!station.isCollected) {
                val isYAligned = kotlin.math.abs(player.y - station.y) < 130f
                val isInRightLane = player.x >= rightLaneBoundary

                if (isYAligned && isInRightLane) {
                    // Collect passengers!
                    station.collect()
                    val earnedFare = station.totalFare
                    totalFareEgp += earnedFare
                    totalPassengers += station.commuterCount
                    lastCollectedStationName = station.name

                    // Create floating fare text animation
                    farePopups.add(
                        FarePopup(
                            text = "+$earnedFare ج.م الأجرة 💰",
                            x = player.x,
                            y = player.y - 50f
                        )
                    )
                }
            }

            // Clean up station when scrolled past bottom
            if (station.y > road.height + 200f) {
                iterator.remove()
            }
        }

        // 3. Update Floating Fare Text Animations
        val popupIterator = farePopups.iterator()
        while (popupIterator.hasNext()) {
            val popup = popupIterator.next()
            if (!popup.update()) {
                popupIterator.remove()
            }
        }
    }

    private fun spawnStation(road: Road) {
        val name = stationNames[stationIndex % stationNames.size]
        stationIndex++

        val commuters = Random.nextInt(3, 7) // 3 to 6 waiting commuters
        val farePerHead = Random.nextInt(5, 9)  // 5 to 8 EGP per commuter

        val stationX = road.roadRight + 12f
        val stationY = -180f

        activeStations.add(
            PassengerStation(
                name = name,
                x = stationX,
                y = stationY,
                commuterCount = commuters,
                farePerCommuter = farePerHead
            )
        )
    }

    fun draw(canvas: Canvas, road: Road) {
        val laneWidth = road.roadWidth / road.laneCount
        for (station in activeStations) {
            station.draw(canvas, road.roadRight, laneWidth)
        }
        for (popup in farePopups) {
            popup.draw(canvas)
        }
    }

    fun reset() {
        activeStations.clear()
        farePopups.clear()
        totalFareEgp = 0
        totalPassengers = 0
        lastCollectedStationName = ""
        distanceSinceLastStation = 0f
        stationIndex = 0
    }
}
