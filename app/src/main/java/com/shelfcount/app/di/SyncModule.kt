package com.shelfcount.app.di

import com.shelfcount.app.data.remote.NoOpRemoteDataSource
import com.shelfcount.app.data.remote.NoOpSyncOrchestrator
import com.shelfcount.app.data.remote.RemoteDataSource
import com.shelfcount.app.data.remote.SyncOrchestrator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    @Binds
    @Singleton
    abstract fun bindRemoteDataSource(impl: NoOpRemoteDataSource): RemoteDataSource

    @Binds
    @Singleton
    abstract fun bindSyncOrchestrator(impl: NoOpSyncOrchestrator): SyncOrchestrator
}
