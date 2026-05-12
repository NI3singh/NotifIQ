package com.notifiq.app.di

import android.content.Context
import com.notifiq.core.datastore.UserPreferenceDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserPreferenceDataStore(
        @ApplicationContext context: Context
    ): UserPreferenceDataStore {
        return UserPreferenceDataStore(context)
    }
}