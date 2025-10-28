import com.julianotalora.features.countriesdatasdk.api.CarDto
import com.julianotalora.features.countriesdatasdk.api.CountryDto
import com.julianotalora.features.countriesdatasdk.api.CurrencyDto
import com.julianotalora.features.countriesdatasdk.api.FlagsDto
import com.julianotalora.features.countriesdatasdk.api.NameDto
import com.julianotalora.features.countriesdatasdk.internal.db.CountryEntity

// Remove all imports to other modules and only use internal SDK classes

fun CountryDto.toEntity(): CountryEntity {
    return CountryEntity(
        cca3 = cca3,
        commonName = name.common,
        officialName = name.official,
        capital = capital.firstOrNull() ?: "",
        region = region,
        subRegion = subregion,
        languages = languages.values.joinToString(", "),
        currencies = currencies.values.joinToString(", ") { it.name },
        population = population,
        carDriverSide = car.side,
        flagUrl = flags.png,
        searchName = name.common.lowercase().replace("[^a-z0-9]".toRegex(), "")
    )
}

// Map Entity to DTO (for SDK internal use)
fun CountryEntity.toDto(): CountryDto {
    return CountryDto(
        name = NameDto(
            common = commonName,
            official = officialName,
            nativeName = emptyMap()
        ),
        capital = if (capital.isEmpty()) emptyList() else listOf(capital),
        cca3 = cca3,
        region = region,
        subregion = subRegion,
        languages = languages.split(", ").associate { it to it },
        currencies = currencies.split(", ").associate { it to CurrencyDto(it, null) },
        population = population,
        car = CarDto(emptyList(), carDriverSide),
        flags = FlagsDto(flagUrl, "", null)
    )
}
