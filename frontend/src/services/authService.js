import api from './api';

export const authService = {
  async login(username, password) {
    const response = await api.post('/auth/login', { username, password });
    return response.data;
  },

  async signup(username, password, email) {
    const response = await api.post('/auth/signup', { username, password, email });
    return response.data;
  },

  async refresh() {
    const response = await api.post('/auth/refresh');
    return response.data;
  },

  async mfaVerify(code) {
    const response = await api.post('/mfa/verify', { code });
    return response.data;
  },

  async mfaSetup() {
    const response = await api.post('/mfa/setup');
    return response.data;
  },

  async mfaEnable(code) {
    const response = await api.post('/mfa/enable', { code });
    return response.data;
  },

  async mfaDisable(code) {
    const body = code ? { code } : {};
    const response = await api.post('/mfa/disable', body);
    return response.data;
  },
};
