import { useEffect, useRef } from 'react';
import { useMatchStore } from '../stores/useMatchStore';
import { useToastStore } from '../stores/useToastStore';
import { matchApi } from '../api/matchApi';
import { MOCK_STATE } from '../mock/match.mock';
import type { AxiosError } from 'axios';
import type { ErrorResponse } from '../models/match';

export function useMatchState(pollingInterval = 3000) {
  const {
    state,
    teamName,
    useMockData,
    isLoading,
    isSimulating,
    setState,
    setLoading,
    setError,
    lastUpdated,
  } = useMatchStore();
  const { addToast } = useToastStore();
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetchState = async () => {
    if (useMockData) {
      // Simulate live data by slightly mutating mock
      const mockWithTurn = {
        ...MOCK_STATE,
        turn: Math.min((state?.turn ?? 0) + 1, 50),
      };
      setState(mockWithTurn);
      return;
    }

    setLoading(true);
    try {
      const data = await matchApi.getState(teamName);
      setState(data);
    } catch (err) {
      const axiosErr = err as AxiosError<ErrorResponse>;
      const errData = axiosErr.response?.data ?? {
        code: 'NETWORK_ERROR',
        message: 'Cannot reach backend server.',
        timestamp: Date.now(),
      };
      setError(errData);
      if (!useMockData) {
        addToast({ type: 'warning', title: 'Connection Lost', message: 'Using cached data.' });
        setState(MOCK_STATE);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchState();
    if (isSimulating) {
      intervalRef.current = setInterval(fetchState, pollingInterval);
    }
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [teamName, useMockData, isSimulating, pollingInterval]);

  return { state, isLoading, lastUpdated, refetch: fetchState };
}
