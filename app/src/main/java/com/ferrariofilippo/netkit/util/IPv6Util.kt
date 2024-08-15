/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import com.ferrariofilippo.netkit.Constants.HEXTET_IN_ADDRESS
import com.ferrariofilippo.netkit.Constants.LAST_HEXTET_INDEX
import com.ferrariofilippo.netkit.Constants.OMISSION_LENGTH_KEY
import com.ferrariofilippo.netkit.Constants.OMISSION_START_KEY
import com.ferrariofilippo.netkit.Constants.ZERO_STR

object IPv6Util {
    fun compressAddress(components: Array<String>, length: Int): String {
        val params = arrayOf(0, 0)
        val builder = StringBuilder(LAST_HEXTET_INDEX + HEXTET_IN_ADDRESS * 4)

        for (i in 0 until length) {
            components[i] =
                if (components[i].isNullOrBlank()) ZERO_STR else omitLeadingZeroes(components[i])
        }

        var j = 0
        while (j < length) {
            val i = j
            var size = 0

            while (j < length && components[j] == ZERO_STR) {
                ++j
                ++size
            }

            if (size > params[OMISSION_LENGTH_KEY]) {
                params[OMISSION_START_KEY] = i
                params[OMISSION_LENGTH_KEY] = size
            }

            ++j
        }

        for (i in 0 until params[OMISSION_LENGTH_KEY]) {
            components[i + params[OMISSION_START_KEY]] = ZERO_STR
        }

        j = 0
        while (j < length) {
            if (components[j].isEmpty()) {
                if (j == 0) {
                    builder.append("::")
                } else {
                    builder.append(":")
                }

                if (params[OMISSION_LENGTH_KEY] != 0) {
                    j += params[OMISSION_LENGTH_KEY] - 1
                }
            } else {
                builder.append(components[j])
                if (j != LAST_HEXTET_INDEX) {
                    builder.append(":")
                }
            }

            ++j
        }

        return builder.toString()
    }

    fun expandAddress(compressed: List<String>): Array<String> {
        val result = mutableListOf<String>()
        val removedSegmentsCount = HEXTET_IN_ADDRESS - compressed.size

        for (i in compressed.indices) {
            if (compressed[i].isEmpty()) {
                for (j in 0 until removedSegmentsCount) {
                    result.add(ZERO_STR)
                }
            } else {
                result.add(compressed[i])
            }
        }

        return result.toTypedArray()
    }

    fun validateAndSplitAddress(address: String?, segments: Array<String>): Boolean {
        if (address.isNullOrBlank() || !isStringHexOrColon(address.uppercase())) {
            return false
        }

        val split = address.split(':')
        if (split.size != HEXTET_IN_ADDRESS || segments.size != HEXTET_IN_ADDRESS) {
            return false
        }

        for (i in split.indices) {
            segments[i] = split[i]
        }

        return true
    }

    fun isStringHexOrColon(address: String): Boolean {
        for (c in address) {
            if (((c < '0' || c > '9') && (c < 'A' || c > 'F')) && c != ':') {
                return false
            }
        }

        return true
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun getFormattedAddress(unformattedAddress: Array<String>, value: UInt, index: Int): String {
        val components = Array(HEXTET_IN_ADDRESS) { "" }
        for (i in components.indices) {
            components[i] =
                if (i != index) unformattedAddress[i] else value.toHexString(HexFormat.UpperCase)
        }

        return compressAddress(components, HEXTET_IN_ADDRESS)
    }

    private fun omitLeadingZeroes(segment: String): String {
        var i = 0
        val indexUpperBounds = segment.length - 1
        while (segment.length != 1 && i < indexUpperBounds && segment[i] == '0') {
            i++
        }

        return segment.substring(i)
    }
}
