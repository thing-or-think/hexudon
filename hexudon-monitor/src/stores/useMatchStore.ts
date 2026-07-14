import { create } from 'zustand';
import type { MatchConfigResponse, MatchStateResponse, ErrorResponse } from '../models/match';

interface MatchStore {
  config: MatchConfigResponse | null;
  state: MatchStateResponse | null;
  teamName: string;
  isLoading: boolean;
  error: ErrorResponse | null;
  useMockData: boolean;
  isSimulating: boolean;
  simulationSpeed: number;
  lastUpdated: Date | null;

  setConfig: (config: MatchConfigResponse) => void;
  setState: (state: MatchStateResponse) => void;
  setTeamName: (name: string) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: ErrorResponse | null) => void;
  toggleMockData: () => void;
  setSimulating: (sim: boolean) => void;
  setSimulationSpeed: (speed: number) => void;
  clearError: () => void;
}

export const useMatchStore = create<MatchStore>((set) => ({
  config: null,
  state: null,
  teamName: 'Team Alpha',
  isLoading: false,
  error: null,
  useMockData: true,
  isSimulating: false,
  simulationSpeed: 2000,
  lastUpdated: null,

  setConfig: (config) => set({ config }),
  setState: (state) => set({ state, lastUpdated: new Date() }),
  setTeamName: (teamName) => set({ teamName }),
  setLoading: (isLoading) => set({ isLoading }),
  setError: (error) => set({ error }),
  toggleMockData: () => set((s) => ({ useMockData: !s.useMockData })),
  setSimulating: (isSimulating) => set({ isSimulating }),
  setSimulationSpeed: (simulationSpeed) => set({ simulationSpeed }),
  clearError: () => set({ error: null }),
}));
