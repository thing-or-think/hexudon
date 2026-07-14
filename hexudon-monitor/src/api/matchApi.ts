import { apiClient, createTeamClient } from './axiosClient';
import type {
  MatchConfigResponse,
  MatchStateResponse,
  SubmitActionRequest,
  TeamRegisterRequest,
} from '../models/match';

export const matchApi = {
  getConfig: async (): Promise<MatchConfigResponse> => {
    const res = await apiClient.get<MatchConfigResponse>('/match/config');
    return res.data;
  },

  getState: async (teamName: string): Promise<MatchStateResponse> => {
    const client = createTeamClient(teamName);
    const res = await client.get<MatchStateResponse>('/match/state');
    return res.data;
  },

  registerTeam: async (request: TeamRegisterRequest): Promise<void> => {
    await apiClient.post('/match/register', request);
  },

  submitActions: async (teamName: string, request: SubmitActionRequest): Promise<void> => {
    const client = createTeamClient(teamName);
    await client.post('/match/actions', request);
  },
};
