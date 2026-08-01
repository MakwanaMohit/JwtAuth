import api from './api';

export const friendService = {
  async getUsers() {
    const response = await api.get('/friends/users');
    return response.data?.data || [];
  },

  async getFriends() {
    const response = await api.get('/friends/list');
    return response.data?.data || [];
  },

  async sendRequest(receiverId) {
    const response = await api.post('/friends/request', { receiverId });
    return response.data;
  },

  async cancelRequest(receiverId) {
    const response = await api.post('/friends/request/cancel', { receiverId });
    return response.data;
  },

  async getPendingRequests() {
    const response = await api.get('/friends/requests');
    return response.data?.data || [];
  },

  async getSentRequests() {
    const response = await api.get('/friends/requests/sent');
    return response.data?.data || [];
  },

  async handleAction(senderId, status) {
    const response = await api.post('/friends/request/action', { senderId, status });
    return response.data;
  },

  async removeFriend(userId) {
    const response = await api.post('/friends/remove', { userId });
    return response.data;
  },
};
