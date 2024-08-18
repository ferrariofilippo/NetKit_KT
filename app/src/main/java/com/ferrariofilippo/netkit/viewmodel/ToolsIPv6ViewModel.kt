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
import com.ferrariofilippo.netkit.Constants.HEXTET_IN_ADDRESS
import com.ferrariofilippo.netkit.util.IPv6Util
import com.ferrariofilippo.netkit.util.ValidationUtil

class ToolsIPv6ViewModel(app: Application) : AndroidViewModel(app), Observable {
    private val _callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    // IP Compression
    private var _addressIPv6ToSqueeze = ""
    private var _canResetSqueeze = false

    private var _squeezedAddress = ""
    val squeezedAddress get() = _squeezedAddress

    var validateIPAddressToSqueeze: (error: Boolean) -> Unit = ValidationUtil::defaultValidationFun

    // Overrides
    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        _callbacks.remove(callback)
    }

    // Bindings
    // IP Compression
    @Bindable
    fun getAddressIPv6ToSqueeze(): String {
        return _addressIPv6ToSqueeze
    }

    fun setAddressIPv6ToSqueeze(value: String) {
        if (value != _addressIPv6ToSqueeze) {
            _addressIPv6ToSqueeze = value
            validateIPAddressToSqueeze(false)
            notifyPropertyChanged(BR.addressIPv6ToSqueeze)
        }
    }

    @Bindable
    fun getCanResetSqueeze(): Boolean {
        return _canResetSqueeze
    }

    private fun setCanResetSqueeze(value: Boolean) {
        if (value != _canResetSqueeze) {
            _canResetSqueeze = value
            notifyPropertyChanged(BR.canResetSqueeze)
        }
    }

    // Methods
    // IP Compression
    fun squeezeAddress(): Boolean {
        val components = Array(HEXTET_IN_ADDRESS) { "" }
        _squeezedAddress = ""

        if (!IPv6Util.validateAndSplitAddress(_addressIPv6ToSqueeze, components)) {
            validateIPAddressToSqueeze(true)
            return false
        }

        setCanResetSqueeze(true)
        _squeezedAddress = IPv6Util.compressAddress(components, HEXTET_IN_ADDRESS)

        return true
    }

    fun resetSqueeze() {
        setAddressIPv6ToSqueeze("")
        validateIPAddressToSqueeze(false)
        setCanResetSqueeze(false)
    }

    // UI
    private fun notifyPropertyChanged(fieldId: Int) {
        _callbacks.notifyCallbacks(this, fieldId, null)
    }
}
