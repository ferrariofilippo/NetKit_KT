/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import com.ferrariofilippo.netkit.Constants.BITS_IN_BYTE
import com.ferrariofilippo.netkit.Constants.BITS_IN_IPv4_ADDRESS
import com.ferrariofilippo.netkit.Constants.BYTES_IN_ADDRESS
import com.ferrariofilippo.netkit.Constants.LAST_ADDRESS_BIT_INDEX
import com.ferrariofilippo.netkit.model.data.ACE
import com.ferrariofilippo.netkit.model.data.Bounds
import com.ferrariofilippo.netkit.model.enums.NetworkClass

object WildcardUtil {
    private val _evenOrOddWildcard = arrayOf<UByte>(0xFFu, 0xFFu, 0xFFu, 0xFFu)

    private val _classAWildcard = arrayOf<UByte>(0x7Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classBWildcard = arrayOf<UByte>(0x3Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classCWildcard = arrayOf<UByte>(0x1Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classDWildcard = arrayOf<UByte>(0x0Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classEWildcard = arrayOf<UByte>(0x0Fu, 0xFFu, 0xFFu, 0xFFu)

    private val _classASupportIP = arrayOf<UByte>(0x00u, 0x00u, 0x00u, 0x00u)
    private val _classBSupportIP = arrayOf<UByte>(0x80u, 0x00u, 0x00u, 0x00u)
    private val _classCSupportIP = arrayOf<UByte>(0xC0u, 0x00u, 0x00u, 0x00u)
    private val _classDSupportIP = arrayOf<UByte>(0xE0u, 0x00u, 0x00u, 0x00u)
    private val _classESupportIP = arrayOf<UByte>(0xF0u, 0x00u, 0x00u, 0x00u)

    fun getEvenOrOddACE(isEven: Boolean, networkAddress: Array<UByte>, networkBitsCount: Int): ACE {
        val mask = getNetworkWildcard(networkBitsCount)
        for (i in mask.indices) {
            mask[i] = mask[i] and _evenOrOddWildcard[i]
        }

        val i = networkBitsCount / BITS_IN_BYTE
        for (j in i until BYTES_IN_ADDRESS) {
            if (isEven) {
                networkAddress[j] = networkAddress[j] and 0xFEu
            } else {
                networkAddress[j] = networkAddress[j] or 1u
            }

            mask[j] = 0xFEu
        }

        return createACE(networkAddress, mask)
    }

    fun getNetworkACE(networkAddress: Array<UByte>, networkBitsCount: Int): ACE {
        val mask = getNetworkWildcard(networkBitsCount)
        for (i in mask.indices) {
            networkAddress[i] = networkAddress[i] and mask[i].inv()
        }

        return createACE(networkAddress, mask)
    }

    fun getACEForClass(networkClass: NetworkClass): ACE {
        val networkAddress: Array<UByte>
        val wildcard: Array<UByte>
        when (networkClass) {
            NetworkClass.A -> {
                wildcard = _classAWildcard
                networkAddress = _classASupportIP
            }

            NetworkClass.B -> {
                wildcard = _classBWildcard
                networkAddress = _classBSupportIP
            }

            NetworkClass.C -> {
                wildcard = _classCWildcard
                networkAddress = _classCSupportIP
            }

            NetworkClass.D -> {
                wildcard = _classDWildcard
                networkAddress = _classDSupportIP
            }

            else -> {
                wildcard = _classEWildcard
                networkAddress = _classESupportIP
            }
        }

        return createACE(networkAddress, wildcard)
    }

    fun getGreaterThanBoundACEs(
        networkAddress: Array<UByte>,
        lowerBound: UInt,
        networkBitsCount: Int
    ): List<ACE> {
        val aces = mutableListOf<ACE>()

        val indexComplement = MathUtil.getCeilBaseTwoLog(lowerBound) / (BITS_IN_BYTE + 1)
        val byteIndex = 3 - indexComplement
        var exponent = (indexComplement + 1) * 8
        var upperBound = MathUtil.powersOfTwo[exponent]
        var lastSetBitIndex = exponent

        val wildcard = getNetworkWildcard(networkBitsCount)
        val mask = networkAddress.copyOf()
        val hostIP = wildcard.copyOf()

        while (upperBound > (lowerBound + 1u) && lastSetBitIndex > 0) {
            for (i in networkAddress.indices) {
                hostIP[i] = networkAddress[i]
                mask[i] = wildcard[i]
            }

            while (exponent > 0 && (upperBound - MathUtil.powersOfTwo[--exponent]) < lowerBound) {
                lastSetBitIndex = exponent
            }

            upperBound -= MathUtil.powersOfTwo[exponent]
            hostIP[byteIndex] =
                ((upperBound + if (upperBound == lowerBound) 1u else 0u) shr ((3 - byteIndex) * 8)).toUByte()
            mask[byteIndex] =
                if (upperBound == lowerBound) 0u
                else ((MathUtil.powersOfTwo[exponent] - 1u) shr ((3 - byteIndex) * 8)).toUByte()

            aces.add(createACE(hostIP, mask))
        }

        return aces
    }

    fun getSmallerThanBoundACEs(
        networkAddress: Array<UByte>,
        upperBound: UInt,
        networkBitsCount: Int
    ): List<ACE> {
        val aces = mutableListOf<ACE>()
        if (upperBound == 0u) {
            return aces
        }

        var exponent = MathUtil.getFloorBaseTwoLog(upperBound)
        var lowerBound = MathUtil.powersOfTwo[exponent]
        val byteIndex = 3 - exponent / (BITS_IN_BYTE + 1)
        val wildcard = getNetworkWildcard(networkBitsCount)

        // Smaller than 2^exponent ACE
        val mask = networkAddress.copyOf()
        val hostIP = wildcard.copyOf()
        hostIP[byteIndex] = 0u
        mask[byteIndex] = (lowerBound - 1u).toUByte()
        aces.add(createACE(hostIP, mask))

        // Greater than 2^exponent ACEs
        var lastSetBitIndex = exponent
        var forceACE = lowerBound < upperBound

        while ((forceACE || lowerBound < (upperBound - 1u)) && lastSetBitIndex > 0) {
            for (i in networkAddress.indices) {
                hostIP[i] = networkAddress[i]
                mask[i] = wildcard[i]
            }

            exponent = lastSetBitIndex
            while (exponent > 0 && (lowerBound + MathUtil.powersOfTwo[--exponent]) > upperBound) {
                lastSetBitIndex = exponent
            }

            hostIP[byteIndex] = (lowerBound shr ((3 - byteIndex) * 8)).toUByte()
            mask[byteIndex] =
                ((MathUtil.powersOfTwo[exponent] - 1u) shr ((3 - byteIndex) * 8)).toUByte()

            lowerBound += MathUtil.powersOfTwo[exponent]
            forceACE = lowerBound == (upperBound - 1u) && lastSetBitIndex > 1

            aces.add(createACE(hostIP, mask))
        }

        return aces
    }

    fun getRangeACEs(
        networkAddress: Array<UByte>,
        bounds: Bounds,
        networkBitsCount: Int
    ): List<ACE> {
        val aces = mutableListOf<ACE>()
        if (bounds.lower == bounds.upper) {
            val byteIndex = networkBitsCount / BITS_IN_BYTE
            networkAddress[byteIndex] = (bounds.lower shr (byteIndex * BITS_IN_BYTE)).toUByte()
            aces.add(createACE(networkAddress, arrayOf(0u, 0u, 0u, 0u)))

            return aces
        }

        val wildcard = getNetworkWildcard(networkBitsCount)
        val mask = networkAddress.copyOf()
        val hostIP = wildcard.copyOf()

        var lowerExponent = MathUtil.getCeilBaseTwoLog(bounds.lower)
        var higherExponent = MathUtil.getFloorBaseTwoLog(bounds.upper)
        var roundedLower = MathUtil.powersOfTwo[lowerExponent]
        var roundedHigher = MathUtil.powersOfTwo[higherExponent]

        var byteIndex = 3 - lowerExponent / (BITS_IN_BYTE + 1)

        // Handle low
        // This is not redundant, it's needed in the "Handle mid" section below
        var lowerExpCopy = lowerExponent
        var lastSetBitIndex = lowerExponent

        while (roundedLower > (bounds.lower + 1u) && lastSetBitIndex > 0) {
            for (i in networkAddress.indices) {
                hostIP[i] = networkAddress[i]
                mask[i] = wildcard[i]
            }

            while (lowerExpCopy > 0 && (roundedLower - MathUtil.powersOfTwo[--lowerExpCopy]) < bounds.lower) {
                lastSetBitIndex = lowerExpCopy
            }

            roundedLower -= MathUtil.powersOfTwo[lowerExpCopy]
            hostIP[byteIndex] =
                ((roundedLower + if (roundedLower == bounds.lower) 1u else 0u) shr ((3 - byteIndex) * 8)).toUByte()
            mask[byteIndex] =
                if (roundedLower == bounds.lower) 0u
                else ((MathUtil.powersOfTwo[lowerExpCopy] - 1u) shr ((3 - byteIndex) * 8)).toUByte()

            aces.add(0, createACE(hostIP, mask))
        }

        // Handle mid
        roundedLower = MathUtil.powersOfTwo[lowerExponent]
        if (lowerExponent != higherExponent) {
            while (roundedLower < roundedHigher) {
                for (i in networkAddress.indices) {
                    hostIP[i] = networkAddress[i]
                    mask[i] = wildcard[i]
                }

                byteIndex = 3 - lowerExponent / (BITS_IN_BYTE + 1)
                hostIP[byteIndex] = (roundedLower shr ((3 - byteIndex) * 8)).toUByte()
                mask[byteIndex] =
                    ((MathUtil.powersOfTwo[lowerExponent] - 1u) shr ((3 - byteIndex) * 8)).toUByte()

                aces.add(createACE(hostIP, mask))
                roundedLower += MathUtil.powersOfTwo[lowerExponent++]
            }
        }

        // Handle high
        lastSetBitIndex = higherExponent
        var forceACE = roundedHigher < bounds.upper

        while ((forceACE || roundedHigher < (bounds.upper - 1u)) && lastSetBitIndex > 0) {
            for (i in networkAddress.indices) {
                hostIP[i] = networkAddress[i]
                mask[i] = wildcard[i]
            }

            higherExponent = lastSetBitIndex
            while (higherExponent > 0 && (roundedHigher + MathUtil.powersOfTwo[--higherExponent]) > bounds.upper) {
                lastSetBitIndex = higherExponent
            }

            hostIP[byteIndex] = (roundedHigher shr ((3 - byteIndex) * 8)).toUByte()
            mask[byteIndex] =
                ((MathUtil.powersOfTwo[higherExponent] - 1u) shr ((3 - byteIndex) * 8)).toUByte()

            roundedHigher += MathUtil.powersOfTwo[higherExponent]
            forceACE = roundedHigher == (bounds.upper - 1u) && lastSetBitIndex > 1

            aces.add(createACE(hostIP, mask))
        }

        return aces
    }

    private fun getNetworkWildcard(networkBitsCount: Int): Array<UByte> {
        var mask = 0u
        for (i in 0 until BITS_IN_IPv4_ADDRESS) {
            if (i >= networkBitsCount) {
                ++mask
            }
            if (i != LAST_ADDRESS_BIT_INDEX) {
                mask = mask shl 1
            }
        }

        return arrayOf(
            (mask shr 24).toUByte(),
            (mask shr 16 and 0x00FFu).toUByte(),
            (mask shr 8 and 0x0000FFu).toUByte(),
            (mask and 0x000000FFu).toUByte()
        )
    }

    private fun createACE(networkAddress: Array<UByte>, wildcard: Array<UByte>): ACE {
        return ACE(wildcard.joinToString("."), networkAddress.joinToString("."))
    }
}
