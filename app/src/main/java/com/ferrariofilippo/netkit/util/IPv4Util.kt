/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import com.ferrariofilippo.netkit.Constants.BITS_IN_BYTE
import com.ferrariofilippo.netkit.Constants.BYTES_IN_ADDRESS
import com.ferrariofilippo.netkit.Constants.LAST_ADDRESS_BIT_INDEX
import com.ferrariofilippo.netkit.Constants.LAST_BIT_INDEX
import com.ferrariofilippo.netkit.Constants.SUBNET_SIZES_COUNT

object IPv4Util {
    private val subnetMaxHosts: Array<UInt>

    init {
        val hostsCount = mutableListOf<UInt>()

        for (i in 0 until SUBNET_SIZES_COUNT) {
            hostsCount.add((1u shl (i + 2)) - 2u)
        }

        subnetMaxHosts = hostsCount.toTypedArray()
    }

    fun tryGetSubnetMask(prefixLength: Int, mask: Array<UByte>): Boolean {
        if (mask.size != BYTES_IN_ADDRESS) {
            return false
        }

        for (i in 0 until BYTES_IN_ADDRESS) {
            for (j in 0 until BITS_IN_BYTE) {
                if (i * BITS_IN_BYTE + j < prefixLength)  {
                    mask[i] = mask[i] or MathUtil.powersOfTwo[LAST_BIT_INDEX - j].toUByte()
                } else {
                    return true
                }
            }
        }

        return true
    }

    fun tryParseAddress(addressString: String?, address: Array<UByte>): Boolean {
        if (addressString.isNullOrBlank() || address.size != BYTES_IN_ADDRESS) {
            return false
        }

        val components = addressString.split('.')
        if (components.size != BYTES_IN_ADDRESS) {
            return false
        }

        for (i in address.indices) {
            val value = components[i].toUByteOrNull() ?: return false

            address[i] = value
        }

        return true
    }

    fun getPrefixLength(hostsCount: UInt): Int {
        var prefix = 0
        while (hostsCount > subnetMaxHosts[prefix++]);

        return LAST_ADDRESS_BIT_INDEX - prefix
    }

    fun getMinimumWasteSize(hostsCount: UInt): UInt {
        var i = 0
        while (i <= SUBNET_SIZES_COUNT && hostsCount > subnetMaxHosts[i++]);

        return if (i > SUBNET_SIZES_COUNT) 0u else subnetMaxHosts[--i]
    }

    fun getHostsCountByPrefixLength(prefixLength: Int): UInt {
        val i = SUBNET_SIZES_COUNT - 1 - prefixLength

        return if (i >= 0) subnetMaxHosts[i] else 0u
    }
}
