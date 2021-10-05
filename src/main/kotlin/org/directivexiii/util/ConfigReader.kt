package org.directivexiii.util

import com.typesafe.config.ConfigFactory
import io.ktor.config.HoconApplicationConfig

fun fetchProperty(property: String): String?{
    val config = HoconApplicationConfig(ConfigFactory.load())
    return config.propertyOrNull(property)?.getString()
}
