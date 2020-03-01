package com.kroegerama.kaiteki

import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes

operator fun Menu.get(@IdRes idRes: Int): MenuItem? = findItem(idRes)