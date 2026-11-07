const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create own profile and read it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const profileRef = aliceDb.collection("users").doc(ALICE_UID);
  await assertSucceeds(
    profileRef.set({
      userId: ALICE_UID,
      email: "alice@test.com",
      displayName: "Alice",
      languagePreference: "auto",
      role: "user",
      status: "active",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(profileRef.get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  // Seed Alice's profile bypassing rules
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      email: "alice@test.com",
      displayName: "Alice",
      languagePreference: "auto",
      role: "user",
      status: "active",
      createdAt: new Date(),
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: can create and read own memories, but not others", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const memoryRef = aliceDb.collection("users").doc(ALICE_UID).collection("memories").doc("mem_1");
  await assertSucceeds(
    memoryRef.set({
      id: "mem_1",
      userId: ALICE_UID,
      title: "Favorite Language",
      content: "Prefers Kotlin and Bangla responses",
      category: "preference",
      createdAt: new Date(),
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("memories").doc("mem_1").get()
  );
});

test("Authenticated user: cannot write memory for another user", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const evilMemoryRef = bobDb.collection("users").doc(ALICE_UID).collection("memories").doc("mem_2");
  await assertFails(
    evilMemoryRef.set({
      id: "mem_2",
      userId: ALICE_UID,
      title: "Hacked Memory",
      content: "Malicious injection",
      category: "preference",
      createdAt: new Date(),
    })
  );
});
