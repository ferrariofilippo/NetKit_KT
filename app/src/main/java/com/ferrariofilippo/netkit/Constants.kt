/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit

object Constants {
    const val SUBNET_SIZES_COUNT = 31
    const val BITS_IN_BYTE = 8
    const val BITS_IN_IPv4_ADDRESS = 32
    const val LAST_ADDRESS_BIT_INDEX = 31
    const val BYTES_IN_ADDRESS = 4
    const val LAST_BIT_INDEX = 7

    const val HEXTET_IN_ADDRESS = 8
    const val LAST_HEXTET_INDEX = 7
    const val OMISSION_START_KEY = 0
    const val OMISSION_LENGTH_KEY = 1
    const val BITS_IN_IPv6_COMPONENT = 16

    const val ZERO_STR = "0"

    const val DEFAULT_WILDCARD_NETWORK_ADDRESS = "192.168.0.0/24"
}
