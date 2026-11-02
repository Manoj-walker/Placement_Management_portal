import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, getToken, setToken } from "../api/client.js";
import { mapUser } from "../api/mappers.js";

const SessionContext = createContext(null);

export const ROLE_HOME = {
  student: "/student",
  coordinator: "/coordinator",
  admin: "/admin",
};

const SESSION_KEY = "pn_session";

function readCachedUser() {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function cacheUser(user) {
  try {
    if (user) localStorage.setItem(SESSION_KEY, JSON.stringify(user));
    else localStorage.removeItem(SESSION_KEY);
  } catch {
    /* storage unavailable */
  }
}

export function SessionProvider({ children }) {
  const [user, setUser] = useState(readCachedUser);
  const [loading, setLoading] = useState(() => Boolean(getToken()));

  // Re-validate the stored JWT against the API on page load.
  useEffect(() => {
    if (!getToken()) {
      setLoading(false);
      return undefined;
    }
    let cancelled = false;
    api
      .get("/api/me")
      .then((data) => {
        if (cancelled) return;
        const next = mapUser(data);
        cacheUser(next);
        setUser(next);
      })
      .catch(() => {
        /* 401 already clears the token via the API client */
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const applyAuth = useCallback((response) => {
    const next = mapUser(response.user);
    setToken(response.token);
    cacheUser(next);
    setUser(next);
    return next;
  }, []);

  const login = useCallback(
    async (email, password) => {
      const response = await api.post("/api/auth/login", { email, password });
      return applyAuth(response);
    },
    [applyAuth]
  );

  const register = useCallback(
    async (payload) => {
      const response = await api.post("/api/auth/register", payload);
      return applyAuth(response);
    },
    [applyAuth]
  );

  const logout = useCallback(() => {
    setToken(null);
    cacheUser(null);
    setUser(null);
  }, []);

  const refresh = useCallback(async () => {
    const next = mapUser(await api.get("/api/me"));
    cacheUser(next);
    setUser(next);
    return next;
  }, []);

  return (
    <SessionContext.Provider value={{ user, loading, login, register, logout, refresh }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  return useContext(SessionContext);
}
