package dev.simonsickle.flux.domain.usecase

import dev.simonsickle.flux.core.model.CatalogRow
import dev.simonsickle.flux.core.model.ContentType
import dev.simonsickle.flux.domain.repository.AddonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class GetAggregatedCatalogUseCase @Inject constructor(
    private val addonRepository: AddonRepository
) {
    suspend operator fun invoke(contentType: ContentType): List<CatalogRow> = coroutineScope {
        val addons = addonRepository.getInstalledAddons().first()
            .filter { it.enabled }

        addons.flatMap { addon ->
            val matchingCatalogs = addon.manifest.catalogs
                .filter { it.type == contentType.value }

            matchingCatalogs.map { catalog ->
                async {
                    withTimeoutOrNull(CATALOG_TIMEOUT_MS) {
                        try {
                            val items = addonRepository.getCatalog(addon, catalog.type, catalog.id)
                            CatalogRow(
                                addonId = addon.manifest.id,
                                addonName = addon.manifest.name,
                                catalogId = catalog.id,
                                catalogName = catalog.name,
                                type = contentType,
                                items = items
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
            }
        }.awaitAll().filterNotNull().filter { it.items.isNotEmpty() }
    }

    companion object {
        private const val CATALOG_TIMEOUT_MS = 15_000L
    }
}
