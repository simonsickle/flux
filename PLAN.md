# Flux - Android Streaming App Plan

## Context

Build "Flux," a native Android streaming app (like Stremio/Omni) using Kotlin + Jetpack Compose. The app supports both mobile and Android TV in a single APK, implements the Stremio addon protocol for content discovery and streaming, integrates Real-Debrid for premium stream resolution, and plays content via Media3/ExoPlayer (primary) or VLC (fallback). Future phases add native APK plugins for local Usenet streaming with P2P stream coordination.

The project directory (`/Users/simonsickle/development/flux`) is currently empty.

---

## Architecture Overview

**Stack**: Kotlin, Jetpack Compose, Compose TV, Media3 1.9.x, Hilt, Retrofit, Room, DataStore, Coil 3, kotlinx-serialization

**Pattern**: Clean Architecture (MVVM) with multi-module Gradle build

**UI Strategy**: Single APK with `CompositionLocal`-based TV detection. Each feature has a shared ViewModel + Route composable that branches to `MobileXxxScreen` or `TvXxxScreen`.

### Module Structure
```
:app                          - Application entry, navigation, theme, DI wiring
:core:model                   - Pure Kotlin domain entities (no Android deps)
:core:network                 - OkHttp client, JSON config
:core:database                - Room DB, DAOs, entities
:core:common                  - Utilities, TV detection, dispatchers, SettingsRepository
:core:player                  - PlayerEngine interface, Media3 + VLC implementations
:data:addon                   - Stremio addon protocol client + repository
:data:debrid                  - Real-Debrid API (Retrofit) + repository
:domain                       - Repository interfaces, use cases
:feature:home                 - Catalog browsing (home screen)
:feature:detail               - Content metadata, season/episode picker
:feature:player               - Video player screen + controls
:feature:search               - Cross-addon search
:feature:settings             - App settings, debrid config
:feature:addons               - Addon browser/installer/manager
```

**Dependency rule**: Features -> Domain -> Core:Model. Data modules implement Domain interfaces. App module wires everything via Hilt. Features never depend on Data modules directly.

### Key Design Decisions
- **Addon API client**: Raw OkHttp + kotlinx-serialization (NOT Retrofit) because each addon has a different base URL
- **Debrid API client**: Retrofit (single fixed base URL, clean interceptor-based auth)
- **Player**: Interface abstraction over Media3 (default) and VLC (fallback/opt-in)
- **Settings**: DataStore (not Room) for key-value preferences
- **Build config**: Convention plugins in `/build-logic/convention/` for DRY module setup

---

## Phase 1: Project Scaffolding & Core Infrastructure

### 1.1 Gradle Setup
- Create `settings.gradle.kts` with all module includes
- Create `gradle/libs.versions.toml` version catalog (AGP 9.1.0, Kotlin 2.1.x, Compose BOM 2026.03.01, Media3 1.9.3, Hilt 2.55, Room 2.7.1, etc.)
- Create `build-logic/convention/` with shared plugins: `FluxAndroidLibraryPlugin`, `FluxComposePlugin`, `FluxHiltPlugin`, `FluxFeaturePlugin`
- Root `build.gradle.kts` applying plugins with `apply false`

### 1.2 App Module
- `FluxApplication.kt` (@HiltAndroidApp)
- `MainActivity.kt` - Single activity, both `LAUNCHER` and `LEANBACK_LAUNCHER` categories
- `AndroidManifest.xml` - Internet permission, leanback feature (not required), touchscreen (not required), TV banner
- `FluxNavHost.kt` - Compose Navigation with routes: home, detail/{type}/{id}, player, search, settings, addons
- Theme: `FluxTheme.kt`, `Color.kt`, `Type.kt` - Dark-first design, TV typography uses larger base sizes (18sp vs 14sp body)

### 1.3 Core Modules
- **:core:model** - `AddonManifest`, `InstalledAddon`, `MetaPreview`, `MetaDetail`, `Video`, `StreamInfo`, `SubtitleInfo`, `ContentType` enum, `PosterShape` enum
- **:core:network** - Hilt-provided `OkHttpClient` (30s connect/60s read), `Json` instance (ignoreUnknownKeys, isLenient), `FluxHttpClient` wrapper with `suspend fun getJson<T>(url)`
- **:core:database** - `FluxDatabase` (Room) with `InstalledAddonEntity`, `WatchHistoryEntity`, `BookmarkEntity`. DAOs: `AddonDao`, `WatchHistoryDao`, `BookmarkDao`
- **:core:common** - `LocalIsTv` CompositionLocal, `PlatformDetector`, Result sealed class, coroutine dispatcher provider

