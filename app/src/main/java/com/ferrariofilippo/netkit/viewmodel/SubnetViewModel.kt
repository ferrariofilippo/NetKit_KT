/*
 * Copyright (c) 2024 Filippo Ferrario
 * Licensed under the MIT License. See the LICENSE.
 */

package com.ferrariofilippo.netkit.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ferrariofilippo.netkit.BR
import com.ferrariofilippo.netkit.Constants.BITS_IN_BYTE
import com.ferrariofilippo.netkit.Constants.BITS_IN_IPv4_ADDRESS
import com.ferrariofilippo.netkit.Constants.BITS_IN_IPv6_COMPONENT
import com.ferrariofilippo.netkit.Constants.BYTES_IN_ADDRESS
import com.ferrariofilippo.netkit.Constants.HEXTET_IN_ADDRESS
import com.ferrariofilippo.netkit.Constants.ZERO_STR
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.adapters.SizeItemAdapter
import com.ferrariofilippo.netkit.adapters.SubnetItemAdapter
import com.ferrariofilippo.netkit.model.data.NetworkInfo
import com.ferrariofilippo.netkit.model.data.SizeInfo
import com.ferrariofilippo.netkit.util.IPv4Util
import com.ferrariofilippo.netkit.util.IPv6Util
import com.ferrariofilippo.netkit.util.MathUtil

class SubnetViewModel(app: Application) : AndroidViewModel(app), Observable {
    companion object {
        private var _sizeId = 0
    }

    private val _callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    private lateinit var _sizesAdapter: SizeItemAdapter
    private lateinit var _subnetsAdapter: SubnetItemAdapter

    private var _lastSubnetComponents = arrayOf<UByte>(0u, 0u, 0u, 0u)
    private var _globalRoutingPrefix = 0
    private var _ipv6SubnetId = 0
    private var _baseIPv6Address = Array(8) { "" }
    private var _ipv6Address = ""
    private var _subnetsCountV6 = 0

    private val _sizeList: MutableList<SizeInfo> = mutableListOf(SizeInfo("", 0u, ++_sizeId))
    private val _subnetList: MutableList<NetworkInfo> = mutableListOf()

    private val _canReset = MutableLiveData(false)
    val canReset: LiveData<Boolean> = _canReset

    private val _ipVersionIndex = MutableLiveData(0)
    val ipVersionIndex: LiveData<Int> = _ipVersionIndex

    val ipVersions = arrayOf(app.getString(R.string.ip4), app.getString(R.string.ip6))

