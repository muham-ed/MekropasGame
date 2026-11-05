   # Project Plan

Mekropas (ميكروباص المنوفية) Android Arcade Game - Phase 1 MVP Prototype

## Project Brief

Mekropas (ميكروباص المنوفية) MVP Phase 1 Prototype Architecture & Technical Implementation Plan:

Features to build:
1. Step 1: High-Performance SurfaceView Game Engine & Game Loop (60 FPS thread, surface lifecycle, render loop).
2. Step 2: Microbus rendering & interactive touch controls (Left/Right touch steering, road boundary calculation, smooth movement).
3. Step 3: NPC Traffic Vehicle engine (dynamic spawning at top, downward movement along road lanes, list management).
4. Step 4: Collision Detection System & Game Over / Stop state (AABB bounding box collision between player microbus and traffic cars).
5. Step 5: Jetpack Compose HUD overlay integration (`AndroidView` holding `GameView` in a Compose `Box`, Material 3 top HUD bar displaying fare "الأجرة", route progress, and Klakson horn button).
6. Step 6: Passenger Pickup & Fare Calculation Mechanics (Roadside station stops, picking up commuters, fare incrementing, HUD state update, trip stats).
7. Verification & Testing: Build, run connected tests/unit tests, ensure full Material 3, Arabic UX, edge-to-edge support, and smooth 60 FPS performance.

## Implementation Steps
**Total Duration:** 1h 12m 3s

### Task_1_GameCoreEngine: Build core SurfaceView game engine, 60 FPS game loop thread, scrolling road canvas rendering, player microbus entity, and touch controls for left/right lane steering.
- **Status:** COMPLETED
- **Updates:** Successfully built GameThread 60 FPS loop, GameView SurfaceHolder lifecycle, Road scrolling canvas rendering, and PlayerMicrobus entity with touch controls. Verified build via assembleDebug.
- **Acceptance Criteria:**
  - SurfaceView and game thread created with clean lifecycle management
  - Scrolling road with lane markings rendered cleanly
  - Microbus entity rendered with smooth touch steering within road boundaries
- **Duration:** 1h 5m 49s

### Task_2_TrafficAndCollision: Implement dynamic NPC traffic vehicle spawning/movement engine and AABB collision detection system with game over state.
- **Status:** COMPLETED
- **Updates:** Implemented TrafficVehicle entity with multi-vehicle types (Taxi, Truck, TukTuk, Microbus), TrafficManager for dynamic lane spawning and movement, AABB collision detection, and Game Over canvas overlay with touch restart. Verified with assembleDebug and testDebugUnitTest.
- **Acceptance Criteria:**
  - NPC vehicles spawn dynamically across road lanes and move downward
  - AABB collision detection between player microbus and traffic vehicles triggers Game Over state
  - Game state supports reset/restart upon game over
- **Duration:** 2m 43s

### Task_3_PassengersAndFareMechanics: Implement roadside passenger pickup stations, commuter pickup mechanics, fare calculation system ('الأجرة'), and trip progress logic.
- **Status:** COMPLETED
- **Updates:** Implemented PassengerStation with Menofia route landmarks (Shibin El Kom, Tomlai, Damlej, El Shohada), StationManager for passenger pickup alignment, fare calculation (+25 EGP popups), passenger count, dynamic route progress tracking, and Compose HUD overlay. Verified build via assembleDebug and unit tests.
- **Acceptance Criteria:**
  - Roadside passenger pickup zones spawn along the route
  - Aligning microbus with station collects commuters and increments total fare
  - Fare counter and route progress update in real time
- **Duration:** 3m 31s

### Task_4_ComposeHUDAndTheme: Integrate SurfaceView GameView into Jetpack Compose with Material 3 top HUD bar (Arabic UX, fare 'الأجرة', route progress, Klakson horn button), vibrant energetic theme, edge-to-edge display, and adaptive app icon.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - AndroidView embeds SurfaceView inside Compose Box with edge-to-edge layout
  - Material 3 HUD displays Arabic labels ('الأجرة', Klakson horn button, game over overlay)
  - Vibrant M3 theme and custom adaptive app icon created
- **StartTime:** 2026-11-03 23:31:49 EET

### Task_5_RunAndVerify: Build project, execute unit/instrumented tests, verify application stability, smooth 60 FPS rendering, Arabic UX, and ensure no crashes or UI glitches. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** PENDING
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - 60 FPS game loop and full Mekropas gameplay verified without crashes

