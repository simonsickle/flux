package dev.simonsickle.flux.core.model

data class AddonManifest(
    val id: String,
    val version: String,
    val name: String,
    val description: String,
    val logo: String? = null,
    val background: String? = null,
    val resources: List<String>,
    val types: List<String>,
    val idPrefixes: List<String>,
    val catalogs: List<CatalogEntry>,
    val behaviorHints: BehaviorHints = BehaviorHints()
)

data class CatalogEntry(
    val type: String,
    val id: String,
    val name: String,
    val extra: List<ExtraEntry> = emptyList()
)

data class ExtraEntry(
    val name: String,
    val isRequired: Boolean = false,
    val options: List<String> = emptyList()
)

data class BehaviorHints(
    val adult: Boolean = false,
    val p2p: Boolean = false,
    val configurable: Boolean = false,
    val configurationRequired: Boolean = false
)

data class InstalledAddon(
    val manifest: AddonManifest,
    val transportUrl: String,
    val enabled: Boolean = true,
    val orderIndex: Int = 0
)
