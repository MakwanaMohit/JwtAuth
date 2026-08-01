import React, { useState } from 'react';
import { ShieldOff, AlertTriangle } from 'lucide-react';
import { authService } from '../../services/authService';

export default function TotpDisableModal({ username, onClose, onMfaDisabled }) {
  const [confirmUsername, setConfirmUsername] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isMatch = confirmUsername.trim() === username;

  const handleDisable = async (e) => {
    e.preventDefault();
    if (!isMatch) return;

    setLoading(true);
    setError('');

    try {
      await authService.mfaDisable('');
      if (onMfaDisabled) onMfaDisabled();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to disable 2FA');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: 'var(--danger-color)', marginBottom: '8px' }}>
          <ShieldOff size={24} />
          <h3 style={{ margin: 0, color: 'var(--danger-color)' }}>Disable Two-Factor Authentication</h3>
        </div>

        <p className="modal-sub">
          Disabling 2FA will remove extra security from your account.
        </p>

        {error && <div className="alert alert-error">{error}</div>}

        <div className="alert alert-error" style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', fontSize: '0.85rem' }}>
          <AlertTriangle size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
          <div>
            Please confirm by typing your username <strong style={{ textDecoration: 'underline' }}>{username}</strong> below:
          </div>
        </div>

        <form onSubmit={handleDisable}>
          <div className="form-group" style={{ marginTop: '12px' }}>
            <input
              type="text"
              className="input-field"
              placeholder={`Type "${username}" to confirm...`}
              value={confirmUsername}
              onChange={(e) => setConfirmUsername(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="modal-actions" style={{ marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-danger"
              disabled={!isMatch || loading}
            >
              {loading ? 'Disabling...' : 'Disable 2FA'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
