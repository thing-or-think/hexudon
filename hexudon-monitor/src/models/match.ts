// Match domain models aligned to backend DTOs

export type MatchStatus = 'WAITING' | 'PLAYING' | 'FINISHED';

export type TerrainType = 'PLAIN' | 'MOUNTAIN' | 'POND' | 'ROAD';

export type TrafficLevel = 'NORMAL' | 'BUSY' | 'CONGESTED';

export type AgentType = 'PATROL' | 'REFUEL';

export type ActionType = 'WAIT' | 'MOVE';

export type Direction = 'EAST' | 'SOUTHEAST' | 'SOUTHWEST' | 'WEST' | 'NORTHWEST' | 'NORTHEAST';

export type UdonTypeName = 'TANUKI' | 'KITSUNE' | 'TEMPURA' | 'BEEF';

export interface Coordinate {
  x: number;
  y: number;
}

export interface UdonType {
  typeName: UdonTypeName;
}

export interface CellResponse {
  coordinate: Coordinate;
  terrainType: TerrainType;
}

export interface SpotResponse {
  coordinate: Coordinate;
  udonType: UdonType;
  amount: number;
}

export interface TrafficResponse {
  coordinate: Coordinate;
  trafficLevel: TrafficLevel;
}

export interface AgentResponse {
  agentId: string;
  coordinate: Coordinate;
  agentType: AgentType;
  fuel: number;
  step: number;
}

export interface TeamScoreResponse {
  teamName: string;
  uniqueUdonTypeCount: number;
  totalDailyUdon: number;
  totalUdonServings: number;
  totalResponseTimeMillis: number;
}

export interface MatchConfigResponse {
  mapWidth: number;
  mapHeight: number;
  cells: CellResponse[];
  spots: SpotResponse[];
  agentsPerTeam: number;
  maxFuel: number;
  maxStepsPerTurn: number;
  maxTurn: number;
}

export interface MatchStateResponse {
  status: MatchStatus;
  turn: number;
  agents: AgentResponse[];
  traffic: TrafficResponse[];
  spots: SpotResponse[];
  teamScores: TeamScoreResponse[];
}

export interface ActionRequest {
  agentId: string;
  order: number;
  actionType: ActionType;
  coordinate?: Coordinate;
}

export interface SubmitActionRequest {
  actions: ActionRequest[];
}

export interface TeamRegisterRequest {
  teamName: string;
  amountPatrol: number;
  amountRefuel: number;
}

export interface ErrorResponse {
  code: string;
  message: string;
  timestamp: number;
  errors?: ValidationErrorDetail[];
}

export interface ValidationErrorDetail {
  field: string;
  rejectedValue?: string;
  message: string;
}

// UI-enriched agent with team info
export interface AgentWithTeam extends AgentResponse {
  teamName: string;
}

// Simulation history entry
export interface ActionHistoryEntry {
  turn: number;
  teamName: string;
  agentId: string;
  actionType: ActionType;
  fromCoordinate?: Coordinate;
  toCoordinate?: Coordinate;
  status: 'SUCCESS' | 'FAILED' | 'REJECTED';
  timestamp: number;
}
