import React, { useState, useEffect } from 'react';
import { friendService } from '../../services/friendService';
import { X, Check, UserX, Ban, UserPlus, Clock } from 'lucide-react';

export default function RequestDrawer({ isOpen, onClose, onRefreshFriends }) {
  const [pendingRequests, setPendingRequests] = useState([]);
  const [sentRequests, setSentRequests] = useState([]);
  const [allUsers, setAllUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState('');

  const [loading, setLoading] = useState(false);
  const [actionMsg, setActionMsg] = useState('');
  const [errorMsg, setErrorMsg] = useState('');

  useEffect(() => {
    if (isOpen) {
      loadData();
    }
  }, [isOpen]);

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
      loadData();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to send friend request');
    }
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
          {/* Send Request Form */}
          <div className="drawer-section">
            <h4><UserPlus size={16} /> Send Request</h4>
            <form onSubmit={handleSend} className="send-request-form">
              <select
                className="input-field"
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
              >
                <option value="">Select User...</option>
                {allUsers.map((u) => (
                  <option key={u.userId} value={u.userId}>
                    {u.username} ({u.userId})
                  </option>
                ))}
              </select>
              <div className="input-group-append">
                <input
                  type="text"
                  className="input-field"
                  placeholder="Or paste User ID..."
                  value={selectedUserId}
                  onChange={(e) => setSelectedUserId(e.target.value)}
                />
                <button type="submit" className="btn btn-primary btn-sm">
                  Send
                </button>
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
