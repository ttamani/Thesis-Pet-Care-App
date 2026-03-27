package com.learning.multipet.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory class for creating ViewModels that require Application context.
 * This factory is necessary because the default ViewModelProvider cannot instantiate
 * AndroidViewModel subclasses with Application parameters.
 */
class ViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AppViewModel::class.java) -> {
                AppViewModel(application) as T
            }
            modelClass.isAssignableFrom(SessionViewModel::class.java) -> {
                SessionViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
