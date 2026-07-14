import { X, CheckCircle, AlertCircle, Info, AlertTriangle } from 'lucide-react';
import { useToastStore } from '../../stores/useToastStore';

const ICONS = {
  success: <CheckCircle size={15} className="text-green-400" />,
  error: <AlertCircle size={15} className="text-red-400" />,
  warning: <AlertTriangle size={15} className="text-yellow-400" />,
  info: <Info size={15} className="text-blue-400" />,
};

export function ToastContainer() {
  const { toasts, removeToast } = useToastStore();

  return (
    <div className="toast-container">
      {toasts.map((toast) => (
        <div key={toast.id} className={`toast toast-${toast.type}`}>
          <div style={{ marginTop: 1 }}>{ICONS[toast.type]}</div>
          <div style={{ flex: 1 }}>
            <div className="toast-title">{toast.title}</div>
            {toast.message && <div className="toast-msg">{toast.message}</div>}
          </div>
          <button
            className="btn-icon"
            onClick={() => removeToast(toast.id)}
            style={{ padding: '2px', border: 'none', marginTop: '-2px' }}
          >
            <X size={13} />
          </button>
        </div>
      ))}
    </div>
  );
}
