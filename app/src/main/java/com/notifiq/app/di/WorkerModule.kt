package com.notifiq.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    // Worker dependencies are automatically provided through @HiltWorker
    // and @AssistedInject pattern. This module exists for any additional
    // worker-specific bindings if needed in the future.
}