# HEXUDON Game Client

Welcome to the **HEXUDON Game Client**, a tactical dashboard and visual controller designed to interface with the HEXUDON Game Server. This client provides a premium user interface to monitor game status, visualize the hexagonal grid map, plan agent actions, validate paths locally, and monitor network logs.

---

## Technical Architecture & Stack

To keep build overhead to zero and enforce modular architecture:
1. **Core & Logic:** Vanilla HTML5 and modern modular ES6 Javascript (**Vanilla ES Modules**).
2. **Styling:** Premium **Vanilla CSS** featuring a dark space theme, glassmorphism card layouts, micro-animations, and Outfit/Inter typography.
3. **Hexagonal Map Renderer:** Interactive **SVG-based map layout** supporting mouse drag panning, scrollwheel zooming, coordinate fitting, hovered tooltip metadata, and overlays for planned agent paths.

### Project Structure
```text
client/
│
├── assets/
│   └── icons.js         # Exports inline SVGs for agents, terrains, status indicators
│
├── config/
│   └── config.js        # Default config parameters (Base URL, polling rate, timeouts)
│
├── logger/
│   └── logger.js        # Global in-memory log manager (INFO, WARN, ERROR, SERVER, CLIENT)
│
├── network/
│   └── api_client.js    # Fetch-based REST client with timeouts, retry, and latency logging
│
├── renderer/
│   └── map_renderer.js  # SVG hex grid visualizer, pan & zoom controller, path draw
│
├── state/
│   └── game_state.js    # Client-side state store (MVVM/Controller) with Undo/Redo queues
│
├── ui/
│   ├── agent_dashboard.js # Agent cards displaying fuel, coordinates, step actions
│   ├── action_editor.js   # Interactive editor for Wait, Move, Reorder, and Duplicate actions
│   └── dashboard_panels.js # Widgets for Match Status countdown, Scoreboard ranks, Network logger
│
├── utils/
│   └── hex_utils.js     # Adjacency, pointy-topped pixel locations, direction 1-6 mapping
│
├── validation/
│   └── action_validator.js # Pre-flight local simulator checking Ponds, fuel, and step constraints
│
├── index.html           # HTML structural layout
├── index.css            # Custom CSS styling sheets
└── app.js               # Main bootstrapper wiring up components and background polling loop
```

---

## Features

### 1. Hexagonal Map Renderer
- Renders cells dynamically colored by terrain (`PLAIN`, `MOUNTAIN`, `ROAD`, `POND`).
- Renders spots (Udon Stock count badges) and Fuel Stations.
- Displays agents. If multiple agents occupy the same cell, they are **stacked** with a visual counter badge.
- Patrol agents display a mini **fuel progress bar** below their icon.
- Highlights path route overlays for the currently selected agent.
- Fully supports pan/drag, zoom in/out, and fitting the viewport layout.

### 2. Action Planning Editor
- Interactive planning: click on adjacent cells on the map to add `MOVE` actions.
- Click `Add WAIT Action` to inject waiting states.
- List card actions showing step and fuel costs.
- Edit, reorder (Move Up/Down), duplicate, delete, and clear actions.
- Fully integrated with **Undo** and **Redo** capabilities.

### 3. Pre-flight Validation
Runs a local simulator matching the server's rules step-by-step before sending requests:
- Detects if a move target goes into a **Pond**.
- Detects if an agent doesn't have enough **Fuel** or **Steps** remaining.
- Detects if targeted coordinates are not **Adjacent** or out of bounds.
- Warns if planned actions exceed the day's maximum steps limit (`maxStepsPerTurn`).
- Validates that action order starts consecutively from 1.

### 4. Event & Network Monitors
- **Event Logs Panel:** Filters and displays real-time `INFO`, `WARN`, `ERROR` console messages categorized by `CLIENT` or `SERVER`.
- **Network Monitor Panel:** Lists all HTTP request endpoints, request body payloads, response data payloads, status codes, latency times, and retry histories.
- **Connection Dot:** Dynamic status indicators showing whether the server is online.
- **Countdown Timer:** Computes and displays a real-time countdown to when the next day transition occurs.

---

## How to Run

### Prerequisites
You need the **HEXUDON Game Server** running locally (usually on port `8080`).
You can start the server from the root directory:
```bash
mvn spring-boot:run
```

### Launch the Client
Since the client utilizes ES6 modules, the files must be served from an HTTP origin. Standard file system protocols (`file:///`) will block ES imports due to CORS restrictions.

Use any simple HTTP server utility:

#### Option A: Node.js (http-server)
If you have Node.js installed, run:
```bash
npx http-server ./client -p 3000
```
Then open `http://localhost:3000` in your browser.

#### Option B: Python
If you have Python installed, run:
```bash
python -m http.server 3000 --directory ./client
```
Then open `http://localhost:3000` in your browser.

#### Option C: VS Code Live Server
If you use VS Code, right-click `client/index.html` and select **Open with Live Server**.

---

## User Settings Config
- Enter your **Server URL** (e.g., `http://localhost:8080`) and **Team Name** (e.g., `TeamAlpha`) in the header inputs.
- Click **Apply**.
- Click **Register** to register your team on the server.
- Click **Start Game** (or wait for the server to start).
- Click on an agent card in the Agent Dashboard to select them.
- Build actions by clicking adjacent tiles on the map, then click **Submit Plans**!
