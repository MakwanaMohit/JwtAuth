import React, { useState, useEffect } from 'react';
import { authService } from '../../services/authService';

export default function TotpSetupModal({ onClose, onMfaEnabled }) {
  const [uri, setUri] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    fetchSetupData();
  }, []);

  const fetchSetupData = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await authService.mfaSetup();
      setUri(data.qrCodeUri || '');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to initialize TOTP setup');
    } finally {
      setLoading(false);
    }
  };

  const handleEnable = async (e) => {
    e.preventDefault();
    if (!code.trim()) return;

    setVerifying(true);
    setError('');

    try {
      await authService.mfaEnable(code.trim());
      setSuccess(true);
      if (onMfaEnabled) onMfaEnabled();
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid TOTP verification code');
    } finally {
      setVerifying(false);
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card">
        <h3>Configure Two-Factor Authentication (2FA)</h3>
        <p className="modal-sub">Scan the link in Google Authenticator or enter the parameters below.</p>

        {error && <div className="alert alert-error">{error}</div>}
        {success && <div className="alert alert-success">MFA enabled successfully!</div>}

        {loading ? (
          <div className="loading-spinner">Generating TOTP Secret...</div>
        ) : (
          <div className="totp-setup-content">
            <div className="totp-uri-box">
              <label>TOTP URI / Key Link:</label>
              <textarea readOnly value={uri} rows={3} className="input-field code-text" />
            </div>

            {!success && (
              <form onSubmit={handleEnable}>
                <div className="form-group">
                  <label>Enter 6-Digit Authenticator Code to Confirm:</label>
                  <input
                    type="text"
                    className="input-field"
                    placeholder="000000"
                    maxLength={6}
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    required
                  />
                </div>

                <div className="modal-actions">
                  <button type="button" className="btn btn-secondary" onClick={onClose}>
                    Close
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={verifying}>
                    {verifying ? 'Enabling...' : 'Enable 2FA'}
                  </button>
                </div>
              </form>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
