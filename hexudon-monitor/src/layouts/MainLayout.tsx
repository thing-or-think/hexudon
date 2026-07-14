import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/Sidebar';
import { ToastContainer } from '../components/ui/Toast';

export function MainLayout() {
  return (
    <div className="app-shell">
      <Sidebar />
      <main className="main-content">
        <Outlet />
      </main>
      <ToastContainer />
    </div>
  );
}
