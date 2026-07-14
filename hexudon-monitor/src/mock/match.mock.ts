import type { MatchConfigResponse, MatchStateResponse } from '../models/match';
import { MOCK_CELLS, MOCK_SPOTS } from './map.mock';
import { MOCK_AGENTS } from './agent.mock';
import { MOCK_TRAFFIC } from './traffic.mock';

export const MOCK_CONFIG: MatchConfigResponse = {
  mapWidth: 20,
  mapHeight: 15,
  cells: MOCK_CELLS,
  spots: MOCK_SPOTS,
  agentsPerTeam: 2,
  maxFuel: 100,
  maxStepsPerTurn: 5,
  maxTurn: 50,
};

export const MOCK_STATE: MatchStateResponse = {
  status: 'PLAYING',
  turn: 12,
  agents: MOCK_AGENTS,
  traffic: MOCK_TRAFFIC,
  spots: MOCK_SPOTS,
  teamScores: [
    {
      teamName: 'Team Alpha',
      uniqueUdonTypeCount: 3,
      totalDailyUdon: 14,
      totalUdonServings: 22,
      totalResponseTimeMillis: 4520,
    },
    {
      teamName: 'Team Beta',
      uniqueUdonTypeCount: 2,
      totalDailyUdon: 9,
      totalUdonServings: 14,
      totalResponseTimeMillis: 6890,
    },
  ],
};

export const MOCK_SCORE_HISTORY = Array.from({ length: 12 }, (_, i) => ({
  turn: i + 1,
  'Team Alpha': Math.floor(Math.random() * 5) * (i + 1),
  'Team Beta': Math.floor(Math.random() * 4) * (i + 1),
}));
