package com.mohamedalaa.mekropas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mohamedalaa.mekropas.game.GameStorage
import com.mohamedalaa.mekropas.game.GameView
import com.mohamedalaa.mekropas.game.RouteManager
import com.mohamedalaa.mekropas.game.SoundManager
import com.mohamedalaa.mekropas.ui.GarageScreen
import com.mohamedalaa.mekropas.ui.MainMenuScreen
import com.mohamedalaa.mekropas.ui.RouteSelectionScreen
import com.mohamedalaa.mekropas.ui.theme.MekropasTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class Screen {
    MAIN_MENU,
    GARAGE,
    ROUTE_SELECTION,
    GAME
}

class MainActivity : ComponentActivity() {
    private var soundManager: SoundManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sound = SoundManager(applicationContext)
        soundManager = sound

        setContent {
            MekropasTheme {
                MekropasAppNavigation(sound)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager?.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager?.release()
        soundManager = null
    }
}

@Composable
fun MekropasAppNavigation(soundManager: SoundManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gameStorage = remember { GameStorage(context) }

    var currentScreen by remember { mutableStateOf(Screen.MAIN_MENU) }

    val highestFare by gameStorage.highestFareFlow.collectAsState(initial = 0)
    val totalCoins by gameStorage.totalCoinsFlow.collectAsState(initial = 0)
    val ownedVans by gameStorage.ownedVansFlow.collectAsState(initial = setOf(GameStorage.DEFAULT_VAN))
    val selectedVanId by gameStorage.selectedVanFlow.collectAsState(initial = GameStorage.DEFAULT_VAN)
    val selectedRouteId by gameStorage.selectedRouteFlow.collectAsState(initial = GameStorage.DEFAULT_ROUTE)

    when (currentScreen) {
        Screen.MAIN_MENU -> {
            MainMenuScreen(
                highestFare = highestFare,
                totalCoins = totalCoins,
                onStartGameClick = { currentScreen = Screen.GAME },
                onGarageClick = { currentScreen = Screen.GARAGE },
                onRouteSelectClick = { currentScreen = Screen.ROUTE_SELECTION }
            )
        }
        Screen.GARAGE -> {
            GarageScreen(
                totalCoins = totalCoins,
                ownedVans = ownedVans,
                selectedVanId = selectedVanId,
                onSelectVan = { vanId ->
                    scope.launch { gameStorage.setSelectedVan(vanId) }
                },
                onBuyVan = { van ->
                    scope.launch { gameStorage.unlockVan(van.id, van.costEgp) }
                },
                onBackClick = { currentScreen = Screen.MAIN_MENU }
            )
        }
        Screen.ROUTE_SELECTION -> {
            RouteSelectionScreen(
                selectedRouteId = selectedRouteId,
                onSelectRoute = { routeId ->
                    scope.launch { gameStorage.setSelectedRoute(routeId) }
                },
                onStartGame = { currentScreen = Screen.GAME },
                onBackClick = { currentScreen = Screen.MAIN_MENU }
            )
        }
        Screen.GAME -> {
            MekropasGameScreen(
                gameStorage = gameStorage,
                soundManager = soundManager,
                selectedVanId = selectedVanId,
                selectedRouteId = selectedRouteId,
                onExitGame = {
                    soundManager.pauseMusic()
                    currentScreen = Screen.MAIN_MENU
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MekropasGameScreen(
    gameStorage: GameStorage,
    soundManager: SoundManager,
    selectedVanId: String,
    selectedRouteId: String,
    onExitGame: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var gameViewInstance by remember { mutableStateOf<GameView?>(null) }

    var fareAmount by remember { mutableIntStateOf(0) }
    var passengerCount by remember { mutableIntStateOf(0) }
    var progressPercent by remember { mutableFloatStateOf(0f) }
    var distanceMeters by remember { mutableIntStateOf(0) }

    val currentRoute = remember(selectedRouteId) {
        RouteManager.getRouteById(selectedRouteId)
    }

    LaunchedEffect(gameViewInstance) {
        while (isActive) {
            gameViewInstance?.let { view ->
                fareAmount = view.totalFareEgp
                passengerCount = view.totalPassengers
                progressPercent = view.routeProgressPercent
                distanceMeters = view.distanceMeters
            }
            delay(50)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🚐 ${currentRoute.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExitGame) {
                        Text(text = "🏠", fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    GameView(context).also { view ->
                        view.configureEngine(gameStorage, soundManager, scope)
                        view.applyVanAndRoute(selectedVanId, selectedRouteId)
                        gameViewInstance = view
                    }
                }
            )

            // Top HUD Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF1B5E20),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💵 الأجرة: $fareAmount ج.م",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF0D47A1),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "👥 الركاب: $passengerCount",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFE65100),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🛣️ $distanceMeters م",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentRoute.startTerminal,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "التقدم: ${progressPercent.toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = currentRoute.endTerminal,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // Klakson FAB
            FloatingActionButton(
                onClick = {
                    gameViewInstance?.triggerHorn()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Text(
                    text = "🔊 كلاكس",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}
