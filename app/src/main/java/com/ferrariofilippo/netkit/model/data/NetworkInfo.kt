/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.model.data

data class NetworkInfo(
    var networkAddress: String,
    var broadcastAddress: String,
    var subnetMask: String,
    var prefixLength: Int,
    var hostCount: UInt
)
