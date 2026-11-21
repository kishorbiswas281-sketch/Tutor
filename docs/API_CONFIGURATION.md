# API & Backend Configuration Guide

## Security Architecture

1. **Zero Client Secrets**: The `OPENAI_API_KEY` is stored exclusively on the backend server (`backend/.env` or server secret manager) and is **never** packaged into the Android APK.
2. **Short-Lived Ephemeral Client Secrets (OpenAI Realtime API GA)**:
   - The Android app first obtains a short-lived signed HMAC bearer token from `POST /api/auth/session` bound to its `deviceId`.
   - The app then calls `POST /api/realtime/token` with `Authorization: Bearer <sessionToken>`.
   - The backend validates the request with `zod`, enforces IP rate limits and per-device/global daily caps, and calls:
     `POST https://api.openai.com/v1/realtime/client_secrets`
   - The backend returns the ephemeral `clientSecret` (`ek_...`), `rtcUrl` (`https://api.openai.com/v1/realtime/calls`), and `maxSessionSeconds` to the Android app.
3. **Direct Low-Latency WebRTC**:
   - The Android client creates an SDP offer via `stream-webrtc-android` (`PeerConnection` + `oai-events` DataChannel) and exchanges it directly with `POST https://api.openai.com/v1/realtime/calls` using `Authorization: Bearer <clientSecret>`.

---

## Backend Environment Variables (`backend/.env`)

Copy `backend/.env.example` to `backend/.env`:

| Variable | Required | Default | Description |
| --- | --- | --- | --- |
| `PORT` | No | `8787` | HTTP server port |
| `OPENAI_API_KEY` | **Yes** | *(empty)* | Your OpenAI API key for `/v1/realtime/client_secrets` |
| `SESSION_TOKEN_SECRET` | Recommended | Random 32-byte hex | HMAC-SHA256 secret for signing app session tokens |
| `REALTIME_MODEL` | No | `gpt-realtime` | OpenAI Realtime GA model ID |
| `REALTIME_TRANSCRIPTION_MODEL` | No | `gpt-4o-mini-transcribe` | Input audio transcription model |
| `REALTIME_VOICE` | No | `marin` | AI teacher voice (`marin`, `alloy`, `coral`, `sage`, etc.) |
| `SESSION_MAX_SECONDS` | No | `600` | Maximum voice session duration in seconds (60–1800) |
| `DAILY_SESSIONS_PER_DEVICE` | No | `5` | Maximum daily AI voice sessions per device |
| `GLOBAL_DAILY_SESSION_CAP` | No | `500` | Global daily AI voice session safety cap |
| `ALLOWED_ORIGINS` | No | *(empty)* | Comma-separated CORS origins for browser testing |

---

## Production Deployment Recommendations

- Host `/backend` behind HTTPS (Cloud Run, Fly.io, Railway, or Nginx TLS reverse proxy).
- Update the backend URL in the app's **Settings** screen or set `DEFAULT_BACKEND_URL` in `app/build.gradle.kts` before building a release APK.
- Persist `deviceUsageMap` in Redis or PostgreSQL if scaling across multiple backend instances.
