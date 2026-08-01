import React, { useState } from 'react';
import { Sun, Moon, SunMoon } from 'lucide-react';
import { authService } from '../../services/authService';
import { useTheme } from '../../hooks/useTheme';
import MfaModal from './MfaModal';

const THEME_ICONS = {
  light: { Icon: Sun, label: 'Light mode — click for Dark' },
  dark: { Icon: Moon, label: 'Dark mode — click for System' },
  system: { Icon: SunMoon, label: 'System mode — click for Light' },
};

export default function AuthPage({ onLoginSuccess }) {
  const { theme, cycleTheme } = useTheme();
  const { Icon: ThemeIcon, label: themeLabel } = THEME_ICONS[theme] ?? THEME_ICONS.system;

  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  // MFA Temporary Token Flow
  const [tempUsername, setTempUsername] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMsg('');
    setLoading(true);

    try {
      if (isLogin) {
        const res = await authService.login(username.trim(), password);
        if (res.tokenType === 'TOKEN_TEMPORARY') {
          // Store temporary access token for MFA verification
          localStorage.setItem('accessToken', res.token);
          setTempUsername(username);
        } else {
          localStorage.setItem('accessToken', res.token);
          onLoginSuccess(res);
        }
      } else {
        await authService.signup(username.trim(), password, email.trim());
        setSuccessMsg('Account registered successfully! Please log in.');
        setIsLogin(true);
        setPassword('');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  const handleMfaVerified = (res) => {
    localStorage.setItem('accessToken', res.token);
    setTempUsername(null);
    onLoginSuccess(res);
  };

  return (
    <div className="auth-container">
      {tempUsername && (
        <MfaModal
          username={tempUsername}
          onVerified={handleMfaVerified}
          onCancel={() => {
            setTempUsername(null);
            localStorage.removeItem('accessToken');
          }}
        />
      )}

      <div className="auth-card" style={{ position: 'relative' }}>
        <button
          type="button"
          className="icon-btn theme-toggle-btn"
          onClick={cycleTheme}
          title={themeLabel}
          aria-label={themeLabel}
          style={{ position: 'absolute', top: '16px', right: '16px' }}
        >
          <ThemeIcon size={20} />
        </button>

        <div className="auth-header">
          <h2>JwtAuth Messenger</h2>
          <p>Secure Spring Boot Chat Application</p>
        </div>

        <div className="auth-tabs">
          <button
            className={`tab-btn ${isLogin ? 'active' : ''}`}
            onClick={() => {
              setIsLogin(true);
              setError('');
              setSuccessMsg('');
            }}
          >
            Login
          </button>
          <button
            className={`tab-btn ${!isLogin ? 'active' : ''}`}
            onClick={() => {
              setIsLogin(false);
              setError('');
              setSuccessMsg('');
            }}
          >
            Register
          </button>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {successMsg && <div className="alert alert-success">{successMsg}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label>Username</label>
            <input
              type="text"
              className="input-field"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. mohit8"
              required
            />
          </div>

          {!isLogin && (
            <div className="form-group">
              <label>Email Address</label>
              <input
                type="email"
                className="input-field"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                required
              />
            </div>
          )}

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              className="input-field"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
            {loading ? 'Processing...' : isLogin ? 'Sign In' : 'Create Account'}
          </button>
        </form>
      </div>
    </div>
  );
}
