# HEXUDON API Reference

This document describes the REST API of the HEXUDON Game Server. All endpoints are prefixed with `/api/match`.

## General Information

- **Base URL:** `http://localhost:8080` (or configured via server)
- **Content-Type:** `application/json`
- **Default Port:** `8080`
- **Authentication:** No authentication tokens are required, but active team requests must provide the custom header `X-Team-Name`.

---

## Game State Types

### MatchStatus
Represented as a string enum:
- `WAITING`: The game is waiting for teams to register and start.
- `PLAYING`: The game is active; turns/days are progressing.
- `FINISHED`: The game has reached the maximum turn limit (`maxTurns`) or ended.

### TerrainType
Represented as a string enum:
- `PLAIN`: Plain terrain. Movement step cost = 2, fuel cost = 1.
- `MOUNTAIN`: Mountain terrain. Movement step cost = 3, fuel cost = 2.
- `ROAD`: Road terrain. Movement step cost = 1, fuel cost = 2.
- `POND`: Pond/Water terrain. Inaccessible; cannot be walked on.

### AgentType
Represented as a string enum:
- `PATROL`: Patrol agent. Can move around the map and collect Udon from Spots. Consumes fuel.
- `REFUEL`: Refueling agent. Can move around the map. Auto-refuels any Patrol agent standing on the same cell at the start of a movement step. Has unlimited fuel (or rather, is not subject to fuel constraints, though initialized with initial fuel).

### ActionType
- `MOVE`: Move agent to target cell.
- `WAIT`: Wait/idle for the step.

---

## Endpoint Details

### 1. Register a Team
Register a new team in the match. A maximum of `maxTeams` (default 2) teams can register. Upon successful registration, the server automatically spawns 3 agents for the team starting at `(0, 0)`: 2 `PATROL` agents and 1 `REFUEL` agent.

- **HTTP Method:** `POST`
- **Path:** `/api/match/register`
- **Request Headers:** None
- **Request Body (JSON):**
  ```json
  {
    "teamName": "TeamAlpha"
  }
  ```
- **Response Body (JSON - 200 OK):**
  ```json
  {
    "teamName": "TeamAlpha",
    "agents": [
      {
        "id": "A1",
        "type": "PATROL",
        "posX": 0,
        "posY": 0,
        "fuel": 100,
        "remainingSteps": 0
      },
      {
        "id": "A2",
        "type": "PATROL",
        "posX": 0,
        "posY": 0,
        "fuel": 100,
        "remainingSteps": 0
      },
      {
        "id": "A3",
        "type": "REFUEL",
        "posX": 0,
        "posY": 0,
        "fuel": 100,
        "remainingSteps": 0
      }
    ]
  }
  ```
- **Error Responses:**
  - `400 Bad Request` if team name is blank or invalid JSON.
  - `500 Internal Server Error` (or business error mapping to `400/500`) with details:
    - `"Match is not in WAITING state"` if the match has already started.
    - `"Team already exists"` if a team with that name is already registered.
    - `"Max teams reached"` if the maximum team limit has been reached.

---

### 2. Start the Match
Starts the match, transition status to `PLAYING`, resets agent resources (fuel and remaining steps), and resets Udon stock on the spots.

- **HTTP Method:** `POST`
- **Path:** `/api/match/start`
- **Request Headers:** None
- **Request Body:** None
- **Response Body (JSON - 200 OK):**
  Returns the complete current match state (`MatchStateResponse`).
  ```json
  {
    "status": "PLAYING",
    "currentTurn": 1,
    "teams": [
      {
        "teamName": "TeamAlpha",
        "agents": [
          {
            "id": "A1",
            "type": "PATROL",
            "posX": 0,
            "posY": 0,
            "fuel": 100,
            "remainingSteps": 5
          },
          ...
        ]
      }
    ],
    "cells": [
      {
        "x": 0,
        "y": 0,
        "terrainType": "PLAIN"
      },
      ...
    ],
    "currentTurnActions": {},
    "spots": [
      {
        "cell": {
          "x": 10,
          "y": 7,
          "terrainType": "PLAIN"
        },
        "spotType": "FUEL_STATION",
        "teamUdonStocks": {
          "TeamAlpha": 5
        }
      }
    ]
  }
  ```

