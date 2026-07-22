// HEXUDON Monitor API Client

const BASE_URL = '/api/game';

/**
 * Handles Response checking and error mapping
 */
async function handleResponse(response) {
    if (response.status === 204) {
        return null;
    }
    
    let data = null;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        data = await response.json();
    }

    if (!response.ok) {
        const error = new Error(data?.message || `HTTP error! Status: ${response.status}`);
        error.status = response.status;
        error.code = data?.errorCode || 'API_ERROR';
        error.errors = data?.errors || null;
        throw error;
    }

    return data;
}

export const GameApi = {
    /**
     * GET /api/game/list
     * Fetches all games available on the server
     */
    async getGames() {
        try {
            const response = await fetch(`${BASE_URL}/list`);
            return await handleResponse(response);
        } catch (error) {
            console.error('getGames error:', error);
            throw error;
        }
    },

    /**
     * GET /api/game/board?game_id={gameId}
     * Fetches static board details
     */
    async getBoard(gameId) {
        try {
            const response = await fetch(`${BASE_URL}/board?game_id=${encodeURIComponent(gameId)}`);
            return await handleResponse(response);
        } catch (error) {
            console.error(`getBoard (${gameId}) error:`, error);
            throw error;
        }
    },

    /**
     * GET /api/game/state?game_id={gameId}
     * Fetches live state details
     */
    async getState(gameId) {
        try {
            const response = await fetch(`${BASE_URL}/state?game_id=${encodeURIComponent(gameId)}`);
            return await handleResponse(response);
        } catch (error) {
            console.error(`getState (${gameId}) error:`, error);
            throw error;
        }
    },

    /**
     * GET /api/game/result?game_id={gameId}
     * Fetches final standings
     */
    async getResult(gameId) {
        try {
            const response = await fetch(`${BASE_URL}/result?game_id=${encodeURIComponent(gameId)}`);
            return await handleResponse(response);
        } catch (error) {
            console.error(`getResult (${gameId}) error:`, error);
            throw error;
        }
    },

    /**
     * POST /api/game/generate
     * Generates a random map configuration preview
     */
    async generateMap(width, height, teams) {
        try {
            const response = await fetch(`${BASE_URL}/generate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ width, height, teams })
            });
            return await handleResponse(response);
        } catch (error) {
            console.error('generateMap error:', error);
            throw error;
        }
    },

    /**
     * POST /api/game/init
     * Configures and creates a new game
     */
    async initGame(config) {
        try {
            const response = await fetch(`${BASE_URL}/init`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(config)
            });
            return await handleResponse(response);
        } catch (error) {
            console.error('initGame error:', error);
            throw error;
        }
    },

    /**
     * DELETE /api/game/{gameId}
     * Deletes a match configuration
     */
    async deleteGame(gameId) {
        try {
            const response = await fetch(`${BASE_URL}/${encodeURIComponent(gameId)}`, {
                method: 'DELETE'
            });
            return await handleResponse(response);
        } catch (error) {
            console.error(`deleteGame (${gameId}) error:`, error);
            throw error;
        }
    }
};
