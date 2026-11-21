import "dotenv/config";
import crypto from "node:crypto";
import express, { NextFunction, Request, Response } from "express";
import cors from "cors";
import helmet from "helmet";
import rateLimit from "express-rate-limit";
import { z } from "zod";

const PORT = Number(process.env.PORT ?? 8787);
const REALTIME_MODEL = process.env.REALTIME_MODEL ?? "gpt-realtime";
const REALTIME_TRANSCRIPTION_MODEL =
  process.env.REALTIME_TRANSCRIPTION_MODEL ?? "gpt-4o-mini-transcribe";
const REALTIME_VOICE = process.env.REALTIME_VOICE ?? "marin";
const SESSION_MAX_SECONDS = Math.min(
  Math.max(Number(process.env.SESSION_MAX_SECONDS ?? 600), 60),
  1800
);
const DAILY_SESSIONS_PER_DEVICE = Math.max(
  Number(process.env.DAILY_SESSIONS_PER_DEVICE ?? 5),
  1
);
const GLOBAL_DAILY_SESSION_CAP = Math.max(
  Number(process.env.GLOBAL_DAILY_SESSION_CAP ?? 500),
  1
);

const TOKEN_SECRET =
  process.env.SESSION_TOKEN_SECRET && process.env.SESSION_TOKEN_SECRET.length >= 16
    ? process.env.SESSION_TOKEN_SECRET
    : crypto.randomBytes(32).toString("hex");

interface DeviceUsageRecord {
  dateIso: string;
  sessionsCount: number;
  totalSeconds: number;
  lastTokenIssuedAt: number;
}

const deviceUsageMap = new Map<string, DeviceUsageRecord>();
let globalUsageDate = new Date().toISOString().slice(0, 10);
let globalSessionsToday = 0;

function todayIso(): string {
  return new Date().toISOString().slice(0, 10);
}

export function signAppSessionToken(deviceId: string, ttlSeconds = 3600): string {
  const payload = {
    sub: deviceId,
    iat: Math.floor(Date.now() / 1000),
    exp: Math.floor(Date.now() / 1000) + ttlSeconds,
  };
  const encoded = Buffer.from(JSON.stringify(payload), "utf8").toString("base64url");
  const sig = crypto
    .createHmac("sha256", TOKEN_SECRET)
    .update(encoded)
    .digest("base64url");
  return `${encoded}.${sig}`;
}

export function verifyAppSessionToken(token: string): { deviceId: string } | null {
  const parts = token.split(".");
  if (parts.length !== 2) return null;
  const [encoded, sig] = parts;
  const expectedSig = crypto
    .createHmac("sha256", TOKEN_SECRET)
    .update(encoded)
    .digest("base64url");

  const sigBuf = Buffer.from(sig);
  const expBuf = Buffer.from(expectedSig);
  if (sigBuf.length !== expBuf.length || !crypto.timingSafeEqual(sigBuf, expBuf)) {
    return null;
  }

  try {
    const payload = JSON.parse(Buffer.from(encoded, "base64url").toString("utf8")) as {
      sub?: string;
      exp?: number;
    };
    if (!payload.sub || typeof payload.exp !== "number") return null;
    if (Math.floor(Date.now() / 1000) > payload.exp) return null;
    return { deviceId: payload.sub };
  } catch {
    return null;
  }
}

export function buildTutorInstructions(params: {
  learnerName: string;
  topic: string;
  completedLessons: number;
  languageMode: string;
}): string {
  const safeName = params.learnerName.slice(0, 40);
  const safeTopic = params.topic.slice(0, 80);

  return `You are a friendly, patient, professional English-speaking teacher for Bengali-speaking absolute beginners.
Your student has almost zero English-speaking experience.
Student profile:
- Name: ${safeName}
- Completed beginner lessons: ${params.completedLessons} out of 20
- Preferred support mode: ${params.languageMode}
- Today's conversation focus: ${safeTopic}

Teaching rules:
1. Always start with very simple English.
2. Speak slowly and clearly.
3. Use short sentences.
4. Ask only one question at a time.
5. Allow the student enough time to answer.
6. Never embarrass or discourage the student.
7. Correct grammar and pronunciation mistakes politely.
8. Explain difficult English words in Bengali.
9. Give simple examples.
10. Encourage the student to repeat corrected sentences.
11. Gradually increase difficulty based on performance.
12. Use everyday situations for practical conversations.
13. Keep the conversation interactive.
14. Do not use complicated grammar terminology with beginners.
15. Track the student's learning level and weaknesses when progress data is available.

Example correction style:
Teacher: Hello! How are you?
Student: I am fine.
Teacher: Excellent! What is your name?
Student: My name Kishor.
Teacher: Very good! A more correct sentence is 'My name is Kishor.'
Bengali explanation: নিজের নাম বলার সময় 'My name is' ব্যবহার করতে হয়.
Teacher: Now please repeat: My name is Kishor.`;
}

