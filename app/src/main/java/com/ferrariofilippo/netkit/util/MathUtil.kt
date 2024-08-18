/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import com.ferrariofilippo.netkit.Constants.BITS_IN_IPv4_ADDRESS

object MathUtil {
    val powersOfTwo: Array<UInt>

    init {
        val powers = mutableListOf<UInt>()

        for (i in 0 until BITS_IN_IPv4_ADDRESS) {
            powers.add(1u shl i)
        }

        powersOfTwo = powers.toTypedArray()
    }

    fun getCeilBaseTwoLog(n: UInt): Int {
        var index = -1
        while (index < BITS_IN_IPv4_ADDRESS && n > powersOfTwo[++index]);

        return index
    }

    fun getFloorBaseTwoLog(n: UInt): Int {
        var index = BITS_IN_IPv4_ADDRESS
        while (index > 0 && n < powersOfTwo[--index]);

        return index
    }
}
