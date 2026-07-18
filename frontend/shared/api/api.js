import axios from 'axios';

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080/api';

export const TOKEN_KEY = 'travelify_token';
export const REFRESH_KEY = 'travelify_refresh';
export const USER_KEY = 'travelify_user';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

let refreshPromise = null;

function clearSessionStorage() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(USER_KEY);
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const status = error.response?.status;

    if (status !== 401 || !original || original._retry) {
      return Promise.reject(error);
    }

    const url = original.url || '';
    if (url.includes('/auth/login') || url.includes('/auth/register') || url.includes('/auth/refresh')) {
      return Promise.reject(error);
    }

    const refreshToken = localStorage.getItem(REFRESH_KEY);
    if (!refreshToken) {
      clearSessionStorage();
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
      return Promise.reject(error);
    }

    original._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = axios
          .post(`${API_BASE_URL}/auth/refresh`, { refreshToken })
          .then((res) => {
            const access = res.data.accessToken;
            localStorage.setItem(TOKEN_KEY, access);
            return access;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      const access = await refreshPromise;
      original.headers.Authorization = `Bearer ${access}`;
      return api(original);
    } catch (refreshError) {
      clearSessionStorage();
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
      return Promise.reject(refreshError);
    }
  }
);

export const authApi = {
  login: (payload) => api.post('/auth/login', payload),
  register: (payload) => api.post('/auth/register', payload),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken }),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
  updateProfile: (payload) => api.put('/auth/me', payload),
  changePassword: (payload) => api.put('/auth/change-password', payload),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }),
  resetPassword: (token, newPassword) =>
    api.post('/auth/reset-password', { token, newPassword }),
};

export const packageApi = {
  list: () => api.get('/packages'),
  get: (id) => api.get(`/packages/${id}`),
};

export const customerApi = {
  bookings: () => api.get('/customer/bookings'),
  createBooking: (payload) => api.post('/customer/bookings', payload),
};

export const agentApi = {
  packages: () => api.get('/agent/packages'),
  createPackage: (payload) => api.post('/agent/packages', payload),
  updatePackage: (id, payload) => api.put(`/agent/packages/${id}`, payload),
  bookings: () => api.get('/agent/bookings'),
  updateBookingStatus: (id, status) => api.patch(`/agent/bookings/${id}/status`, { status }),
  // Trip Management
  listTrips: (params) => api.get('/agent/trips', { params }),
  getTrip: (id) => api.get(`/agent/trips/${id}`),
  createTrip: (payload) => api.post('/agent/trips', payload),
  updateTrip: (id, payload) => api.put(`/agent/trips/${id}`, payload),
  deleteTrip: (id) => api.delete(`/agent/trips/${id}`),
  copyTrip: (id) => api.post(`/agent/trips/${id}/copy`),
  listTemplates: (params) => api.get('/agent/templates', { params }),
  getTemplate: (id) => api.get(`/agent/templates/${id}`),
  // Service Management
  listServices: (params) => api.get('/provider/services', { params }),
  getService: (id) => api.get(`/provider/services/${id}`),
  createService: (payload) => api.post('/provider/services', payload),
  updateService: (id, payload) => api.put(`/provider/services/${id}`, payload),
  deleteService: (id) => api.delete(`/provider/services/${id}`),
  // Categories
  listCategories: () => api.get('/categories'),
};

export const adminApi = {
  overview: () => api.get('/admin/overview'),
  users: (params) => api.get('/admin/users', { params }),
  getUser: (id) => api.get(`/admin/users/${id}`),
  updateUserRole: (id, role) => api.put(`/admin/users/${id}/role`, { role }),
  updateUserStatus: (id, isActive) => api.put(`/admin/users/${id}/status`, { isActive }),
  deleteUser: (id) => api.delete(`/admin/users/${id}`),
  packages: () => api.get('/admin/packages'),
  bookings: () => api.get('/admin/bookings'),
};

export default api;
