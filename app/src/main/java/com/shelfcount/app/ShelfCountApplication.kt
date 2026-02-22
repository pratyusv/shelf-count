package com.shelfcount.app

import android.app.Application
import com.shelfcount.app.core.common.AppBootstrapper
import com.shelfcount.app.core.common.AppStartTracker
import com.shelfcount.app.core.common.CrashLogger
import com.shelfcount.app.data.local.ShelfCountDatabase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ShelfCountApplication : Application() {
    @Inject
    lateinit var appBootstrapper: AppBootstrapper

    private val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            deleteDatabase(ShelfCountDatabase.DATABASE_NAME)
        }
        AppStartTracker.markApplicationCreated()
        CrashLogger.install(context = this, enabled = BuildConfig.DEBUG)
        applicationScope.launch {
            appBootstrapper.run()
        }
    }
}
