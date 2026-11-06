package com.mohamedalaa.mekropas.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mekropas_storage")

/**
 * Handles persistent storage for player statistics, currency, owned microbuses, and high scores.
 */
class GameStorage(private val context: Context) {

    companion object {
        val KEY_HIGHEST_FARE = intPreferencesKey("highest_fare")
        val KEY_TOTAL_COINS = intPreferencesKey("total_coins")
        val KEY_OWNED_VANS = stringSetPreferencesKey("owned_vans")
        val KEY_SELECTED_VAN = stringPreferencesKey("selected_van")
        val KEY_SELECTED_ROUTE = stringPreferencesKey("selected_route")

        const val DEFAULT_VAN = "suzuki_8"
        const val DEFAULT_ROUTE = "shebin_shohada"
    }

    val highestFareFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_HIGHEST_FARE] ?: 0
    }

    val totalCoinsFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOTAL_COINS] ?: 0
    }

    val ownedVansFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_OWNED_VANS] ?: setOf(DEFAULT_VAN)
    }

    val selectedVanFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_VAN] ?: DEFAULT_VAN
    }

    val selectedRouteFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SELECTED_ROUTE] ?: DEFAULT_ROUTE
    }

    suspend fun recordTripResult(fareEgp: Int) {
        context.dataStore.edit { prefs ->
            val currentHighest = prefs[KEY_HIGHEST_FARE] ?: 0
            if (fareEgp > currentHighest) {
                prefs[KEY_HIGHEST_FARE] = fareEgp
            }
            val currentCoins = prefs[KEY_TOTAL_COINS] ?: 0
            prefs[KEY_TOTAL_COINS] = currentCoins + fareEgp
        }
    }

    suspend fun unlockVan(vanId: String, cost: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentCoins = prefs[KEY_TOTAL_COINS] ?: 0
            if (currentCoins >= cost) {
                prefs[KEY_TOTAL_COINS] = currentCoins - cost
                val owned = (prefs[KEY_OWNED_VANS] ?: setOf(DEFAULT_VAN)).toMutableSet()
                owned.add(vanId)
                prefs[KEY_OWNED_VANS] = owned
                prefs[KEY_SELECTED_VAN] = vanId
                success = true
            }
        }
        return success
    }

    suspend fun setSelectedVan(vanId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_VAN] = vanId
        }
    }

    suspend fun setSelectedRoute(routeId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_ROUTE] = routeId
        }
    }
}
