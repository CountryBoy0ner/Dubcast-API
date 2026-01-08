# Features Walkthrough

This page explains the main Dubcast listener features with short step-by-step scenarios.

---

## Feature 1: Listen to Radio (Now Playing)

### Overview
Open the Radio page and start listening. The UI shows **Now Playing** information (track title and cover artwork, if available) and stays in sync with the server timeline.

### How to Use
[Screenshot: `![Radio Page](../assets/images/feature-radio.png)`]

**Step 1:** Open the Radio page
- Navigate to `/radio`.

**Step 2:** Start playback
- Click **Play** in the player.

**Step 3:** Adjust volume (optional)
- Use the volume slider in the player.

**Expected Result:** Audio is playing and the **Now Playing** section displays the current track details.

### Tips
- If cover artwork is not shown, the current track may not have artwork available in its metadata.

---

## Feature 2: Real-time Chat

### Overview
Chat messages are delivered in real time (WebSocket/STOMP). You can read history and send new messages while listening.

### How to Use
[Screenshot: `![Chat](../assets/images/feature-chat.png)`]

**Step 1:** Sign in
- Go to `/login` and sign in.

**Step 2:** Open chat
- Open the chat panel on the Radio page (or navigate to `/chat` if your UI has a dedicated page).

**Step 3:** Send a message
- Type a message and press **Send**.

**Expected Result:** Your message appears in the chat and is visible to other online users.

### Tips
- If messages do not arrive, verify that the backend is running and the WebSocket endpoint is reachable.

---

## Feature 3: Online Listeners Counter

### Overview
Dubcast shows an **online listeners** counter that updates automatically (near real time) while users are actively listening.

### How to Use
[Screenshot: `![Online Counter](../assets/images/feature-online-counter.png)`]

**Step 1:** Open the Radio page
- Navigate to `/radio`.

**Step 2:** Start playback
- Click **Play** (the counter is intended to reflect active listeners).

**Step 3:** Observe updates
- The counter updates automatically as listeners join/leave.

**Expected Result:** The online listeners number changes without refreshing the page.

### Tips
- The counter is presence-based and approximate (it may lag by a few seconds).

---

## Feature 4: User Profile (Bio / Username)

### Overview
Authenticated users can edit a short profile (username and bio). Public profile info can be shown from chat UI (e.g., on user click/hover).

### How to Use
[Screenshot: `![Profile](../assets/images/feature-profile.png)`]

**Step 1:** Sign in
- Go to `/login` and sign in.

**Step 2:** Open your profile
- Navigate to `/profile`.

**Step 3:** Update your information
- Edit **Username** and/or **Bio**, then click **Save**.

**Expected Result:** Your profile updates successfully and public profile info can be displayed where supported in the UI.

### Tips
- If saving fails, ensure the backend API is reachable and your JWT session is valid.
