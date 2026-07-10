import { Config } from "../config/config.js";
import { logger } from "../logger/logger.js";

class ApiClient {
  constructor() {
    this.baseUrl = Config.DEFAULT_BASE_URL;
    this.teamName = Config.DEFAULT_TEAM_NAME;
    this.statusListeners = [];
    this.networkMonitorListeners = [];
    this.isConnected = false;
  }

  setBaseUrl(url) {
    this.baseUrl = url.replace(/\/$/, ""); // Strip trailing slash
    logger.info(`Server Base URL updated to: ${this.baseUrl}`);
  }

  setTeamName(name) {
    this.teamName = name;
    logger.info(`Team Name updated to: ${this.teamName}`);
  }

  subscribeStatus(listener) {
    this.statusListeners.push(listener);
    return () => {
      this.statusListeners = this.statusListeners.filter(l => l !== listener);
    };
  }

  subscribeNetworkMonitor(listener) {
    this.networkMonitorListeners.push(listener);
    return () => {
      this.networkMonitorListeners = this.networkMonitorListeners.filter(l => l !== listener);
    };
  }

  notifyStatus(isConnected) {
    this.isConnected = isConnected;
    for (const listener of this.statusListeners) {
      try {
        listener(isConnected);
      } catch (e) {
        console.error(e);
      }
    }
  }

  notifyNetworkMonitor(record) {
    for (const listener of this.networkMonitorListeners) {
      try {
        listener(record);
      } catch (e) {
        console.error(e);
      }
    }
  }

  async request(path, options = {}) {
    const url = `${this.baseUrl}${path}`;
    const method = options.method || "GET";
    const headers = {
      "Content-Type": "application/json",
      ...(options.headers || {})
    };

    // If endpoint requires team name or we have it, send it
    if (this.teamName) {
      headers["X-Team-Name"] = this.teamName;
    }

    let attempts = 0;
    const maxRetries = options.maxRetries ?? Config.MAX_RETRY_ATTEMPTS;

    while (attempts <= maxRetries) {
      attempts++;
      const startTime = performance.now();
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), Config.API_TIMEOUT_MS);

      logger.info(`Sending ${method} request to ${path} (Attempt ${attempts}/${maxRetries + 1})...`, "CLIENT");
      
      const reqRecord = {
        timestamp: new Date(),
        method,
        path,
        requestBody: options.body ? JSON.parse(options.body) : null,
        status: "PENDING",
        latency: 0,
        statusCode: null,
        retry: attempts - 1
      };
      this.notifyNetworkMonitor(reqRecord);

      try {
        const fetchOptions = {
          ...options,
          headers,
          signal: controller.signal
        };

        const response = await fetch(url, fetchOptions);
        clearTimeout(timeoutId);
        
        const latency = Math.round(performance.now() - startTime);
        reqRecord.latency = latency;
        reqRecord.statusCode = response.status;
        reqRecord.status = response.ok ? "SUCCESS" : "ERROR";

        if (!response.ok) {
          let errMsg = response.statusText;
          try {
            const errJson = await response.json();
            errMsg = errJson.message || JSON.stringify(errJson);
          } catch (_) {}
          
          throw new Error(`HTTP ${response.status}: ${errMsg}`);
        }

        const data = await response.json();
        reqRecord.responseBody = data;
        this.notifyNetworkMonitor(reqRecord);

        logger.info(`${method} ${path} successful. Latency: ${latency}ms`, "SERVER");
        this.notifyStatus(true);
        return { data, latency };

      } catch (error) {
        clearTimeout(timeoutId);
        const latency = Math.round(performance.now() - startTime);
        reqRecord.latency = latency;
        reqRecord.status = "FAILED";
        reqRecord.error = error.message;
        this.notifyNetworkMonitor(reqRecord);

        const isAbort = error.name === "AbortError";
        const errorMsg = isAbort ? "Request timed out" : error.message;
        logger.error(`${method} ${path} failed: ${errorMsg}`, "SERVER");

        if (attempts <= maxRetries && !isAbort) {
          logger.warn(`Retrying in ${Config.RETRY_DELAY_MS}ms...`, "CLIENT");
          await new Promise(resolve => setTimeout(resolve, Config.RETRY_DELAY_MS));
        } else {
          this.notifyStatus(false);
          throw error;
        }
      }
    }
  }

  async registerTeam(teamName) {
    this.setTeamName(teamName);
    return this.request("/api/match/register", {
      method: "POST",
      body: JSON.stringify({ teamName })
    });
  }

  async startMatch() {
    return this.request("/api/match/start", {
      method: "POST"
    });
  }

  async getMatchState() {
    return this.request("/api/match/state", {
      method: "GET",
      maxRetries: 0 // Do not retry background polling requests to avoid flooding logs
    });
  }

  async submitActions(day, agentPlans) {
    const payload = {
      day,
      agentPlans: Object.keys(agentPlans).map(agentId => ({
        agentId,
        actions: agentPlans[agentId]
      }))
    };
    return this.request("/api/match/actions", {
      method: "POST",
      body: JSON.stringify(payload)
    });
  }
}

export const apiClient = new ApiClient();
export default apiClient;
