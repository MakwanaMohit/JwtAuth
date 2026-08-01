import React, { useState } from 'react';
import { MessageSquare, Users, Search, PlusCircle } from 'lucide-react';

export default function Sidebar({
  conversations,
  friends,
  selectedConvoId,
  selectedFriendId,
  onSelectConvo,
  onSelectFriend,
  onStartChat,
}) {
  const [activeTab, setActiveTab] = useState('chats'); // 'chats' | 'friends'
  const [searchTerm, setSearchTerm] = useState('');

  const filteredConversations = conversations.filter((c) =>
    c.username.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const filteredFriends = friends.filter((f) =>
    f.username.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <aside className="chat-sidebar">
      <div className="sidebar-tabs">
        <button
          className={`sidebar-tab ${activeTab === 'chats' ? 'active' : ''}`}
          onClick={() => setActiveTab('chats')}
        >
          <MessageSquare size={16} /> Chats ({conversations.length})
        </button>
        <button
          className={`sidebar-tab ${activeTab === 'friends' ? 'active' : ''}`}
          onClick={() => setActiveTab('friends')}
        >
          <Users size={16} /> Friends ({friends.length})
        </button>
      </div>

      <div className="search-bar">
        <Search size={16} className="search-icon" />
        <input
          type="text"
          className="search-input"
          placeholder="Search..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      <div className="sidebar-list">
        {activeTab === 'chats' ? (
          filteredConversations.length === 0 ? (
            <div className="empty-sidebar">No active conversations</div>
          ) : (
            filteredConversations.map((c) => (
              <div
                key={c.conversationId}
                className={`sidebar-item ${
                  selectedConvoId === c.conversationId ? 'selected' : ''
                }`}
                onClick={() => onSelectConvo(c)}
              >
                <div className="avatar">{c.username.substring(0, 2).toUpperCase()}</div>
                <div className="item-details">
                  <div className="item-header">
                    <span className="item-title">{c.username}</span>
                  </div>
                  <div className="item-preview">
                    {c.lastMessage || 'No messages yet...'}
                  </div>
                </div>
              </div>
            ))
          )
        ) : filteredFriends.length === 0 ? (
          <div className="empty-sidebar">No approved friends yet</div>
        ) : (
          filteredFriends.map((f) => (
            <div
              key={f.userId}
              className={`sidebar-item ${
                selectedFriendId === f.userId ? 'selected' : ''
              }`}
              onClick={() => onSelectFriend(f)}
            >
              <div className="avatar">{f.username.substring(0, 2).toUpperCase()}</div>
              <div className="item-details">
                <div className="item-header">
                  <span className="item-title">{f.username}</span>
                </div>
                <div className="item-sub">User ID: {f.userId}</div>
              </div>
              <button
                className="btn btn-outline btn-xs"
                onClick={(e) => {
                  e.stopPropagation();
                  onStartChat(f);
                }}
                title="Start Chat"
              >
                <PlusCircle size={14} /> Chat
              </button>
            </div>
          ))
        )}
      </div>
    </aside>
  );
}
