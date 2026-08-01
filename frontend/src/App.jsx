import React, { useState, useEffect } from 'react';
import AuthPage from './components/Auth/AuthPage';
import Header from './components/Layout/Header';
import Sidebar from './components/Chat/Sidebar';
import MessageThread from './components/Chat/MessageThread';
import { authService } from './services/authService';
import { friendService } from './services/friendService';
import { messageService } from './services/messageService';
import { useTheme } from './hooks/useTheme';

export default function App() {
  useTheme();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [initLoading, setInitLoading] = useState(true);

  // App Data
  const [conversations, setConversations] = useState([]);
  const [friends, setFriends] = useState([]);

  // Active Chat State
  const [activeTarget, setActiveTarget] = useState(null); // { username, userId, conversationId, canSend }
  const [messages, setMessages] = useState([]);
  const [messagesLoading, setMessagesLoading] = useState(false);

  useEffect(() => {
    attemptAutoLogin();

    const handleLogoutEvent = () => {
      handleLogout();
    };

    window.addEventListener('auth:logout', handleLogoutEvent);
    return () => window.removeEventListener('auth:logout', handleLogoutEvent);
  }, []);

  const attemptAutoLogin = async () => {
    setInitLoading(true);
    try {
      // Auto session restoration on mount via HttpOnly refreshToken cookie
      const res = await authService.refresh();
      if (res && res.token) {
        localStorage.setItem('accessToken', res.token);
        setUser({ userId: res.userid, username: '', mfaEnabled: !!res.mfaEnabled });
        setIsAuthenticated(true);
        loadAppData(res.userid);
      } else {
        setIsAuthenticated(false);
      }
    } catch (err) {
      setIsAuthenticated(false);
    } finally {
      setInitLoading(false);
    }
  };

  const loadAppData = async (currentUserId) => {
    try {
      const [convos, friendList] = await Promise.all([
        messageService.getConversations(),
        friendService.getFriends(),
      ]);

      setConversations(convos);
      setFriends(friendList);

      // Extract username if missing
      if (convos.length > 0 && currentUserId) {
        const myConvo = convos.find((c) => c.userId === currentUserId);
        if (myConvo) {
          setUser((u) => ({ ...u, username: myConvo.username }));
        }
      }
    } catch (err) {
      console.error('Failed to load application data', err);
    }
  };

  const handleLoginSuccess = (authResponse) => {
    setUser({ userId: authResponse.userid, username: '', mfaEnabled: !!authResponse.mfaEnabled });
    setIsAuthenticated(true);
    loadAppData(authResponse.userid);
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    setIsAuthenticated(false);
    setUser(null);
    setActiveTarget(null);
    setMessages([]);
  };

  const handleSelectConvo = async (convo) => {
    setActiveTarget(convo);
    loadMessagesForConversation(convo.conversationId);
  };

  const handleSelectFriend = (friend) => {
    // Check if conversation already exists with this friend
    const existing = conversations.find((c) => c.userId === friend.userId);
    if (existing) {
      handleSelectConvo(existing);
    } else {
      setActiveTarget({
        username: friend.username,
        userId: friend.userId,
        conversationId: null,
        canSend: true,
      });
      setMessages([]);
    }
  };

  const loadMessagesForConversation = async (convoId) => {
    if (!convoId) {
      setMessages([]);
      return;
    }
    setMessagesLoading(true);
    try {
      const msgList = await messageService.getMessages(convoId);
      setMessages(msgList);
    } catch (err) {
      console.error('Failed to load messages', err);
    } finally {
      setMessagesLoading(false);
    }
  };

  const handleSendMessage = async (receiverId, content) => {
    try {
      await messageService.sendMessage(receiverId, content);
      // Refresh conversations & active thread
      const updatedConvos = await messageService.getConversations();
      setConversations(updatedConvos);

      const activeConvo = updatedConvos.find((c) => c.userId === receiverId);
      if (activeConvo) {
        setActiveTarget(activeConvo);
        loadMessagesForConversation(activeConvo.conversationId);
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to send message');
    }
  };

  const handleRemoveFriend = async (targetUserId) => {
    if (!window.confirm('Are you sure you want to remove this friend? You will no longer be able to message each other.')) {
      return;
    }
    try {
      await friendService.removeFriend(targetUserId);
      await loadAppData(user?.userId);
      if (activeTarget && activeTarget.userId === targetUserId) {
        setActiveTarget((prev) => (prev ? { ...prev, canSend: false } : null));
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to remove friend');
    }
  };

  if (initLoading) {
    return (
      <div className="init-screen">
        <div className="loading-spinner">Verifying Session...</div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <AuthPage onLoginSuccess={handleLoginSuccess} />;
  }

  return (
    <div className="app-layout">
      <Header
        user={user}
        onLogout={handleLogout}
        onRefreshFriends={() => loadAppData(user?.userId)}
        onMfaStatusChange={(enabled) => setUser((u) => ({ ...u, mfaEnabled: enabled }))}
      />

      <div className="app-main-body">
        <Sidebar
          conversations={conversations}
          friends={friends}
          selectedConvoId={activeTarget?.conversationId}
          selectedFriendId={activeTarget?.userId}
          onSelectConvo={handleSelectConvo}
          onSelectFriend={handleSelectFriend}
          onStartChat={handleSelectFriend}
          onRemoveFriend={handleRemoveFriend}
        />

        <MessageThread
          activeTarget={activeTarget}
          messages={messages}
          currentUserId={user?.userId}
          onSendMessage={handleSendMessage}
          onRemoveFriend={handleRemoveFriend}
          loading={messagesLoading}
        />
      </div>
    </div>
  );
}