### Files (~35)
Key: `/gradle/libs.versions.toml`, `/settings.gradle.kts`, `/app/src/main/AndroidManifest.xml`, `/app/.../MainActivity.kt`, `/app/.../FluxNavHost.kt`, `/core/model/.../StreamInfo.kt`, `/core/network/.../FluxHttpClient.kt`, `/core/database/.../FluxDatabase.kt`

---

## Phase 2: Stremio Addon Support & Content Browsing

### 2.1 Addon Data Layer (`:data:addon`)
- `StremioAddonApi.kt` - Builds URLs dynamically per addon: `/manifest.json`, `/catalog/{type}/{id}.json`, `/meta/{type}/{id}.json`, `/stream/{type}/{videoID}.json`, `/subtitles/{type}/{id}.json`
- DTO classes (`@Serializable`): `AddonManifestDto`, `CatalogResponseDto`, `MetaResponseDto`, `StreamResponseDto`, `SubtitleResponseDto`
- `AddonMapper.kt` - DTO-to-domain mapping
- `AddonRepositoryImpl.kt` - Implements `AddonRepository` interface from `:domain`

### 2.2 Domain Use Cases
- `GetAggregatedCatalogUseCase` - Fetches catalogs from ALL installed addons in parallel, returns `List<CatalogRow>` (addonName, catalogName, items)
- `GetContentDetailUseCase` - Routes to addon by `idPrefixes` match, falls back to querying all
- `GetStreamsUseCase` - Queries ALL addons supporting `stream` for the content type, merges results
- `InstallAddonUseCase` - Validates manifest, checks duplicates, persists to Room

### 2.3 Home Feature (`:feature:home`)
- `HomeViewModel` - StateFlow of selected content type tab + catalog rows + loading/error
- `MobileHomeScreen` - LazyColumn of horizontal LazyRows (poster cards), type filter chips, pull-to-refresh
- `TvHomeScreen` - `TvLazyColumn`/`TvLazyRow`, `ImmersiveList` hero banner, `Card` from tv-material with focus handling

### 2.4 Detail Feature (`:feature:detail`)
- `DetailViewModel` - Loads meta from best-matching addon, season/episode for series
- `MobileDetailScreen` - Collapsing toolbar, backdrop, poster, metadata, Play button
- `TvDetailScreen` - Full-bleed background, info overlay, episode browser

### Files (~30)
Key: `/data/addon/.../StremioAddonApi.kt`, `/data/addon/.../AddonRepositoryImpl.kt`, `/domain/.../GetAggregatedCatalogUseCase.kt`, `/feature/home/.../HomeViewModel.kt`

---

## Phase 3: Video Playback

### 3.1 Core Player (`:core:player`)
- `PlayerEngine` interface - `prepare(uri, startPosition)`, `play()`, `pause()`, `seekTo()`, `setSubtitle()`, `release()`, `state: StateFlow<PlaybackState>`
- `Media3PlayerEngine` - Wraps ExoPlayer, exposes raw `Player` for `media3-ui-compose` `PlayerSurface`
- `VlcPlayerEngine` - Stub initially, full implementation in Phase 6
- `PlayerEngineFactory` - Creates engine based on user preference or fallback

### 3.2 Player Feature (`:feature:player`)
- `PlayerViewModel` - Manages engine lifecycle, stream fallback, subtitle selection, watch progress tracking
- `MobilePlayerScreen` - `PlayerSurface` composable, custom overlay controls, gesture support (swipe seek/brightness/volume), landscape lock
- `TvPlayerScreen` - `PlayerSurface`, D-pad controls (center=play/pause, left/right=seek), auto-hide overlay, "Next Episode" prompt
- Subtitle handling via `MediaItem.SubtitleConfiguration` for external subs

### Files (~20)
Key: `/core/player/.../PlayerEngine.kt`, `/core/player/.../Media3PlayerEngine.kt`, `/feature/player/.../PlayerViewModel.kt`, `/feature/player/.../MobilePlayerScreen.kt`, `/feature/player/.../TvPlayerScreen.kt`

---

## Phase 4: Real-Debrid Integration

