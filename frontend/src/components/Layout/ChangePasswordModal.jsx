import React, { useState } from 'react';
import { KeyRound, ShieldCheck, AlertCircle } from 'lucide-react';
import { authService } from '../../services/authService';

export default function ChangePasswordModal({ user, onClose, onPasswordChanged }) {
  const isMfaEnabled = !!user?.mfaEnabled;
  const [activeMode, setActiveMode] = useState('password'); // 'password' | 'mfa'

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [mfaCode, setMfaCode] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (newPassword.length < 6) {
      setError('New password must be at least 6 characters long.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('New password and confirm password do not match.');
      return;
    }

    if (activeMode === 'password' && !currentPassword.trim()) {
      setError('Please enter your current password.');
      return;
    }

    if (activeMode === 'mfa' && !mfaCode.trim()) {
      setError('Please enter your 6-digit 2FA authenticator code.');
      return;
    }

    setLoading(true);

    try {
      const payload = {
        newPassword: newPassword.trim(),
        currentPassword: activeMode === 'password' ? currentPassword : '',
        mfaCode: activeMode === 'mfa' ? mfaCode.trim() : '',
      };

      const res = await authService.changePassword(payload);

      if (res && res.token) {
        localStorage.setItem('accessToken', res.token);
      }

      setSuccess('Password changed successfully!');
      if (onPasswordChanged) onPasswordChanged(res);

      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to change password'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
          <KeyRound size={22} style={{ color: 'var(--primary-color)' }} />
          <h3 style={{ margin: 0 }}>Change Password</h3>
        </div>

        <p className="modal-sub">
          Update your account password securely.
        </p>

        {isMfaEnabled && (
          <div className="auth-tabs" style={{ marginBottom: '16px' }}>
            <button
              type="button"
              className={`tab-btn ${activeMode === 'password' ? 'active' : ''}`}
              onClick={() => {
                setActiveMode('password');
                setError('');
              }}
            >
              Current Password
            </button>
            <button
              type="button"
              className={`tab-btn ${activeMode === 'mfa' ? 'active' : ''}`}
              onClick={() => {
                setActiveMode('mfa');
                setError('');
              }}
            >
              Forgot Password? (Use 2FA)
            </button>
          </div>
        )}

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          {activeMode === 'password' ? (
            <div className="form-group">
              <label>Current Password</label>
              <input
                type="password"
                className="input-field"
                placeholder="••••••••"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                required
              />
            </div>
          ) : (
            <div className="form-group">
              <label style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <ShieldCheck size={16} style={{ color: 'var(--primary-color)' }} />
                Enter 6-Digit Authenticator Code
              </label>
              <input
                type="text"
                className="input-field"
                placeholder="000000"
                maxLength={6}
                value={mfaCode}
                onChange={(e) => setMfaCode(e.target.value)}
                required
                autoFocus
              />
            </div>
          )}

          <div className="form-group">
            <label>New Password</label>
            <input
              type="password"
              className="input-field"
              placeholder="Min 6 characters..."
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Confirm New Password</label>
            <input
              type="password"
              className="input-field"
              placeholder="Re-enter new password..."
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <div className="modal-actions" style={{ marginTop: '20px' }}>
            <button type="button" className="btn btn-secondary" onClick={onClose} disabled={loading}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Updating...' : 'Update Password'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
