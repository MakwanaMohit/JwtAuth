import React, { useEffect, useRef } from 'react';
import MessageInput from './MessageInput';
import { MessageSquare, UserMinus } from 'lucide-react';

export default function MessageThread({
  activeTarget, // { username, userId, conversationId, canSend }
  messages,
  currentUserId,
  onSendMessage,
  onRemoveFriend,
  loading,
}) {
  const bottomRef = useRef(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  if (!activeTarget) {
    return (
      <main className="chat-main empty-thread">
        <div className="empty-thread-content">
          <MessageSquare size={48} className="empty-icon" />
          <h3>No Conversation Selected</h3>
          <p>Select a chat from the sidebar or click "Chat" on a friend to begin messaging.</p>
        </div>
      </main>
    );
  }

  return (
    <main className="chat-main">
      <div className="thread-header">
        <div className="thread-header-info" style={{ display: 'flex', alignItems: 'center', gap: '12px', flex: 1 }}>
          <div className="avatar avatar-sm">
            {activeTarget.username.substring(0, 2).toUpperCase()}
          </div>
          <div className="thread-user-info">
            <h3>{activeTarget.username}</h3>
            <span className="thread-sub">ID: {activeTarget.userId}</span>
          </div>
        </div>

        {onRemoveFriend && (
          <button
            className="btn btn-outline btn-xs btn-danger-text"
            style={{ color: 'var(--danger-color)', borderColor: '#fecaca' }}
            onClick={() => onRemoveFriend(activeTarget.userId)}
            title="Remove Friend"
          >
            <UserMinus size={14} /> Remove Friend
          </button>
        )}
      </div>

      <div className="thread-messages">
        {loading ? (
          <div className="loading-spinner">Loading messages...</div>
        ) : messages.length === 0 ? (
          <div className="empty-messages">No messages yet. Send a greeting!</div>
        ) : (
          messages.slice().reverse().map((msg, index) => {
            const isMe = msg.senderId === currentUserId;
            return (
              <div
                key={index}
                className={`message-bubble-wrapper ${isMe ? 'outgoing' : 'incoming'}`}
              >
                <div className="message-bubble">
                  <div className="message-content">{msg.content}</div>
                  <div className="message-time">
                    {msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                  </div>
                </div>
              </div>
            );
          })
        )}
        <div ref={bottomRef} />
      </div>

      <MessageInput
        onSendMessage={(text) => onSendMessage(activeTarget.userId, text)}
        disabled={activeTarget.canSend === false}
      />
    </main>
  );
}
