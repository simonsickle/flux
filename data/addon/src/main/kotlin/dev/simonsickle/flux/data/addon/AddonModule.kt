package dev.simonsickle.flux.data.addon

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.simonsickle.flux.domain.repository.AddonRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AddonModule {

    @Binds
    @Singleton
    abstract fun bindAddonRepository(impl: AddonRepositoryImpl): AddonRepository
}
