import React, { useState } from 'react';
import { UserCheck, Shield, LogOut, Users, Sun, Moon, SunMoon } from 'lucide-react';
import RequestDrawer from './RequestDrawer';
import TotpSetupModal from './TotpSetupModal';
import { useTheme } from '../../hooks/useTheme';

const THEME_ICONS = {
  light: { Icon: Sun,     label: 'Light mode — click for Dark' },
  dark:  { Icon: Moon,    label: 'Dark mode — click for System' },
  system:{ Icon: SunMoon, label: 'System mode — click for Light' },
};

export default function Header({ user, onLogout, onRefreshFriends }) {
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isMfaModalOpen, setIsMfaModalOpen] = useState(false);
  const { theme, cycleTheme } = useTheme();

  const { Icon, label } = THEME_ICONS[theme] ?? THEME_ICONS.system;

  return (
    <header className="app-header">
      <div className="header-brand">
        <h1>JwtAuth Chat</h1>
      </div>

      <div className="header-actions">
        <div className="user-badge">
          <span className="user-name">{user?.username || 'User'}</span>
        </div>

        {/* Theme Toggle */}
        <button
          className="icon-btn theme-toggle-btn"
          onClick={cycleTheme}
          title={label}
          aria-label={label}
        >
          <Icon size={20} />
        </button>

        <button
          className="btn btn-outline btn-sm"
          onClick={() => setIsMfaModalOpen(true)}
          title="Configure 2FA TOTP"
        >
          <Shield size={16} /> 2FA Setup
        </button>

        <button
          className="btn btn-secondary btn-sm"
          onClick={() => setIsDrawerOpen(true)}
          title="Friend Requests"
        >
          <Users size={16} /> Friend Requests
        </button>

        <button className="btn btn-danger btn-sm" onClick={onLogout} title="Sign Out">
          <LogOut size={16} /> Logout
        </button>
      </div>

      {isMfaModalOpen && (
        <TotpSetupModal
          onClose={() => setIsMfaModalOpen(false)}
          onMfaEnabled={() => alert('2FA is active on your account!')}
        />
      )}

      <RequestDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        onRefreshFriends={onRefreshFriends}
      />
    </header>
  );
}

