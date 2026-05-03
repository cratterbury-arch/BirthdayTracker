package com.chris.birthdaytracker

object SoundPlaybackManager {
    private var soundPlayedThisSession = false

    fun hasSoundPlayedThisSession(): Boolean {
        return soundPlayedThisSession
    }

    fun markSoundAsPlayedThisSession() {
        soundPlayedThisSession = true
    }
}
