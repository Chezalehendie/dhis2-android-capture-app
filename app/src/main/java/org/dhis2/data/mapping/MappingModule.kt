package org.dhis2.data.mapping

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MappingModule {
    @Provides
    @Singleton
    fun provideMappingService(): MappingService {
        val mappingConfigs = listOf<MappingConfig>()
        return MappingServiceImpl(mappingConfigs)
    }
}