package me.jhan.file.toy.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

@Configuration
class ReactiveMongoConfig : AbstractReactiveMongoConfiguration() {
    override fun getDatabaseName(): String {
        return "fileAPI"
    }

    @Bean
    override fun mappingMongoConverter(
        databaseFactory: ReactiveMongoDatabaseFactory,
        customConversions: MongoCustomConversions, mappingContext: MongoMappingContext
    ): MappingMongoConverter {
        val converter = MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext)
        converter.setCustomConversions(customConversions)
        converter.setCodecRegistryProvider(databaseFactory)
        converter.setMapKeyDotReplacement("-DOT-")
        return converter
    }
}