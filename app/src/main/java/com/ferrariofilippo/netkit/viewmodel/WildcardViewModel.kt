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
import com.ferrariofilippo.netkit.BR
import com.ferrariofilippo.netkit.Constants.DEFAULT_WILDCARD_NETWORK_ADDRESS
import com.ferrariofilippo.netkit.R
import com.ferrariofilippo.netkit.adapters.WildcardItemAdapter
import com.ferrariofilippo.netkit.model.data.ACE
import com.ferrariofilippo.netkit.model.data.Bounds
import com.ferrariofilippo.netkit.model.enums.NetworkClass
import com.ferrariofilippo.netkit.model.enums.WildcardMethods
import com.ferrariofilippo.netkit.util.IPv4Util
import com.ferrariofilippo.netkit.util.ValidationUtil.defaultValidationFun
import com.ferrariofilippo.netkit.util.WildcardUtil

class WildcardViewModel(app: Application) : AndroidViewModel(app), Observable {
    private lateinit var _recyclerAdapter: WildcardItemAdapter

    private val _callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    private val _aces = mutableListOf<ACE>()

    private var _wildcardMethod = WildcardMethods.Range
    private var _lowerBound = 0L
    private var _upperBound = 0L
    private var _networkString = DEFAULT_WILDCARD_NETWORK_ADDRESS
    private var _networkClass = NetworkClass.A
    private var _canReset = false

    val wildcardMethods = arrayOf(
        app.getString(R.string.range),
        app.getString(R.string.smaller),
        app.getString(R.string.greater),
        app.getString(R.string.even),
        app.getString(R.string.odd),
        app.getString(R.string.network),
        app.getString(R.string.class_string)
    )
    val networkClasses = NetworkClass.entries.toTypedArray()

    var validateLowerBound: (error: Boolean) -> Unit = ::defaultValidationFun
    var validateUpperBound: (error: Boolean) -> Unit = ::defaultValidationFun
    var validateNetworkString: (error: Boolean) -> Unit = ::defaultValidationFun

    // Overrides
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.remove(callback)
    }

    // Bindings
    @Bindable
    fun getShowLowerBound(): Boolean {
        return _wildcardMethod == WildcardMethods.Greater || _wildcardMethod == WildcardMethods.Range
    }

    @Bindable
    fun getShowUpperBound(): Boolean {
        return _wildcardMethod == WildcardMethods.Lower || _wildcardMethod == WildcardMethods.Range
    }

    @Bindable
    fun getShowNetworkAddress(): Boolean {
        return _wildcardMethod != WildcardMethods.Class
    }

    @Bindable
    fun getShowClass(): Boolean {
        return _wildcardMethod == WildcardMethods.Class
    }

    @Bindable
    fun getWildcardMethod(): WildcardMethods {
        return _wildcardMethod
    }

    fun setWildcardMethod(value: WildcardMethods) {
        if (value != _wildcardMethod) {
            _wildcardMethod = value
            notifyPropertyChanged(BR.wildcardMethod)
            notifyPropertyChanged(BR.showLowerBound)
            notifyPropertyChanged(BR.showUpperBound)
            notifyPropertyChanged(BR.showNetworkAddress)
            notifyPropertyChanged(BR.showClass)
        }
    }

    @Bindable
    fun getLowerBound(): Long {
        return _lowerBound
    }

    fun setLowerBound(value: Long) {
        if (value != _lowerBound) {
            _lowerBound = value
            validateLowerBound(false)
            notifyPropertyChanged(BR.lowerBound)
        }
    }

    @Bindable
    fun getUpperBound(): Long {
        return _upperBound
    }

    fun setUpperBound(value: Long) {
        if (value != _upperBound) {
            _upperBound = value
            validateUpperBound(false)
            notifyPropertyChanged(BR.upperBound)
        }
    }

    @Bindable
    fun getNetworkString(): String {
        return _networkString
    }

    fun setNetworkString(value: String) {
        if (value != _networkString) {
            _networkString = value
            validateNetworkString(false)
            notifyPropertyChanged(BR.networkString)
        }
    }

    @Bindable
    fun getNetworkClass(): NetworkClass {
        return _networkClass
    }

    fun setNetworkClass(value: NetworkClass) {
        if (value != _networkClass) {
            _networkClass = value
            notifyPropertyChanged(BR.networkClass)
        }
    }

    @Bindable
    fun getCanReset(): Boolean {
        return _canReset
    }

    private fun setCanReset(value: Boolean) {
        if (value xor _canReset) {
            _canReset = !_canReset
            notifyPropertyChanged(BR.canReset)
        }
    }

    // Methods
    @SuppressLint("NotifyDataSetChanged")
    fun compute() {
        val networkAddress = Array<UByte>(4) { 0u }
        val addressAndPrefixLength = _networkString.split('/', '\\')
        val networkBits =
            if (addressAndPrefixLength.size == 2) addressAndPrefixLength[1].toIntOrNull() ?: 0
            else 0

        if ((!IPv4Util.tryParseAddress(
                addressAndPrefixLength[0],
                networkAddress
            ) || networkBits == 0) && getShowNetworkAddress()
        ) {
            validateNetworkString(true)
            return
        }
        if (_lowerBound < 0 && getShowLowerBound()) {
            validateLowerBound(true)
            return
        }
        if (getShowUpperBound() && (_upperBound < 0 || (_upperBound < _lowerBound && getShowLowerBound()))) {
            validateUpperBound(true)
            return
        }

        _aces.clear()
        when (_wildcardMethod) {
            WildcardMethods.Even, WildcardMethods.Odd -> {
                _aces.add(
                    WildcardUtil.getEvenOrOddACE(
                        _wildcardMethod == WildcardMethods.Even,
                        networkAddress,
                        networkBits
                    )
                )
            }

            WildcardMethods.Network -> {
                _aces.add(WildcardUtil.getNetworkACE(networkAddress, networkBits))
            }

            WildcardMethods.Class -> {
                _aces.add(WildcardUtil.getACEForClass(_networkClass))
            }

            else -> {
                val entries = when (_wildcardMethod) {
                    WildcardMethods.Range -> WildcardUtil.getRangeACEs(
                        networkAddress,
                        Bounds(_lowerBound.toUInt(), _upperBound.toUInt()),
                        networkBits
                    )

                    WildcardMethods.Lower -> WildcardUtil.getSmallerThanBoundACEs(
                        networkAddress,
                        _upperBound.toUInt(),
                        networkBits
                    )

                    else -> WildcardUtil.getGreaterThanBoundACEs(
                        networkAddress,
                        _lowerBound.toUInt(),
                        networkBits
                    )
                }

                entries.forEach {
                    _aces.add(it)
                }
            }
        }
        _recyclerAdapter.notifyDataSetChanged()
        setCanReset(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun reset() {
        if (_canReset) {
            _aces.clear()
            _recyclerAdapter.notifyDataSetChanged()
            setLowerBound(0L)
            validateLowerBound(false)
            setUpperBound(0L)
            validateUpperBound(false)
            setNetworkString(DEFAULT_WILDCARD_NETWORK_ADDRESS)
            validateNetworkString(false)
            setNetworkClass(NetworkClass.A)

            setCanReset(false)
        }
    }

    // UI
    @SuppressLint("NotifyDataSetChanged")
    fun setAdapter(adapter: WildcardItemAdapter) {
        _recyclerAdapter = adapter
        _recyclerAdapter.submitList(_aces)
        _recyclerAdapter.notifyDataSetChanged()
    }

    private fun notifyPropertyChanged(fieldId: Int) {
        _callbacks.notifyCallbacks(this, fieldId, null)
    }
}
