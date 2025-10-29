package com.julianotalora.features.countriesdatasdk.api

import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
  val name: NameDto,
  val capital: List<String> = emptyList(),
  val cca3: String,
  val region: String,
  val subregion: String? = null,
  val languages: Map<String, String> = emptyMap(),
  val currencies: Map<String, CurrencyDto> = emptyMap(),
  val population: Long,
  val car: CarDto,
  val flags: FlagsDto
) {

  val formattedCurrency: String
    get() {
      val firstCurrencyEntry = currencies.entries.firstOrNull()
      return firstCurrencyEntry?.let { entry ->
        val code = entry.key
        val currencyName = entry.value.name
        //"$code ($currencyName)"
        code
      } ?: ""
    }

}

@Serializable
data class NameDto(
  val common: String,
  val official: String,
  val nativeName: Map<String, NativeNameDto> = emptyMap()
)

@Serializable
data class NativeNameDto(
  val official: String,
  val common: String
)

@Serializable
data class CurrencyDto(
  val name: String,
  val symbol: String? = null
)

@Serializable
data class CarDto(
  val signs: List<String> = emptyList(),
  val side: String
)

@Serializable
data class FlagsDto(
  val png: String,
  val svg: String,
  val alt: String? = null
)
