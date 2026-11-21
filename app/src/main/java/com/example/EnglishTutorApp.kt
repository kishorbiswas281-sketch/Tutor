package com.example

import android.app.Application
import com.example.data.local.EnglishTutorDatabase
import com.example.data.preferences.AppPreferencesManager
import com.example.data.remote.RealtimeBackendApi
import com.example.data.repository.TutorRepository
import com.example.speech.SpeechServiceManager
import com.example.webrtc.RealtimeWebRtcManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(application: Application) {
    private val database: EnglishTutorDatabase by lazy {
        EnglishTutorDatabase.getInstance(application)
    }

    val preferencesManager: AppPreferencesManager by lazy {
        AppPreferencesManager(application)
    }

    val repository: TutorRepository by lazy {
        TutorRepository(database, preferencesManager)
    }

    val backendApi: RealtimeBackendApi by lazy {
        RealtimeBackendApi(application)
    }

    val speechManager: SpeechServiceManager by lazy {
        SpeechServiceManager(application)
    }

    val webRtcManager: RealtimeWebRtcManager by lazy {
        RealtimeWebRtcManager(application, backendApi)
    }
}

class EnglishTutorApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.repository.ensureSeeded()
        }
    }
}
