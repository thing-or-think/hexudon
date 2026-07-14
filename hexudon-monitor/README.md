# HEXUDON Monitor — Frontend Dashboard

A modern, dark-themed tactical game admin dashboard for monitoring HEXUDON match state in real-time.

## 📸 Overview

The dashboard connects to the HEXUDON Spring Boot backend and provides:

- **Live match status** (turn, status, teams, agents)
- **Interactive SVG hex grid** with terrain, traffic overlay, spots, and agents
- **Agent monitoring** with fuel progress bars and filterable table
- **Team ranking** with score cards
- **Action timeline** per turn
- **Traffic heatmap** with history chart
- **Score dashboard** with line chart

## 🚀 Quick Start

### Prerequisites

- Node.js 18+
- HEXUDON backend running on `http://localhost:8080` (optional — mock data works without it)

### Install & Run

```bash
# From the hexudon-monitor directory
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000)

### Build for Production

```bash
npm run build
npm run preview
```

## 🏗️ Project Structure

```
src/
├── api/
│   ├── axiosClient.ts      # Axios instance + team header factory
│   └── matchApi.ts         # API calls: getConfig, getState, registerTeam, submitActions
├── components/
│   ├── ui/
│   │   ├── Badge.tsx        # Status, Traffic, Agent type badges
│   │   ├── Card.tsx         # Card + StatCard
│   │   ├── Modal.tsx        # Dialog modal
│   │   ├── Toast.tsx        # Toast notification container
│   │   └── LoadingSpinner.tsx
│   ├── HexGridViewer.tsx    # SVG hex grid (click cells for details)
│   ├── AgentTable.tsx       # Filterable agent table with modal
│   ├── ScoreBoard.tsx       # Team ranking table
│   ├── TrafficHeatMap.tsx   # Traffic level summary + table
│   ├── ActionTimeline.tsx   # Turn-grouped action history
│   ├── SimulationControls.tsx # Play/Pause/Next + speed
│   └── Sidebar.tsx          # Navigation sidebar
├── hooks/
│   ├── useMatchConfig.ts   # Load map + game config
│   ├── useMatchState.ts    # Poll match state
│   └── usePolling.ts       # Generic polling hook
├── layouts/
│   └── MainLayout.tsx      # App shell: sidebar + outlet + toasts
├── mock/
│   ├── match.mock.ts       # Mock config + state + score history
│   ├── map.mock.ts         # 20×15 deterministic hex cells + spots
│   ├── agent.mock.ts       # Mock agents + team mapping
│   └── traffic.mock.ts     # Mock traffic levels + history
├── models/
│   └── match.ts            # All TypeScript types aligned to backend DTOs
├── pages/
│   ├── Dashboard.tsx       # /dashboard — Main overview
│   ├── MapPage.tsx         # /map — Full hex grid
│   ├── AgentsPage.tsx      # /agents — Agent monitoring
│   ├── TeamsPage.tsx       # /teams — Team management
│   ├── ActionsPage.tsx     # /actions — Action timeline
│   ├── TrafficPage.tsx     # /traffic — Traffic system
│   └── ScorePage.tsx       # /score — Score charts
├── stores/
│   ├── useMatchStore.ts    # Zustand: config, state, simulation, mock flag
│   └── useToastStore.ts    # Zustand: toast notifications
└── utils/
    ├── hexUtils.ts         # Odd-R hex grid math for SVG rendering
    ├── colorUtils.ts       # Terrain/traffic/agent colors
    └── formatters.ts       # Turn, fuel, ms, coordinate formatters
```

## 🔌 API Integration

The frontend proxies `/api` to `http://localhost:8080` via Vite. All backend endpoints:

| Method | Endpoint | Header | Description |
|--------|----------|--------|-------------|
| GET | `/api/match/config` | — | Map + game config |
| GET | `/api/match/state` | `X-Team-Name` | Current match state |
| POST | `/api/match/register` | — | Register a team |
| POST | `/api/match/actions` | `X-Team-Name` | Submit agent actions |

## 🎮 Mock vs Live Mode

Toggle between **Mock** and **Live** mode using the sidebar button:

- **Mock Mode** (default): Uses pre-generated data aligned to backend DTOs. No backend required.
- **Live Mode**: Polls `/api/match/state` every 2–3 seconds. Falls back to mock on error.

## 🗺️ Hex Grid

The grid uses **Odd-R offset coordinates** (matching the backend's `Coordinate(x, y)` system):
- X = column, Y = row
- Odd rows are offset by half a hex width (Odd-R horizontal layout)
- Click any cell to see: terrain type, traffic level, udon spot, agents present

## 🎨 Design

- **Dark tactical game admin** theme
- **JetBrains Mono** for code/numbers, **Inter** for UI text
- Color coding:
  - 🟢 PLAIN terrain, NORMAL traffic, success states
  - 🟡 BUSY traffic, REFUEL agents, warnings
  - 🔴 CONGESTED traffic, low fuel, errors
  - 🔵 PATROL agents, primary actions

## 📡 Real-Time

No WebSocket available on backend (uses HTTP polling):

- Simulation mode: auto-refresh at configurable intervals (0.5s → 5s)
- Uses `usePolling` hook with `setInterval`
- When WebSocket is added to backend, replace `usePolling` with a WebSocket hook in `useMatchState.ts`

## 🛠️ Tech Stack

| Package | Purpose |
|---------|---------|
| React 18 + TypeScript | UI framework |
| Vite | Build tool + dev server |
| TailwindCSS v4 | Utility CSS |
| React Router v6 | Client-side routing |
| Zustand | State management |
| Axios | HTTP client |
| Recharts | Charts (score, traffic) |
| Lucide React | Icons |