    // Overrides
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.remove(callback)
    }

    // Bindings
    @Bindable
    fun getIPv6Address(): String {
        return _ipv6Address
    }

    fun setIPv6Address(value: String) {
        if (value != _ipv6Address) {
            _ipv6Address = value
            notifyPropertyChanged(BR.iPv6Address)
        }
    }

    @Bindable
    fun getGlobalRoutingPrefix(): Int {
        return _globalRoutingPrefix
    }

    fun setGlobalRoutingPrefix(value: Int) {
        if (value != _globalRoutingPrefix) {
            _globalRoutingPrefix = value
            notifyPropertyChanged(BR.globalRoutingPrefix)
        }
    }

    @Bindable
    fun getSubnetsCountV6(): Int {
        return _subnetsCountV6
    }

    fun setSubnetsCountV6(value: Int) {
        if (value != _subnetsCountV6) {
            _subnetsCountV6 = value
            notifyPropertyChanged(BR.subnetsCountV6)
        }
    }

    // Methods
    fun setAdapters(sizesAdapter: SizeItemAdapter, subnetsAdapter: SubnetItemAdapter) {
        _sizesAdapter = sizesAdapter
        _subnetsAdapter = subnetsAdapter

        _sizesAdapter.submitList(_sizeList)
        _subnetsAdapter.submitList(_subnetList)
    }

    fun setIPVersionIndex(index: Int) {
        if (_ipVersionIndex.value != index) {
            _ipVersionIndex.value = index
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reset() {
        if (canReset.value!!) {
            _sizeList.clear()
            _subnetList.clear()
            _sizeList.add(SizeInfo("", 0u, ++_sizeId))
            _sizesAdapter.notifyDataSetChanged()
            _subnetsAdapter.notifyDataSetChanged()

            _canReset.value = false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun computeIPv4() {
        resetLastIPv4Subnet()

        val networks = getIPv4Subnets(_sizeList.sortedByDescending { it.actualSize })
        _subnetList.clear()
        networks.forEach {
            _subnetList.add(it)
        }
        _subnetsAdapter.notifyDataSetChanged()

        _canReset.value = true
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun computeIPv6() {
        if (!tryGetBaseIPv6Address()) {
            return
        }

        _ipv6SubnetId = BITS_IN_IPv6_COMPONENT - (_globalRoutingPrefix % BITS_IN_IPv6_COMPONENT)
        var index = (_globalRoutingPrefix + _ipv6SubnetId) / BITS_IN_IPv6_COMPONENT - 1
        var lastDigit = _baseIPv6Address[index].last().toString()
        var baseValue = lastDigit.hexToInt()

        if (_subnetsCountV6 > (1 shr _ipv6SubnetId - baseValue)) {
            return
        } else if (_subnetsCountV6 > 1 shr 16) {
            // TODO: Show alert
        }

        getIPv6Subnets(_subnetsCountV6)

        _canReset.value = true
    }

    fun formatList() {
        var i = 0
        var hostsSum = 0u

        // Last item should be 0
        while (i < _sizeList.size - 1) {
            if (_sizeList[i].sizeOrPrefix.isBlank() || _sizeList[i].sizeOrPrefix == ZERO_STR) {
                _sizeList.removeAt(i)
                _sizesAdapter.notifyItemRemoved(i)
            } else {
                hostsSum += _sizeList[i].actualSize
                ++i
            }
        }
        hostsSum += _sizeList.last().actualSize

        if (_sizeList.last().sizeOrPrefix.isNotEmpty() &&
            hostsSum < (0xFFFFFFFFu - 2u * _sizeList.size.toUInt())
        ) {
            _sizeList.add(SizeInfo("", 0u, ++_sizeId))
            _sizesAdapter.notifyItemInserted(_sizeList.size - 1)
        }
    }

    private fun getIPv4Subnets(sizes: List<SizeInfo>): List<NetworkInfo> {
        val networks = mutableListOf<NetworkInfo>()

        for (i in sizes.indices) {
            if (sizes[i].actualSize != 0u) {
                networks.add(
                    NetworkInfo(
                        "",
                        "",
                        "",
                        IPv4Util.getPrefixLength(sizes[i].actualSize),
                        sizes[i].actualSize
                    )
                )
            }
        }

        if (networks.size == 0) {
            return networks
        }

        setIPv4Subnets(networks)
        setIPv4Networks(networks)
        setIPv4Broadcast(networks)

        return networks
    }

    private fun setIPv4Subnets(networks: List<NetworkInfo>) {
        val mask = arrayOf<UByte>(0u, 0u, 0u, 0u)
        for (i in networks.indices) {
            for (j in mask.indices) {
                mask[j] = 0u
            }

            IPv4Util.tryGetSubnetMask(networks[i].prefixLength, mask)

            networks[i].subnetMask = mask.joinToString(".")
        }
    }

    private fun setIPv4Networks(networks: List<NetworkInfo>) {
        if (networks[0].prefixLength > 24) {
            _lastSubnetComponents[0] = 192u
            _lastSubnetComponents[1] = 168u
        } else if (networks[0].prefixLength > 16) {
            _lastSubnetComponents[0] = 172u
            _lastSubnetComponents[1] = 16u
        } else if (networks[0].prefixLength <= 8) {
            _lastSubnetComponents[0] = 0u
        }

        for (i in networks.indices) {
            val componentIndex = networks[i].prefixLength / 8
            val maxBit = (componentIndex + 1) * 8

            networks[i].networkAddress = _lastSubnetComponents.joinToString(".")
            _lastSubnetComponents[componentIndex] =
                (_lastSubnetComponents[componentIndex].toUInt() + MathUtil.powersOfTwo[maxBit - networks[i].prefixLength]).toUByte()

            if (componentIndex > 0 && _lastSubnetComponents[componentIndex] == 0u.toUByte()) {
                ++_lastSubnetComponents[componentIndex - 1]
            }
        }
    }

    private fun setIPv4Broadcast(networks: List<NetworkInfo>) {
        val ip = arrayOf<UByte>(0u, 0u, 0u, 0u)
        for (i in networks.indices) {
            val hostBits = BITS_IN_IPv4_ADDRESS - networks[i].prefixLength
            var counter = 0

            for (component in networks[i].networkAddress.split('.')) {
                ip[counter++] = component.toUByte()
            }

            for (j in 0 until BYTES_IN_ADDRESS) {
                for (k in 0 until BITS_IN_BYTE) {
                    if (j * BITS_IN_BYTE + k < hostBits) {
                        ip[BYTES_IN_ADDRESS - 1 - j] =
                            ip[BYTES_IN_ADDRESS - 1 - j] or MathUtil.powersOfTwo[k].toUByte()
                    } else {
                        break
                    }
                }
            }

            networks[i].broadcastAddress = ip.joinToString(".")
        }
    }

    private fun resetLastIPv4Subnet() {
        _lastSubnetComponents[0] = 10u
        _lastSubnetComponents[1] = 0u
        _lastSubnetComponents[2] = 0u
        _lastSubnetComponents[3] = 0u
    }

    private fun tryGetBaseIPv6Address(): Boolean {
        _ipv6Address = _ipv6Address.trim()

        if (!IPv6Util.isStringHexOrColon(_ipv6Address.uppercase())) {
            return false
        }

        if (_ipv6Address.endsWith("::")) {
            _baseIPv6Address = IPv6Util.expandAddress(
                _ipv6Address.substring(0, _ipv6Address.length - 2).split('.')
            )
        } else {
            _baseIPv6Address = if (_ipv6Address.contains("::")) {
                IPv6Util.expandAddress(_ipv6Address.split('.'))
            } else {
                _ipv6Address.split('.').toTypedArray()
            }

            if (_baseIPv6Address.size != HEXTET_IN_ADDRESS) {
                return false
            }
        }

        return true
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun getIPv6Subnets(count: Int) {
        val index = (_globalRoutingPrefix + _ipv6SubnetId) / BITS_IN_IPv6_COMPONENT - 1
        var component = _baseIPv6Address[index].hexToUInt()

        for (i in 0 until count) {
            _subnetList.add(
                NetworkInfo(
                    IPv6Util.getFormattedAddress(
                        _baseIPv6Address,
                        component,
                        index
                    ), "", "", 0, 0u
                )
            )
            ++component
        }
    }

    private fun notifyPropertyChanged(fieldId: Int) {
        _callbacks.notifyCallbacks(this, fieldId, null)
    }
}
