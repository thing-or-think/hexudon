import { useEffect } from 'react';
import { useMatchStore } from '../stores/useMatchStore';
import { useToastStore } from '../stores/useToastStore';
import { matchApi } from '../api/matchApi';
import { MOCK_CONFIG } from '../mock/match.mock';
import type { AxiosError } from 'axios';
import type { ErrorResponse } from '../models/match';

export function useMatchConfig() {
  const { config, useMockData, isLoading, setConfig, setLoading, setError } = useMatchStore();
  const { addToast } = useToastStore();

  useEffect(() => {
    const fetchConfig = async () => {
      if (useMockData) {
        setConfig(MOCK_CONFIG);
        return;
      }
      setLoading(true);
      try {
        const data = await matchApi.getConfig();
        setConfig(data);
      } catch (err) {
        const axiosErr = err as AxiosError<ErrorResponse>;
        const errData = axiosErr.response?.data ?? {
          code: 'NETWORK_ERROR',
          message: 'Cannot connect to backend. Switching to mock data.',
          timestamp: Date.now(),
        };
        setError(errData);
        addToast({ type: 'error', title: 'Config Error', message: errData.message });
        // Fallback to mock
        setConfig(MOCK_CONFIG);
      } finally {
        setLoading(false);
      }
    };
    fetchConfig();
  }, [useMockData]);

  return { config, isLoading };
}
