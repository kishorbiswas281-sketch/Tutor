import test from "node:test";
import assert from "node:assert/strict";
import {
  buildTutorInstructions,
  signAppSessionToken,
  verifyAppSessionToken,
} from "./server.js";

test("signAppSessionToken and verifyAppSessionToken round-trip valid deviceId", () => {
  const deviceId = "123e4567-e89b-12d3-a456-426614174000";
  const token = signAppSessionToken(deviceId, 60);
  const verified = verifyAppSessionToken(token);
  assert.deepEqual(verified, { deviceId });
});

test("verifyAppSessionToken rejects tampered token", () => {
  const deviceId = "123e4567-e89b-12d3-a456-426614174000";
  const token = signAppSessionToken(deviceId, 60);
  const tampered = `${token.slice(0, -4)}aaaa`;
  assert.equal(verifyAppSessionToken(tampered), null);
});

test("buildTutorInstructions includes Bengali beginner rules", () => {
  const prompt = buildTutorInstructions({
    learnerName: "Kishor",
    topic: "Greetings",
    completedLessons: 3,
    languageMode: "BENGALI_PRIMARY",
  });
  assert.ok(prompt.includes("Bengali-speaking absolute beginners"));
  assert.ok(prompt.includes("Kishor"));
  assert.ok(prompt.includes("Greetings"));
});
