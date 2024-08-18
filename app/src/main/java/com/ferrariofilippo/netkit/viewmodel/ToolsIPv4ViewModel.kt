/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.viewmodel

import android.app.Application
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import com.ferrariofilippo.netkit.BR
import com.ferrariofilippo.netkit.Constants.BITS_IN_BYTE
import com.ferrariofilippo.netkit.Constants.LAST_BIT_INDEX
import com.ferrariofilippo.netkit.util.IPv4Util
import com.ferrariofilippo.netkit.util.MathUtil
import com.ferrariofilippo.netkit.util.ValidationUtil.defaultValidationFun

class ToolsIPv4ViewModel(app: Application) : AndroidViewModel(app), Observable {
    private val _callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    // Find Subnet
    private var _findSubnetSubnetMask = ""
    private var _findSubnetSubnetNumber = 1L
    private var _canResetFindSubnet = false

    private var _findSubnetNetworkAddress = ""
    val findSubnetNetworkAddress get() = _findSubnetNetworkAddress

    var validateFindSubnetSubnet: (error: Boolean) -> Unit = ::defaultValidationFun
    var validateFindSubnetNumber: (error: Boolean) -> Unit = ::defaultValidationFun

    // Get Network
    private var _getNetworkHostIP = ""
    private var _getNetworkSubnetMask = ""
    private var _canResetGetNetwork = false

    private var _getNetworkNetworkAddress = ""
    val getNetworkNetworkAddress get() = _getNetworkNetworkAddress

    var validateGetNetworkHostIP: (error: Boolean) -> Unit = ::defaultValidationFun
    var validateGetNetworkSubnet: (error: Boolean) -> Unit = ::defaultValidationFun

