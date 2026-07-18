import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { authApi, TOKEN_KEY, REFRESH_KEY, USER_KEY } from '../api/api';

const AuthContext = createContext(null);

function normalizeUser(profile) {
  if (!profile) return null;
  const next = { ...profile };
  if (next.id == null && next.userId != null) {
    next.id = next.userId;
  }
  if (!next.fullName) {
    next.fullName = `${next.firstName || ''} ${next.lastName || ''}`.trim() || next.email;
  }
  return next;
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [refreshToken, setRefreshToken] = useState(null);
  const [loading, setLoading] = useState(true);

  const persistSession = useCallback((auth) => {
    const access = auth.accessToken || auth.token;
    const profile = normalizeUser(auth.user);
    localStorage.setItem(TOKEN_KEY, access);
    if (auth.refreshToken) {
      localStorage.setItem(REFRESH_KEY, auth.refreshToken);
      setRefreshToken(auth.refreshToken);
    }
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setToken(access);
    setUser(profile);
  }, []);

  const clearSession = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
    setToken(null);
    setRefreshToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    let cancelled = false;

    async function restore() {
      const savedToken = localStorage.getItem(TOKEN_KEY);
      const savedRefresh = localStorage.getItem(REFRESH_KEY);
      const savedUser = localStorage.getItem(USER_KEY);

      if (!savedToken || !savedUser) {
        if (!cancelled) setLoading(false);
        return;
      }

      setToken(savedToken);
      setRefreshToken(savedRefresh);
      setUser(normalizeUser(JSON.parse(savedUser)));

      try {
        const { data } = await authApi.me();
        if (!cancelled) {
          const profile = normalizeUser(data);
          localStorage.setItem(USER_KEY, JSON.stringify(profile));
          setUser(profile);
        }
      } catch {
        if (savedRefresh) {
          try {
            const { data } = await authApi.refresh(savedRefresh);
            if (!cancelled) {
              localStorage.setItem(TOKEN_KEY, data.accessToken);
              setToken(data.accessToken);
              const me = await authApi.me();
              const profile = normalizeUser(me.data);
              localStorage.setItem(USER_KEY, JSON.stringify(profile));
              setUser(profile);
            }
          } catch {
            if (!cancelled) clearSession();
          }
        } else if (!cancelled) {
          clearSession();
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    restore();
    return () => {
      cancelled = true;
    };
  }, [clearSession]);

  const login = useCallback(async (email, password) => {
    const { data } = await authApi.login({ email, password });
    persistSession(data);
    return data;
  }, [persistSession]);

  const register = useCallback(async (payload) => {
    const { data } = await authApi.register(payload);
    return data;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // ignore
    }
    clearSession();
  }, [clearSession]);

  const updateProfile = useCallback(async (payload) => {
    const { data } = await authApi.updateProfile(payload);
    const profile = normalizeUser(data);
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setUser(profile);
    return profile;
  }, []);

  const changePassword = useCallback(async (payload) => {
    const { data } = await authApi.changePassword(payload);
    return data;
  }, []);

  const refreshAccessToken = useCallback(async () => {
    const stored = localStorage.getItem(REFRESH_KEY) || refreshToken;
    if (!stored) throw new Error('No refresh token');
    const { data } = await authApi.refresh(stored);
    localStorage.setItem(TOKEN_KEY, data.accessToken);
    setToken(data.accessToken);
    return data.accessToken;
  }, [refreshToken]);

  const value = useMemo(
    () => ({
      user,
      token,
      refreshToken,
      loading,
      isAuthenticated: !!token && !!user,
      login,
      register,
      logout,
      updateProfile,
      changePassword,
      refreshAccessToken,
    }),
    [
      user,
      token,
      refreshToken,
      loading,
      login,
      register,
      logout,
      updateProfile,
      changePassword,
      refreshAccessToken,
    ]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}

export default AuthContext;
