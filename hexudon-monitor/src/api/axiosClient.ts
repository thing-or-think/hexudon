import axios, { AxiosError } from 'axios';
import type { ErrorResponse } from '../models/match';

const BASE_URL = '/api';

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor
apiClient.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);

// Response interceptor – normalize errors
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    return Promise.reject(error);
  }
);

export const createTeamClient = (teamName: string) =>
  axios.create({
    baseURL: BASE_URL,
    timeout: 10000,
    headers: {
      'Content-Type': 'application/json',
      'X-Team-Name': teamName,
    },
  });
