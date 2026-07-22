// HEXUDON Monitor Main Application Orchestrator
import { Lobby } from './lobby.js';
import { GameMonitor } from './monitor.js';
import { GameResult } from './result.js';
import { AdminPanel } from './admin.js';

class AppRouter {
    constructor(rootEl, showToast) {
        this.rootEl = rootEl;
        this.showToast = showToast;
        this.currentViewInstance = null;
        
        // Listen to hash changes
        window.addEventListener('hashchange', () => this.route());
    }
    
    navigate(path) {
        window.location.hash = path;
    }
    
    route() {
        // Clean up previous view instance intervals/listeners
        if (this.currentViewInstance && typeof this.currentViewInstance.destroy === 'function') {
            this.currentViewInstance.destroy();
        }
        
        const hash = window.location.hash || '#/';
        
        // Update header active links
        this.updateHeaderActiveState(hash);
        
        // Route matcher
        // 1. Lobby Page: #/ or #/games
        if (hash === '#/' || hash === '#/games') {
            this.currentViewInstance = new Lobby(this.rootEl, this, this.showToast);
            return;
        }
        
        // 2. Admin Panel: #/admin
        if (hash === '#/admin') {
            this.currentViewInstance = new AdminPanel(this.rootEl, this, this.showToast);
            return;
        }
        
        // 3. Game Monitor Page: #/game/{gameId}
        const monitorMatch = hash.match(/^#\/game\/([^\/]+)$/);
        if (monitorMatch) {
            const gameId = decodeURIComponent(monitorMatch[1]);
            this.currentViewInstance = new GameMonitor(this.rootEl, gameId, this, this.showToast);
            return;
        }
        
        // 4. Game Result Page: #/game/{gameId}/result
        const resultMatch = hash.match(/^#\/game\/([^\/]+)\/result$/);
        if (resultMatch) {
            const gameId = decodeURIComponent(resultMatch[1]);
            this.currentViewInstance = new GameResult(this.rootEl, gameId, this, this.showToast);
            return;
        }
        
        // Fallback: Default to Lobby
        this.navigate('/games');
    }
    
    updateHeaderActiveState(hash) {
        const btnLobby = document.getElementById('btn-lobby');
        const btnAdmin = document.getElementById('btn-admin');
        
        if (!btnLobby || !btnAdmin) return;
        
        btnLobby.classList.remove('active');
        btnAdmin.classList.remove('active');
        
        if (hash === '#/admin') {
            btnAdmin.classList.add('active');
        } else if (hash === '#/' || hash.startsWith('#/games') || hash.startsWith('#/game/')) {
            btnLobby.classList.add('active');
        }
    }
}

// Global Toast notification provider
function showToast(message, type = 'info', duration = 3000) {
    const container = document.getElementById('toast-container');
    if (!container) return;
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = 'fa-circle-info';
    if (type === 'success') icon = 'fa-circle-check';
    else if (type === 'error') icon = 'fa-triangle-exclamation';
    
    toast.innerHTML = `
        <i class="fa-solid ${icon}"></i>
        <div class="toast-message">${message}</div>
        <i class="fa-solid fa-xmark toast-close"></i>
    `;
    
    // Close toast click handler
    toast.querySelector('.toast-close').addEventListener('click', () => {
        toast.style.animation = 'slideOut 0.2s ease forwards';
        toast.addEventListener('animationend', () => toast.remove());
    });
    
    container.appendChild(toast);
    
    // Auto dismiss
    setTimeout(() => {
        if (toast.parentNode) {
            toast.style.animation = 'slideOut 0.2s ease forwards';
            toast.addEventListener('animationend', () => toast.remove());
        }
    }, duration);
}

// CSS slideOut animation helper injection
const style = document.createElement('style');
style.textContent = `
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
`;
document.head.appendChild(style);

// Initialization
document.addEventListener('DOMContentLoaded', () => {
    const rootEl = document.getElementById('app-root');
    const router = new AppRouter(rootEl, showToast);
    
    // Header navigation click binds
    document.getElementById('btn-lobby').addEventListener('click', () => {
        router.navigate('/games');
    });
    
    document.getElementById('btn-admin').addEventListener('click', () => {
        router.navigate('/admin');
    });
    
    document.getElementById('nav-brand').addEventListener('click', () => {
        router.navigate('/games');
    });
    
    // Trigger initial routing
    router.route();
});
