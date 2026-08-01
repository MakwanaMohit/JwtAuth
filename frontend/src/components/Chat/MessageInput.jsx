import React, { useState } from 'react';
import { Send } from 'lucide-react';

export default function MessageInput({ onSendMessage, disabled }) {
  const [text, setText] = useState('');

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!text.trim() || disabled) return;
    onSendMessage(text.trim());
    setText('');
  };

  return (
    <form onSubmit={handleSubmit} className="message-input-form">
      <input
        type="text"
        className="message-text-input"
        placeholder={disabled ? 'Cannot send message to this user' : 'Type a message...'}
        value={text}
        onChange={(e) => setText(e.target.value)}
        disabled={disabled}
      />
      <button type="submit" className="btn btn-primary send-btn" disabled={disabled || !text.trim()}>
        <Send size={16} />
      </button>
    </form>
  );
}
