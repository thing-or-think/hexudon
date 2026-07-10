/**
 * Client In-Memory Logger Module
 */

class Logger {
  constructor() {
    this.logs = [];
    this.listeners = [];
  }

  log(level, type, message) {
    const logEntry = {
      timestamp: new Date(),
      level, // INFO, WARN, ERROR
      type,  // CLIENT, SERVER
      message
    };
    
    this.logs.push(logEntry);
    
    // Cap log list size to prevent memory issues
    if (this.logs.length > 500) {
      this.logs.shift();
    }

    console.log(`[${logEntry.timestamp.toISOString()}] [${level}] [${type}] ${message}`);

    this.notify(logEntry);
  }

  info(message, type = "CLIENT") {
    this.log("INFO", type, message);
  }

  warn(message, type = "CLIENT") {
    this.log("WARN", type, message);
  }

  error(message, type = "CLIENT") {
    this.log("ERROR", type, message);
  }

  server(message, level = "INFO") {
    this.log(level, "SERVER", message);
  }

  client(message, level = "INFO") {
    this.log(level, "CLIENT", message);
  }

  getLogs() {
    return this.logs;
  }

  clear() {
    this.logs = [];
    this.notifyClear();
  }

  subscribe(listener) {
    this.listeners.push(listener);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  notify(entry) {
    for (const listener of this.listeners) {
      try {
        if (listener.onLog) listener.onLog(entry);
      } catch (e) {
        console.error("Logger listener error", e);
      }
    }
  }

  notifyClear() {
    for (const listener of this.listeners) {
      try {
        if (listener.onClear) listener.onClear();
      } catch (e) {
        console.error("Logger listener error", e);
      }
    }
  }
}

export const logger = new Logger();
export default logger;
