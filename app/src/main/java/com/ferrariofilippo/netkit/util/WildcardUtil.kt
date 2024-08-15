/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.util

import com.ferrariofilippo.netkit.model.data.ACE
import com.ferrariofilippo.netkit.model.data.Bounds
import com.ferrariofilippo.netkit.model.enums.NetworkClass

object WildcardUtil {
    private const val LAST_ADDRESS_BITS_INDEX = 31
    private const val ADDRESS_BITS = 32
    private const val BITS_IN_BYTE = 8

    private val _evenOrOddWildcard = arrayOf<UByte>(0xFFu, 0xFFu, 0xFFu, 0xFEu)

    private val _classAWildcard = arrayOf<UByte>(0x7Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classBWildcard = arrayOf<UByte>(0x3Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classCWildcard = arrayOf<UByte>(0x3Fu, 0xFFu, 0xFFu, 0xFFu)
    private val _classDWildcard = arrayOf<UByte>(0x1Fu, 0xFFu, 0xFFu, 0xFFu)
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
        if (isEven) {
            networkAddress[i] = networkAddress[i] and 0xFEu
        } else {
            networkAddress[i] = networkAddress[i] or 1u
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
        val exponent = MathUtil.getCeilBaseTwoLog(lowerBound)
        if (exponent <= 0) {
            return listOf()
        }

        val bounds = Bounds(lowerBound, MathUtil.powersOfTwo[exponent])
        val roundedLowerBound = lowerBound + lowerBound % 2u
        val byteIndex = 3 - (exponent / BITS_IN_BYTE)
        val roundedExponent = exponent - (exponent % BITS_IN_BYTE)
        val wildcard = getNetworkWildcard(networkBitsCount)
        val aces = mutableListOf(getBiggestACE(exponent, wildcard, networkAddress))

        while (bounds.upper > roundedLowerBound) {
            aces.add(getGreaterThanBoundACE(networkAddress, wildcard, bounds, exponent))
        }

        wildcard[byteIndex] = 0u
        while (--bounds.upper >= bounds.lower) {
            networkAddress[byteIndex] = (bounds.upper shr roundedExponent).toUByte()
            aces.add(createACE(networkAddress, wildcard))
        }

        return aces
    }

    fun getSmallerThanBoundACEs(
        networkAddress: Array<UByte>,
        upperBound: UInt,
        networkBitsCount: Int
    ): List<ACE> {
        val exponent = MathUtil.getFloorBaseTwoLog(upperBound)
        if (exponent <= 0) {
            return listOf()
        }

        val bounds = Bounds(MathUtil.powersOfTwo[exponent], upperBound)
        val roundedUpperBound = upperBound + upperBound % 2u
        val byteIndex = 3 - (exponent / BITS_IN_BYTE)
        val roundedExponent = exponent - (exponent % BITS_IN_BYTE)
        val wildcard = getNetworkWildcard(networkBitsCount)
        val aces =
            mutableListOf(getBiggestACEWithUpperBound(exponent, wildcard, networkAddress))

        while (bounds.lower < roundedUpperBound) {
            aces.add(getSmallerThanBoundACE(networkAddress, wildcard, bounds, exponent))
        }

        wildcard[byteIndex] = 0u
        while (++bounds.lower <= bounds.upper) {
            networkAddress[byteIndex] = (bounds.lower shr roundedExponent).toUByte()
            aces.add(createACE(networkAddress, wildcard))
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
        var lowerExponent = MathUtil.getCeilBaseTwoLog(bounds.lower)
        val upperExponent = MathUtil.getFloorBaseTwoLog(bounds.upper)
        val medianPowerOfTwo = MathUtil.powersOfTwo[lowerExponent]
        if (upperExponent > lowerExponent + 1) {
            val upperHalfBounds = Bounds(medianPowerOfTwo, bounds.upper)
            val lowerHalfBounds = Bounds(bounds.lower, medianPowerOfTwo)

            while (upperHalfBounds.upper > upperHalfBounds.lower) {
                aces.add(
                    getGreaterThanBoundACE(
                        networkAddress,
                        wildcard,
                        upperHalfBounds,
                        upperExponent
                    )
                )
            }
            while (lowerHalfBounds.lower < lowerHalfBounds.upper) {
                aces.add(
                    getSmallerThanBoundACE(
                        networkAddress,
                        wildcard,
                        lowerHalfBounds,
                        lowerExponent
                    )
                )
            }

        } else {
            // TODO: Doesn't merge ACEs when it could
            ++lowerExponent
            while (bounds.lower < bounds.upper) {
                aces.add(getSmallerThanBoundACE(networkAddress, wildcard, bounds, lowerExponent))
            }
        }

        return aces
    }

    private fun getNetworkWildcard(networkBitsCount: Int): Array<UByte> {
        var mask = 0u
        for (i in 0 until ADDRESS_BITS) {
            if (i >= networkBitsCount) {
                ++mask
            }
            if (i != LAST_ADDRESS_BITS_INDEX) {
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

    private fun getBiggestACE(exponent: Int, mask: Array<UByte>, networkAddress: Array<UByte>): ACE {
        val byteIndex = 3 - exponent / BITS_IN_BYTE
        val bitIndex = exponent % BITS_IN_BYTE
        val tempMask = mask.copyOf()

        tempMask[byteIndex] = tempMask[byteIndex] and MathUtil.powersOfTwo[bitIndex].inv().toUByte()
        networkAddress[byteIndex] = MathUtil.powersOfTwo[bitIndex].toUByte()

        return createACE(networkAddress, tempMask)
    }

    private fun getBiggestACEWithUpperBound(
        exponent: Int,
        mask: Array<UByte>,
        networkAddress: Array<UByte>
    ): ACE {
        val byteIndex = 3 - exponent / BITS_IN_BYTE
        val bitIndex = exponent % BITS_IN_BYTE
        val tempMask = mask.copyOf()

        tempMask[byteIndex] = tempMask[byteIndex] and (0xff shr (BITS_IN_BYTE - bitIndex)).toUByte()
        networkAddress[byteIndex] = 0u

        return createACE(networkAddress, tempMask)
    }

    private fun getGreaterThanBoundACE(
        networkAddress: Array<UByte>,
        mask: Array<UByte>,
        bounds: Bounds,
        readOnlyExp: Int
    ): ACE {
        val byteIndex = 3 - readOnlyExp / BITS_IN_BYTE
        val roundedExponent = readOnlyExp - (readOnlyExp % BITS_IN_BYTE) // To get a multiple of 8
        var addressesSum = 0u
        var setBits = 0u
        var exponent = readOnlyExp

        for (i in LAST_ADDRESS_BITS_INDEX downTo exponent) {
            setBits += MathUtil.powersOfTwo[i]
        }

        --exponent

        while (addressesSum < bounds.lower && exponent >= 0) {
            setBits += MathUtil.powersOfTwo[exponent]
            if ((addressesSum + MathUtil.powersOfTwo[exponent]) < bounds.upper) {
                addressesSum += MathUtil.powersOfTwo[exponent]
            }

            --exponent
        }

        val tempMask = mask.copyOf()
        val unsetBits = setBits.inv() shr roundedExponent

        tempMask[byteIndex] = tempMask[byteIndex] and unsetBits.toUByte()
        networkAddress[byteIndex] = (addressesSum shr roundedExponent).toUByte()
        bounds.upper = addressesSum

        return createACE(networkAddress, tempMask)
    }

    private fun getSmallerThanBoundACE(
        networkAddress: Array<UByte>,
        mask: Array<UByte>,
        bounds: Bounds,
        readOnlyExp: Int
    ): ACE {
        val byteIndex = 3 - readOnlyExp / BITS_IN_BYTE
        val roundedExponent = readOnlyExp - (readOnlyExp % BITS_IN_BYTE) // To get a multiple of 8
        val addressesSum = bounds.lower
        var setBits = 0u
        var exponent = readOnlyExp - 1

        for (i in LAST_ADDRESS_BITS_INDEX downTo exponent) {
            setBits += MathUtil.powersOfTwo[i]
        }

        while (exponent >= 0 && (addressesSum + MathUtil.powersOfTwo[exponent]) > bounds.upper) {
            --exponent
            setBits += MathUtil.powersOfTwo[exponent]
        }

        val tempMask = mask.copyOf()
        val unsetBits = setBits.inv() shr roundedExponent

        tempMask[byteIndex] = tempMask[byteIndex] and unsetBits.toUByte()
        networkAddress[byteIndex] = (addressesSum shr roundedExponent).toUByte()
        bounds.lower = addressesSum + MathUtil.powersOfTwo[exponent]

        return createACE(networkAddress, tempMask)
    }

    private fun createACE(networkAddress: Array<UByte>, wildcard: Array<UByte>): ACE {
        return ACE(networkAddress.joinToString("."), wildcard.joinToString("."))
    }
}