    // Overrides
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.remove(callback)
    }

    // Bindings
    // Find Subnet
    @Bindable
    fun getFindSubnetSubnetMask(): String {
        return _findSubnetSubnetMask
    }

    fun setFindSubnetSubnetMask(value: String) {
        if (value != _findSubnetSubnetMask) {
            _findSubnetSubnetMask = value
            validateFindSubnetSubnet(false)
            notifyPropertyChanged(BR.findSubnetSubnetMask)
        }
    }

    @Bindable
    fun getFindSubnetSubnetNumber(): Long {
        return _findSubnetSubnetNumber
    }

    fun setFindSubnetSubnetNumber(value: Long) {
        if (value != _findSubnetSubnetNumber) {
            _findSubnetSubnetNumber = value
            validateFindSubnetNumber(false)
            notifyPropertyChanged(BR.findSubnetSubnetNumber)
        }
    }

    @Bindable
    fun getCanResetFindSubnet(): Boolean {
        return _canResetFindSubnet
    }

    private fun setCanResetFindSubnet(value: Boolean) {
        if (value != _canResetFindSubnet) {
            _canResetFindSubnet = value
            notifyPropertyChanged(BR.canResetFindSubnet)
        }
    }

    // Get Network
    @Bindable
    fun getGetNetworkHostIP(): String {
        return _getNetworkHostIP
    }

    fun setGetNetworkHostIP(value: String) {
        if (value != _getNetworkHostIP) {
            _getNetworkHostIP = value
            validateGetNetworkHostIP(false)
            notifyPropertyChanged(BR.getNetworkHostIP)
        }
    }

    @Bindable
    fun getGetNetworkSubnetMask(): String {
        return _getNetworkSubnetMask
    }

    fun setGetNetworkSubnetMask(value: String) {
        if (value != _getNetworkSubnetMask) {
            _getNetworkSubnetMask = value
            validateGetNetworkSubnet(false)
            notifyPropertyChanged(BR.getNetworkSubnetMask)
        }
    }

    @Bindable
    fun getCanResetGetNetwork(): Boolean {
        return _canResetGetNetwork
    }

    private fun setCanResetGetNetwork(value: Boolean) {
        if (value != _canResetGetNetwork) {
            _canResetGetNetwork = value
            notifyPropertyChanged(BR.canResetGetNetwork)
        }
    }

    // Methods
    // Find Subnet
    fun computeFindSubnet(): Boolean {
        val subnet = Array<UByte>(4) { 0u }
        val network = Array<UByte>(4) { 0u }
        _findSubnetNetworkAddress = ""

        if (!IPv4Util.tryParseAddress(_findSubnetSubnetMask, subnet)) {
            validateFindSubnetSubnet(true)
            return false
        }
        if (!isSubnetNumberValid(subnet, network)) {
            validateFindSubnetNumber(true)
            return false
        }

        setCanResetFindSubnet(true)
        _findSubnetNetworkAddress = network.joinToString(".")
        return true
    }

    fun resetFindSubnet() {
        setFindSubnetSubnetMask("")
        validateFindSubnetSubnet(false)
        setFindSubnetSubnetNumber(1)
        validateFindSubnetNumber(false)
        setCanResetFindSubnet(false)
    }

    private fun isSubnetNumberValid(subnetMask: Array<UByte>, network: Array<UByte>): Boolean {
        if (_findSubnetSubnetNumber <= 0) {
            return false
        }

        for (i in subnetMask.indices) {
            if (subnetMask[i] != UByte.MAX_VALUE) {
                when (i) {
                    1 -> {
                        network[0] = 10u
                    }

                    2 -> {
                        network[0] = 172u
                        network[1] = 16u
                    }

                    3 -> {
                        network[0] = 192u
                        network[1] = 168u
                    }
                }

                val lastSetBit = LAST_BIT_INDEX - subnetMask[i].toString(2).lastIndexOf('1')
                if (_findSubnetSubnetNumber.toUInt() > MathUtil.powersOfTwo[BITS_IN_BYTE - lastSetBit]) {
                    return false
                }

                val magicNumber =
                    MathUtil.powersOfTwo[lastSetBit] * (_findSubnetSubnetNumber.toUInt() - 1u)
                network[i] = magicNumber.toUByte()

                return true
            }
        }

        return false
    }

    // Get Network
    fun computeGetNetwork(): Boolean {
        val hostIP = Array<UByte>(4) { 0u }
        val subnet = Array<UByte>(4) { 0u }
        val network = Array<UByte>(4) { 0u }
        _getNetworkNetworkAddress = ""

        if (!IPv4Util.tryParseAddress(_getNetworkHostIP, hostIP)) {
            validateGetNetworkHostIP(true)
            return false
        }
        if (!getNetwork(hostIP, subnet, network)) {
            validateGetNetworkHostIP(true)
            return false
        }

        setCanResetGetNetwork(true)
        _getNetworkNetworkAddress = network.joinToString(".")
        return true
    }

    fun resetGetNetwork() {
        setGetNetworkHostIP("")
        validateGetNetworkHostIP(false)
        setGetNetworkSubnetMask("")
        validateGetNetworkSubnet(false)
        setCanResetGetNetwork(false)
    }

    private fun getNetwork(host: Array<UByte>, mask: Array<UByte>, network: Array<UByte>): Boolean {
        if (_getNetworkSubnetMask.startsWith('/') || _getNetworkSubnetMask.startsWith('\\')) {
            val prefixLength = _getNetworkSubnetMask.substring(1).toIntOrNull()
            if (prefixLength == null || !IPv4Util.tryGetSubnetMask(prefixLength, mask)) {
                return false
            }

        } else if (!IPv4Util.tryParseAddress(_getNetworkSubnetMask, mask)) {
            return false
        }

        for (i in host.indices) {
            network[i] = host[i] and mask[i]
        }

        return true
    }

    // UI
    private fun notifyPropertyChanged(fieldId: Int) {
        _callbacks.notifyCallbacks(this, fieldId, null)
    }
}
