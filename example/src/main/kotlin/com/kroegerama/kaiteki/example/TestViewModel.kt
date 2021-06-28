package com.kroegerama.kaiteki.example

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kroegerama.kaiteki.architecture.LiveEvent
import com.kroegerama.kaiteki.architecture.NotifyLiveEvent

class TestViewModel : ViewModel() {

    val stringEvent = LiveEvent<String>()
    val stringLiveData = MutableLiveData<String>()
    val voidEvent = NotifyLiveEvent()

    fun emitString(string: String) {
        stringEvent(string)
        stringLiveData.value = string
    }

}