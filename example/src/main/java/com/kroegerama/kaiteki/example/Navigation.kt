package com.kroegerama.kaiteki.example

enum class Navigation(val idx: Int) {
    Main(R.id.navMain),
    Info(R.id.navInfo),
    Map(R.id.navMap);

    companion object {
        fun fromIdx(idx: Int) = values().first { it.idx == idx }
    }
}