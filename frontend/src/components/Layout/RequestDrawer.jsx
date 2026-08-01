import React, { useState, useEffect, useRef } from 'react';
import { friendService } from '../../services/friendService';
import { X, Check, UserX, Ban, UserPlus, Clock, Search } from 'lucide-react';

export default function RequestDrawer({ isOpen, onClose, onRefreshFriends }) {
  const [pendingRequests, setPendingRequests] = useState([]);
  const [sentRequests, setSentRequests] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const [loading, setLoading] = useState(false);
  const [actionMsg, setActionMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  const dropdownRef = useRef(null);

  useEffect(() => {
    if (isOpen) {
      loadData();
    }
  }, [isOpen]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const loadData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [pending, sent, users] = await Promise.all([
        friendService.getPendingRequests(),
        friendService.getSentRequests(),
        friendService.getUsers(),
      ]);
      setPendingRequests(pending);
      setSentRequests(sent);
      setAllUsers(users);
    } catch (err) {
      setErrorMsg('Failed to load request data');
    } finally {
      setLoading(false);
    }
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!selectedUserId.trim()) return;

    setErrorMsg('');
    setActionMsg('');

    try {
      await friendService.sendRequest(selectedUserId.trim());
      setActionMsg('Friend request sent!');
      setSelectedUserId('');
      setSearchQuery('');
      setIsDropdownOpen(false);
      loadData();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to send friend request');
    }
  };

  const handleSelectUser = (user) => {
    setSelectedUserId(user.userId);
    setSearchQuery(user.username);
    setIsDropdownOpen(false);
  };

  const handleAction = async (senderId, status) => {
    setErrorMsg('');
    setActionMsg('');
    try {
      await friendService.handleAction(senderId, status);
      setActionMsg(`Request ${status.toLowerCase()}!`);
      loadData();
      if (onRefreshFriends) onRefreshFriends();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Action failed');
    }
  };

  const handleCancel = async (receiverId) => {
    setErrorMsg('');
    setActionMsg('');
    try {
      await friendService.cancelRequest(receiverId);
      setActionMsg('Request cancelled');
      loadData();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Cancel failed');
    }
  };

  if (!isOpen) return null;

  // Filter users based on searchQuery
  const filteredUsers = searchQuery.trim()
    ? allUsers.filter(
        (u) =>
          u.username.toLowerCase().includes(searchQuery.trim().toLowerCase()) ||
          u.userId.includes(searchQuery.trim())
      )
    : allUsers;

  return (
    <div className="drawer-overlay" onClick={onClose}>
      <div className="drawer-panel" onClick={(e) => e.stopPropagation()}>
        <div className="drawer-header">
          <h3>Friend Requests</h3>
          <button className="icon-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        {actionMsg && <div className="alert alert-success">{actionMsg}</div>}
        {errorMsg && <div className="alert alert-error">{errorMsg}</div>}

        <div className="drawer-body">
          {/* WhatsApp Style Searchable Combobox */}
          <div className="drawer-section">
            <h4><UserPlus size={16} /> Send Request</h4>
            <form onSubmit={handleSend} className="send-request-form">
              <div className="searchable-select-container" ref={dropdownRef} style={{ position: 'relative' }}>
                <div className="input-group-append">
                  <div style={{ position: 'relative', flex: 1 }}>
                    <Search
                      size={16}
                      style={{
                        position: 'absolute',
                        left: '12px',
                        top: '50%',
                        transform: 'translateY(-50%)',
                        color: 'var(--text-muted)',
                        pointerEvents: 'none',
                      }}
                    />
                    <input
                      type="text"
                      className="input-field"
                      placeholder="Select user or type username/ID..."
                      value={searchQuery}
                      onFocus={() => setIsDropdownOpen(true)}
                      onChange={(e) => {
                        setSearchQuery(e.target.value);
                        setSelectedUserId(e.target.value);
                        setIsDropdownOpen(true);
                      }}
                      style={{ paddingLeft: '36px' }}
                    />
                  </div>
                  <button type="submit" className="btn btn-primary btn-sm" disabled={!selectedUserId.trim()}>
                    Send
                  </button>
                </div>

                {isDropdownOpen && (
                  <ul
                    className="custom-dropdown-list"
                    style={{
                      position: 'absolute',
                      top: '100%',
                      left: 0,
                      right: 0,
                      zIndex: 20,
                      maxHeight: '200px',
                      overflowY: 'auto',
                      background: 'var(--bg-card)',
                      border: '1px solid var(--border-color)',
                      borderRadius: '6px',
                      boxShadow: '0 8px 16px rgba(0,0,0,0.12)',
                      listStyle: 'none',
                      margin: '4px 0 0 0',
                      padding: 0,
                    }}
                  >
                    {filteredUsers.length === 0 ? (
                      <li style={{ padding: '10px 12px', fontSize: '0.85rem', color: 'var(--text-muted)', textAlign: 'center' }}>
                        No matching users found
                      </li>
                    ) : (
                      filteredUsers.map((u) => (
                        <li
                          key={u.userId}
                          onClick={() => handleSelectUser(u)}
                          style={{
                            padding: '10px 14px',
                            cursor: 'pointer',
                            fontSize: '0.85rem',
                            borderBottom: '1px solid var(--border-color)',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                          }}
                          onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--bg-sidebar)')}
                          onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                        >
                          <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>{u.username}</span>
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {u.userId}</span>
                        </li>
                      ))
                    )}
                  </ul>
                )}
              </div>
            </form>
          </div>

          {/* Incoming Pending Requests */}
          <div className="drawer-section">
            <h4>Incoming Requests ({pendingRequests.length})</h4>
            {pendingRequests.length === 0 ? (
              <p className="empty-text">No incoming requests</p>
            ) : (
              <ul className="request-list">
                {pendingRequests.map((req) => (
                  <li key={req.userId} className="request-item">
                    <div className="req-info">
                      <span className="req-user">{req.username}</span>
                    </div>
                    <div className="req-actions">
                      <button
                        className="btn btn-success btn-xs"
                        title="Approve"
                        onClick={() => handleAction(req.userId, 'APPROVED')}
                      >
                        <Check size={14} /> Approve
                      </button>
                      <button
                        className="btn btn-secondary btn-xs"
                        title="Reject"
                        onClick={() => handleAction(req.userId, 'REJECTED')}
                      >
                        <UserX size={14} /> Reject
                      </button>
                      <button
                        className="btn btn-danger btn-xs"
                        title="Block"
                        onClick={() => handleAction(req.userId, 'BLOCKED')}
                      >
                        <Ban size={14} /> Block
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Sent Outgoing Requests */}
          <div className="drawer-section">
            <h4><Clock size={16} /> Sent Requests ({sentRequests.length})</h4>
            {sentRequests.length === 0 ? (
              <p className="empty-text">No pending sent requests</p>
            ) : (
              <ul className="request-list">
                {sentRequests.map((req) => (
                  <li key={req.userId} className="request-item">
                    <div className="req-info">
                      <span className="req-user">{req.username}</span>
                      <span className="badge badge-pending">{req.status}</span>
                    </div>
                    <button
                      className="btn btn-secondary btn-xs"
                      onClick={() => handleCancel(req.userId)}
                    >
                      Cancel
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
