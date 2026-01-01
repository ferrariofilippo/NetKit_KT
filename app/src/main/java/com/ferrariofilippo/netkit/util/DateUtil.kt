/*
 * Copyright (c) 2026 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import java.time.LocalDate
import java.time.format.DateTimeParseException

object DateUtil {
    fun String.toLocalDateOrNull(): LocalDate? {
        try {
            return LocalDate.parse(this)
        } catch (e: DateTimeParseException) {
        }

        return null
    }
}
