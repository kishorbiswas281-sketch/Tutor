# AI English Tutor (বাংলা থেকে ইংরেজি বলার ব্যক্তিগত শিক্ষক)

A complete, 100% free, ad-free Android application built with **Kotlin**, **Jetpack Compose (Material 3)**, **Room**, **DataStore**, **WebRTC (`stream-webrtc-android`)**, and the **OpenAI Realtime API (GA)** to help Bengali-speaking absolute beginners speak English with confidence.

---

## Features Overview

- **100% Free & Ad-Free**: No subscriptions, no locked lessons, no ads, and no payment gateway.
- **20 Structured Offline Beginner Lessons**: From *English Alphabet* and *Basic Pronunciation* to *Shopping*, *Restaurant*, *Office*, *Travel*, *Telephone*, and *Interview English* — all available offline with Bengali explanations, phonetic pronunciation guides, Text-to-Speech audio, dialogues, and quizzes.
- **Live AI Voice Teacher (WebRTC + OpenAI Realtime API GA)**: Low-latency voice-to-voice English conversation with server-side ephemeral client secrets (`POST /v1/realtime/client_secrets` + `POST /v1/realtime/calls`), automatic VAD turn detection, barge-in interruption, mic mute/unmute, speaker toggle, session duration limits, live transcript, and inline Bengali grammar feedback.
- **Vocabulary Builder**: 64+ categorized beginner words with Bengali meanings, IPA + Bengali pronunciation, TTS playback, favorites, daily goal tracking, and flashcard revision mode.
- **Grammar Correction**: Detects common Bengali-speaker beginner mistakes (missing copula `is/am`, prepositions, past negatives, subject-verb agreement) with Bengali explanations and speaking repeat practice.
- **Pronunciation Practice**: Uses Android `TextToSpeech` and `SpeechRecognizer` with honest word-recognition scoring and Bengali-speaker phonetic tips (`th`, `v/w`, `f`, `sh`).
- **Progress Dashboard & Conversation History**: Local Room persistence for daily streaks, 7-day speaking minutes Canvas chart, lesson completion, and deletable conversation transcripts (raw audio is never stored).

---

## Project Structure

- `/app/` — Android Jetpack Compose application (`com.example` namespace, `com.aistudio.englishtutor.bnkmqr` applicationId, minSdk 26)
  - `data/local/` — Room database (`EnglishTutorDatabase`), 8 entities, and 8 DAOs
  - `data/preferences/` — Jetpack DataStore preferences (`AppPreferencesManager`)
  - `data/seed/` — All 20 offline beginner lessons + curated vocabulary
  - `data/remote/` — `RealtimeBackendApi` (session auth, token fetch, SDP exchange, health check)
  - `webrtc/` — `RealtimeWebRtcManager` (`PeerConnectionFactory`, `JavaAudioDeviceModule`, `oai-events` DataChannel)
  - `speech/` — `SpeechServiceManager` (Android `TextToSpeech` + `SpeechRecognizer`)
  - `domain/engine/` — `GrammarCorrectionEngine` & `PronunciationEvaluator`
  - `ui/screens/` — All 15 screens + adaptive `NavigationBar`/`NavigationRail`
- `/backend/` — Secure Node.js + TypeScript Express backend for minting OpenAI Realtime GA ephemeral client secrets

---

## Quick Setup

### 1. Start the Secure Backend (`/backend`)

```bash
cd backend
cp .env.example .env
# Edit .env and set OPENAI_API_KEY=sk-...
npm install
npm run dev
```

See [docs/API_CONFIGURATION.md](docs/API_CONFIGURATION.md) for all environment variables, security controls, and OpenAI Realtime GA details.

### 2. Configure the Android App

- On Android Emulator pointing to your local machine backend, the app defaults to `http://10.0.2.2:8787` (`BuildConfig.DEFAULT_BACKEND_URL`).
- On a physical device, open **Settings** inside the app and enter your HTTPS backend URL (e.g., `https://tutor-api.yourdomain.com`), then tap **সার্ভার সংযোগ যাচাই ও সেভ করুন** to test `/health`.
- **Important**: Never put `OPENAI_API_KEY` inside the Android app or `local.properties`.

### 3. Build, Test, and Release

See [docs/RELEASE_AND_TESTING.md](docs/RELEASE_AND_TESTING.md) for unit/Robolectric tests, debug APK build commands, and signed release APK instructions.
