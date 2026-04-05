package dev.simonsickle.flux.core.model

data class AddonManifest(
    val id: String,
    val version: String,
    val name: String,
    val description: String,
    val logo: String? = null,
    val background: String? = null,
    val contactEmail: String? = null,
    val resources: List<String>,
    val types: List<String>,
    val idPrefixes: List<String>,
    val catalogs: List<CatalogEntry>,
    val addonCatalogs: List<CatalogEntry> = emptyList(),
    val behaviorHints: BehaviorHints = BehaviorHints(),
    val config: List<AddonConfig> = emptyList()
)

data class AddonConfig(
    val key: String,
    val type: String,
    val title: String? = null,
    val default: String? = null,
    val options: List<String> = emptyList(),
    val required: Boolean = false
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
    val options: List<String> = emptyList(),
    val optionsLimit: Int = 1
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
    val orderIndex: Int = 0,
    val timeoutMs: Long = DEFAULT_ADDON_TIMEOUT_MS
) {
    companion object {
        const val DEFAULT_ADDON_TIMEOUT_MS = 15_000L
    }
}
