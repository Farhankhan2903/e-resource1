package com.eresource.solution.data

import android.content.Context
import com.eresource.solution.data.repository.AppRepository

object DependencyProvider {
    private var repository: AppRepository? = null

    fun initialize(context: Context) {
        if (repository == null) {
            repository = AppRepository(context.applicationContext)
        }
    }

    fun provideRepository(): AppRepository {
        return repository ?: throw IllegalStateException("DependencyProvider not initialized. Call initialize(context) in MainActivity.")
    }
}
