package com.supernet.fitnesstracker.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Configuration
class MongoConfig {
    @Bean
    fun customConversions(): MongoCustomConversions {
        return MongoCustomConversions(listOf(ZonedDateTimeReadConverter(), ZonedDateTimeWriteConverter()))
    }
}

internal class ZonedDateTimeReadConverter: Converter<Date, ZonedDateTime> {
    override fun convert(source: Date): ZonedDateTime {
        return source.toInstant().atZone(ZoneId.of("UTC"))
    }
}

internal class ZonedDateTimeWriteConverter: Converter<ZonedDateTime, Date> {
    override fun convert(source: ZonedDateTime): Date? {
        return Date.from(source.toInstant())
    }
}