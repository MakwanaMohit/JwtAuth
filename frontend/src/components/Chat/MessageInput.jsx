import React, { useState, useRef } from 'react';
import { Send } from 'lucide-react';

export default function MessageInput({ onSendMessage, disabled }) {
  const [text, setText] = useState('');
  const inputRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!text.trim() || disabled) return;
    onSendMessage(text.trim());
    setText('');
  };

  const handleContainerClick = () => {
    if (!disabled && inputRef.current) {
      inputRef.current.focus();
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="message-input-form"
      onClick={handleContainerClick}
      style={{ cursor: disabled ? 'not-allowed' : 'text' }}
    >
      <input
        ref={inputRef}
        type="text"
        className="message-text-input"
        placeholder={disabled ? 'Cannot send message to this user' : 'Type a message...'}
        value={text}
        onChange={(e) => setText(e.target.value)}
        disabled={disabled}
      />
      <button
        type="submit"
        className="btn btn-primary send-btn"
        disabled={disabled || !text.trim()}
        onClick={(e) => e.stopPropagation()}
      >
        <Send size={16} />
      </button>
    </form>
  );
}
