import type { AgentResponse } from '../models/match';

export const MOCK_AGENTS: AgentResponse[] = [
  {
    agentId: 'A1',
    coordinate: { x: 3, y: 2 },
    agentType: 'PATROL',
    fuel: 85,
    step: 3,
  },
  {
    agentId: 'A2',
    coordinate: { x: 5, y: 4 },
    agentType: 'REFUEL',
    fuel: 100,
    step: 5,
  },
  {
    agentId: 'A3',
    coordinate: { x: 14, y: 8 },
    agentType: 'PATROL',
    fuel: 62,
    step: 2,
  },
  {
    agentId: 'A4',
    coordinate: { x: 17, y: 11 },
    agentType: 'REFUEL',
    fuel: 100,
    step: 5,
  },
];

export const MOCK_AGENT_TEAMS: Record<string, string> = {
  A1: 'Team Alpha',
  A2: 'Team Alpha',
  A3: 'Team Beta',
  A4: 'Team Beta',
};