---

### 3. Get Current Match State
Fetch the full real-time status of the match, including teams, agents, positions, map cells, spots, current day, and actions.

- **HTTP Method:** `GET`
- **Path:** `/api/match/state`
- **Request Headers:** None
- **Request Body:** None
- **Response Body (JSON - 200 OK):**
  Same structure as `MatchStateResponse` (shown in `/api/match/start`).

---

### 4. Submit Daily Action Plans
Submit the action plans for all agents for the current day. This request must be submitted for each day before the turn timer expires. The actions are executed immediately for the submitting team and the simulation results are returned.

- **HTTP Method:** `POST`
- **Path:** `/api/match/actions`
- **Request Headers:**
  - `X-Team-Name`: `[Your Registered Team Name]` (Required)
- **Request Body (JSON):**
  ```json
  {
    "day": 1,
    "agentPlans": [
      {
        "agentId": "A1",
        "actions": [
          {
            "order": 1,
            "actionType": "MOVE",
            "targetX": 1,
            "targetY": 0
          },
          {
            "order": 2,
            "actionType": "WAIT",
            "targetX": null,
            "targetY": null
          }
        ]
      },
      {
        "agentId": "A2",
        "actions": [
          {
            "order": 1,
            "actionType": "WAIT",
            "targetX": null,
            "targetY": null
          }
        ]
      },
      {
        "agentId": "A3",
        "actions": [
          {
            "order": 1,
            "actionType": "WAIT",
            "targetX": null,
            "targetY": null
          }
        ]
      }
    ]
  }
  ```
- **Response Body (JSON - 200 OK):**
  ```json
  {
    "day": 1,
    "agentExecutionResults": [
      {
        "agentId": "A1",
        "actions": [
          {
            "order": 1,
            "actionType": "MOVE",
            "targetX": 1,
            "targetY": 0,
            "timestamp": 1720516800000
          },
          {
            "order": 2,
            "actionType": "WAIT",
            "targetX": null,
            "targetY": null,
            "timestamp": 1720516800100
          }
        ]
      },
      ...
    ]
  }
  ```
- **Error Responses:**
  - `400 Bad Request` if validations fail:
    - `"The submitted day does not match the current game day."`
    - `"Each agent may have only one action plan per day."`
    - `"The request must contain exactly one action plan for each agent in the team."`
    - `"Action orders must be consecutive starting from 1."`
    - Request validation constraint failures (e.g. Day < 1, blank Agent ID, null Action Type, empty action plan lists).
  - `500 Internal Server Error` (game logic exception):
    - `"Team has been disqualified."` (due to exceeding spam rate limit)
    - Pathing or move errors (e.g. invalid target coordinates, moving into `POND`, exceeding agent steps or fuel limits).

---

## Match Lifecycle and Day Transition

1. **Setup Phase:**
   - Server boots.
   - Teams register using `/api/match/register`.
   - The match is started using `/api/match/start`.

2. **Playing Phase:**
   - For each turn (`day = 1, 2, ..., maxTurns`):
     - Teams retrieve the map and current positions via `/api/match/state`.
     - Teams submit their action plans using `/api/match/actions`.
     - The scheduler runs in the background. Every second, it checks if `turnTimeLimitMs` has elapsed since the day started.
     - Once the time limit expires, the server increments the day to `currentTurn + 1`.
     - When transitioning, all agent steps and fuel are reset to `maxStepsPerTurn` and `initialFuel`. Visited spots are cleared. Udon stocks on spots are reset to `initialSpotUdonStock`.
   - Note: If all teams submit actions and the server flag is set properly, it can transition immediately (however, due to a known server issue, `submittedPlan` flag is not set, so day transition always happens via timeout `turnTimeLimitMs`).

3. **Finished Phase:**
   - Once `currentTurn > maxTurns`, the status transitions to `FINISHED`.
   - No further action submissions are allowed. The final score is determined by the team's accumulated `collectedUdon`.
