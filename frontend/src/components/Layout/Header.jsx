import React, { useState, Activity } from 'react';
import { Shield, ShieldOff, LogOut, Users, Sun, Moon, SunMoon, KeyRound } from 'lucide-react';
import RequestDrawer from './RequestDrawer';
import TotpSetupModal from './TotpSetupModal';
import TotpDisableModal from './TotpDisableModal';
import ChangePasswordModal from './ChangePasswordModal';
import { useTheme } from '../../hooks/useTheme';

const THEME_ICONS = {
  light:  { Icon: Sun,      label: 'Light mode — click for Dark' },
  dark:   { Icon: Moon,     label: 'Dark mode — click for System' },
  system: { Icon: SunMoon,  label: 'System mode — click for Light' },
};

export default function Header({ user, onLogout, onRefreshFriends, onMfaStatusChange }) {
  const [isDrawerOpen, setIsDrawerOpen]         = useState(false);
  const [isSetupModalOpen, setIsSetupModalOpen]   = useState(false);
  const [isDisableModalOpen, setIsDisableModalOpen] = useState(false);
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const { theme, cycleTheme } = useTheme();

  const { Icon, label } = THEME_ICONS[theme] ?? THEME_ICONS.system;
  const isMfaEnabled = !!user?.mfaEnabled;

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

        {/* Change Password Button */}
        <button
          className="btn btn-outline btn-sm"
          onClick={() => setIsPasswordModalOpen(true)}
          title="Change Account Password"
        >
          <KeyRound size={16} /> Password
        </button>

        {/* 2FA Button — changes based on current state */}
        {isMfaEnabled ? (
          <button
            className="btn btn-danger btn-sm"
            onClick={() => setIsDisableModalOpen(true)}
            title="Disable Two-Factor Authentication"
          >
            <ShieldOff size={16} /> Disable 2FA
          </button>
        ) : (
          <button
            className="btn btn-outline btn-sm"
            onClick={() => setIsSetupModalOpen(true)}
            title="Configure 2FA TOTP"
          >
            <Shield size={16} /> 2FA Setup
          </button>
        )}

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

      {/* Change Password Modal */}
      <Activity mode={isPasswordModalOpen ? 'visible' : 'hidden'}>
        <ChangePasswordModal
          user={user}
          onClose={() => setIsPasswordModalOpen(false)}
          onPasswordChanged={() => setIsPasswordModalOpen(false)}
        />
      </Activity>

      {/* 2FA Setup Modal */}
      <Activity mode={isSetupModalOpen ? 'visible' : 'hidden'}>
        <TotpSetupModal
          onClose={() => setIsSetupModalOpen(false)}
          onMfaEnabled={() => {
            setIsSetupModalOpen(false);
            if (onMfaStatusChange) onMfaStatusChange(true);
          }}
        />
      </Activity>

      {/* 2FA Disable Modal */}
      <Activity mode={isDisableModalOpen ? 'visible' : 'hidden'}>
        <TotpDisableModal
          username={user?.username}
          onClose={() => setIsDisableModalOpen(false)}
          onMfaDisabled={() => {
            setIsDisableModalOpen(false);
            if (onMfaStatusChange) onMfaStatusChange(false);
          }}
        />
      </Activity>

      <Activity mode={isDrawerOpen ? 'visible' : 'hidden'}>
        <RequestDrawer
          isOpen={isDrawerOpen}
          onClose={() => setIsDrawerOpen(false)}
          onRefreshFriends={onRefreshFriends}
        />
      </Activity>
    </header>
  );
}

