import { X } from 'lucide-react';
import type { ReactNode } from 'react';

interface ModalProps {
  title: string;
  onClose: () => void;
  children: ReactNode;
}

export function Modal({ title, onClose, children }: ModalProps) {
  return (
    <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div className="modal">
        <div className="modal-title">
          <span>{title}</span>
          <button className="btn-icon" onClick={onClose} style={{ border: 'none' }}>
            <X size={14} />
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
