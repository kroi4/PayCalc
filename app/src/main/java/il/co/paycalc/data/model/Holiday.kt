package il.co.paycalc.data.model

import java.time.LocalDate

data class Holiday(
    val name: String,
    var startDate: LocalDate,
    var endDate: LocalDate
)
