import React, { useState } from 'react';
import { authService } from '../../services/authService';

export default function MfaModal({ username, onVerified, onCancel }) {
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!code.trim()) return;

    setLoading(true);
    setError('');

    try {
      const response = await authService.mfaVerify(code.trim());
      onVerified(response);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid 2FA TOTP code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card">
        <h3>Two-Factor Verification</h3>
        <p className="modal-sub">Enter the 6-digit code from your authenticator app for <strong>{username}</strong>.</p>

        {error && <div className="alert alert-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <input
            type="text"
            className="input-field"
            placeholder="000000"
            maxLength={6}
            value={code}
            onChange={(e) => setCode(e.target.value)}
            autoFocus
          />

          <div className="modal-actions">
            <button type="button" className="btn btn-secondary" onClick={onCancel}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Verifying...' : 'Verify Code'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
