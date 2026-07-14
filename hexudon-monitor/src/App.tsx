import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { MainLayout } from './layouts/MainLayout';
import { Dashboard } from './pages/Dashboard';
import { MapPage } from './pages/MapPage';
import { AgentsPage } from './pages/AgentsPage';
import { TeamsPage } from './pages/TeamsPage';
import { ActionsPage } from './pages/ActionsPage';
import { TrafficPage } from './pages/TrafficPage';
import { ScorePage } from './pages/ScorePage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="map" element={<MapPage />} />
          <Route path="agents" element={<AgentsPage />} />
          <Route path="teams" element={<TeamsPage />} />
          <Route path="actions" element={<ActionsPage />} />
          <Route path="traffic" element={<TrafficPage />} />
          <Route path="score" element={<ScorePage />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