const AuthSessionSchema = z.object({
  deviceId: z.string().uuid(),
});

const RealtimeTokenRequestSchema = z.object({
  topic: z.string().trim().min(1).max(80).default("Everyday Conversation"),
  learnerName: z.string().trim().min(1).max(40).default("Student"),
  completedLessons: z.number().int().min(0).max(20).default(0),
  languageMode: z
    .enum(["BENGALI_PRIMARY", "BILINGUAL", "ENGLISH_FOCUS"])
    .default("BENGALI_PRIMARY"),
});

const UsageReportSchema = z.object({
  durationSeconds: z.number().int().min(0).max(3600),
});

interface AuthenticatedRequest extends Request {
  deviceId?: string;
}

function requireBearerSession(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): void {
  const authHeader = req.header("authorization");
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    res.status(401).json({ error: "UNAUTHORIZED", message: "Missing bearer session token." });
    return;
  }
  const token = authHeader.slice("Bearer ".length).trim();
  const verified = verifyAppSessionToken(token);
  if (!verified) {
    res.status(401).json({ error: "INVALID_TOKEN", message: "Invalid or expired session token." });
    return;
  }
  req.deviceId = verified.deviceId;
  next();
}

export function createApp() {
  const app = express();
  app.set("trust proxy", 1);
  app.use(helmet());

  const allowedOrigins = (process.env.ALLOWED_ORIGINS ?? "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);

  app.use(
    cors({
      origin: (origin, callback) => {
        if (!origin || allowedOrigins.includes(origin)) {
          callback(null, true);
        } else {
          callback(new Error("Origin not allowed by CORS"));
        }
      },
    })
  );
  app.use(express.json({ limit: "16kb" }));

  const authLimiter = rateLimit({
    windowMs: 15 * 60 * 1000,
    max: 30,
    standardHeaders: true,
    legacyHeaders: false,
    message: { error: "RATE_LIMITED", message: "Too many auth requests. Try again shortly." },
  });

  const tokenLimiter = rateLimit({
    windowMs: 60 * 1000,
    max: 5,
    standardHeaders: true,
    legacyHeaders: false,
    message: { error: "RATE_LIMITED", message: "Too many session requests. Please wait." },
  });

  app.get("/health", (_req, res) => {
    res.json({
      status: "ok",
      aiConfigured: Boolean(process.env.OPENAI_API_KEY?.trim()),
      model: REALTIME_MODEL,
      maxSessionSeconds: SESSION_MAX_SECONDS,
      dailySessionsPerDevice: DAILY_SESSIONS_PER_DEVICE,
    });
  });

  app.post("/api/auth/session", authLimiter, (req, res) => {
    const parsed = AuthSessionSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: "INVALID_REQUEST", message: "Valid UUID deviceId is required." });
      return;
    }
    const token = signAppSessionToken(parsed.data.deviceId, 3600);
    res.json({
      sessionToken: token,
      expiresInSeconds: 3600,
    });
  });

  app.post(
    "/api/realtime/token",
    tokenLimiter,
    requireBearerSession,
    async (req: AuthenticatedRequest, res: Response) => {
      const openAiKey = process.env.OPENAI_API_KEY?.trim();
      if (!openAiKey) {
        res.status(503).json({
          error: "AI_NOT_CONFIGURED",
          message: "OpenAI API key is not configured on the backend server.",
        });
        return;
      }

      const parsed = RealtimeTokenRequestSchema.safeParse(req.body ?? {});
      if (!parsed.success) {
        res.status(400).json({
          error: "INVALID_REQUEST",
          message: "Invalid realtime session parameters.",
        });
        return;
      }

      const deviceId = req.deviceId!;
      const today = todayIso();
      if (globalUsageDate !== today) {
        globalUsageDate = today;
        globalSessionsToday = 0;
      }
      if (globalSessionsToday >= GLOBAL_DAILY_SESSION_CAP) {
        res.status(429).json({
          error: "GLOBAL_DAILY_CAP",
          message: "Global daily AI voice capacity reached for today.",
        });
        return;
      }

      const existing = deviceUsageMap.get(deviceId);
      const record: DeviceUsageRecord =
        existing && existing.dateIso === today
          ? existing
          : { dateIso: today, sessionsCount: 0, totalSeconds: 0, lastTokenIssuedAt: 0 };

      if (record.sessionsCount >= DAILY_SESSIONS_PER_DEVICE) {
        res.status(429).json({
          error: "DEVICE_DAILY_LIMIT",
          message: "Daily AI conversation limit reached for this device.",
        });
        return;
      }

      const instructions = buildTutorInstructions(parsed.data);

      try {
        const upstream = await fetch("https://api.openai.com/v1/realtime/client_secrets", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${openAiKey}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            expires_after: {
              anchor: "created_at",
              seconds: SESSION_MAX_SECONDS,
            },
            session: {
              type: "realtime",
              model: REALTIME_MODEL,
              instructions,
              audio: {
                input: {
                  transcription: {
                    model: REALTIME_TRANSCRIPTION_MODEL,
                  },
                  turn_detection: {
                    type: "server_vad",
                    threshold: 0.5,
                    prefix_padding_ms: 300,
                    silence_duration_ms: 700,
                    interrupt_response: true,
                  },
                },
                output: {
                  voice: REALTIME_VOICE,
                },
              },
            },
          }),
        });

        if (!upstream.ok) {
          console.error(`OpenAI client_secrets returned HTTP ${upstream.status}`);
          res.status(502).json({
            error: "UPSTREAM_AI_ERROR",
            message: "Unable to initialize AI voice session right now.",
          });
          return;
        }

        const data = (await upstream.json()) as {
          value?: string;
          expires_at?: number;
          client_secret?: { value?: string; expires_at?: number };
        };
        const clientSecret = data.value ?? data.client_secret?.value;
        const expiresAt = data.expires_at ?? data.client_secret?.expires_at ?? 0;

        if (!clientSecret) {
          res.status(502).json({
            error: "UPSTREAM_INVALID_PAYLOAD",
            message: "AI service returned an unexpected session payload.",
          });
          return;
        }

        record.sessionsCount += 1;
        record.lastTokenIssuedAt = Date.now();
        deviceUsageMap.set(deviceId, record);
        globalSessionsToday += 1;

        res.json({
          clientSecret,
          expiresAt,
          model: REALTIME_MODEL,
          rtcUrl: "https://api.openai.com/v1/realtime/calls",
          maxSessionSeconds: SESSION_MAX_SECONDS,
          remainingSessionsToday: Math.max(0, DAILY_SESSIONS_PER_DEVICE - record.sessionsCount),
        });
      } catch (err) {
        console.error("Failed to create OpenAI realtime client secret", err instanceof Error ? err.message : "unknown");
        res.status(502).json({
          error: "UPSTREAM_UNREACHABLE",
          message: "Could not reach OpenAI Realtime API.",
        });
      }
    }
  );

  app.post("/api/usage/report", requireBearerSession, (req: AuthenticatedRequest, res: Response) => {
    const parsed = UsageReportSchema.safeParse(req.body);
    if (!parsed.success) {
      res.status(400).json({ error: "INVALID_REQUEST", message: "Invalid usage payload." });
      return;
    }
    const deviceId = req.deviceId!;
    const today = todayIso();
    const record = deviceUsageMap.get(deviceId);
    if (record && record.dateIso === today) {
      record.totalSeconds += parsed.data.durationSeconds;
      deviceUsageMap.set(deviceId, record);
    }
    res.json({ recorded: true });
  });

  return app;
}

if (process.env.NODE_ENV !== "test") {
  const app = createApp();
  app.listen(PORT, () => {
    console.log(`AI English Tutor backend listening on port ${PORT}`);
  });
}
