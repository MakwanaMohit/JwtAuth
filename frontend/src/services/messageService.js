import api from './api';

export const messageService = {
  async getConversations() {
    const response = await api.get('/messages/conversations');
    return response.data?.data || [];
  },

  async getMessages(conversationId) {
    const response = await api.post('/messages/list', conversationId, {
      headers: {
        'Content-Type': 'text/plain',
      },
    });
    return response.data?.data || [];
  },

  async sendMessage(receiverId, content) {
    const response = await api.post('/messages/send', { receiverId, content });
    return response.data;
  },
};