### 4.1 Debrid Data Layer (`:data:debrid`)
- `RealDebridApi.kt` (Retrofit) - Endpoints: `/user`, `/unrestrict/link`, `/unrestrict/check`, `/torrents/addMagnet`, `/torrents/selectFiles/{id}`, `/torrents/info/{id}`, `/torrents/instantAvailability/{hash}`
- `RealDebridAuthInterceptor` - Injects Bearer token from DataStore settings
- `DebridRepositoryImpl` - Core resolution logic:
  1. `infoHash` streams: check instant availability -> addMagnet -> selectFiles -> get direct HTTPS link
  2. `url` streams: unrestrict/link -> get direct HTTPS link
  3. Returns `ResolvedStream(url, filename, filesize, mimeType)`

### 4.2 Stream Resolution Pipeline
- `ResolveAndPlayStreamUseCase` - If debrid configured + stream is torrent/restricted: resolve through debrid. Direct URLs pass through as-is.
- Enhance stream selection: batch-check `instantAvailability`, mark cached streams "[RD+]", sort cached first

### Files (~15)
Key: `/data/debrid/.../RealDebridApi.kt`, `/data/debrid/.../DebridRepositoryImpl.kt`, `/domain/.../ResolveAndPlayStreamUseCase.kt`

---

## Phase 5: Search, Settings, Addon Management & Polish

### 5.1 Search (`:feature:search`)
- Debounced (300ms) query across all addons with `search` extra support
- Deduplicate by content ID, group by type
- Mobile: SearchBar + grid. TV: on-screen keyboard + TvLazyVerticalGrid

### 5.2 Settings (`:feature:settings`)
- **General**: Default content type, subtitle language
- **Player**: Preferred engine (Media3/VLC), hardware acceleration
- **Real-Debrid**: Token input, account info (username, expiry, points), test connection
- **About**: Version, licenses
- Stored via `SettingsRepository` backed by DataStore

### 5.3 Addon Management (`:feature:addons`)
- List installed addons with reorder (drag on mobile, up/down on TV)
- Install by pasting transport URL
- Remove with undo. Configure via WebView for configurable addons
- View manifest details

### 5.4 TV Polish
- Focus management: `Modifier.focusRestorer()` on lazy lists
- TV safe area padding (48dp sides, 27dp top/bottom)
- Audio focus management in player
- Leanback banner asset

### 5.5 Watch History
- Track last played position per content ID in Room
- "Continue Watching" row as first row on home screen
- Resume from saved position on play

### Files (~25)

---

## Phase 6: VLC Fallback (Full Implementation)

- Complete `VlcPlayerEngine` using `org.videolan.android:libvlc-all`
- `VlcVideoSurface` composable (AndroidView wrapping SurfaceView for libVLC)
- Auto-fallback: if Media3 throws `ERROR_CODE_DECODER_INIT_FAILED`, prompt "Try VLC?"

---

## Phase 7 (Future): Native APK Plugins & P2P Coordination

### 7.1 Plugin Architecture
- AIDL `IFluxPlugin` interface: `getManifest()`, `getCatalog()`, `getMeta()`, `getStreams()` - all return JSON strings
- Plugin discovery via `PackageManager` query for services with intent `dev.simonsickle.flux.PLUGIN`
- Security: signature verification, explicit user enable per plugin

### 7.2 Usenet Plugin Example
- Separate APK implementing `IFluxPlugin`
- Downloads NZB, connects to NNTP server, streams by downloading segments in playback order
- Exposes local HTTP server (NanoHTTPD on localhost), returns `url: "http://127.0.0.1:{port}/stream"`

### 7.3 P2P Stream Coordination
- **Local network**: Android `NsdManager` (mDNS) for discovering other Flux instances, negotiate connection allocation (e.g., 20 NNTP connections split between 2 devices)
- **Cross-network**: Lightweight signaling server (WebSocket or Firebase) for coordination across locations
- Each device broadcasts current stream count; new streamers negotiate before connecting

---

## Verification Plan

1. **Phase 1**: `./gradlew assembleDebug` compiles. App launches on phone emulator and TV emulator. Theme applies correctly on both.
2. **Phase 2**: Install a known Stremio addon (e.g., Cinemeta `https://v3-cinemeta.strem.io`). Verify catalogs load on home screen, detail screen shows metadata, seasons/episodes for series.
3. **Phase 3**: Select a stream with a direct URL. Verify Media3 plays it with controls working on both mobile (gestures) and TV (D-pad).
4. **Phase 4**: Configure Real-Debrid token in settings. Install Torrentio addon. Verify cached torrents show "[RD+]", selecting one resolves to direct HTTPS and plays.
5. **Phase 5**: Search for content across addons. Verify settings persist across restart. Verify addon install/remove/reorder. Verify watch history resume.
6. **Phase 6**: Play a stream that requires VLC codec. Verify fallback prompt and VLC playback.
