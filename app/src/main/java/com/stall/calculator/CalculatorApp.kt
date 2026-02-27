package com.stall.calculator

import android.app.Application
import com.stall.calculator.data.db.AppDatabase
import com.stall.calculator.data.repo.SettingsRepository
import com.stall.calculator.data.repo.StallRepository
import com.stall.calculator.util.ImageStorage

class CalculatorApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.build(this)
        val settingsRepository = SettingsRepository(this)
        container = AppContainer(
            repository = StallRepository(database, settingsRepository),
            settingsRepository = settingsRepository,
            imageStorage = ImageStorage(this)
        )
    }
}

data class AppContainer(
    val repository: StallRepository,
    val settingsRepository: SettingsRepository,
    val imageStorage: ImageStorage
)
