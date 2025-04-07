// 중복확인 버튼

import React from 'react';
import '@/domains/signup/themes/BoxButton.css';

interface BoxButtonProps {
  text: string;
  onClick?: () => void;
  className?: string;
  bgColor?: string;
  borderColor?: string;
}

const BoxButton: React.FC<BoxButtonProps> = ({
  text,
  onClick,
  className = '',
  bgColor = 'gray',
  borderColor = 'black',
}) => {
  return (
    <div
      className={`box-button ${className}`}
      onClick={onClick}
      style={{
        backgroundColor: bgColor,
        borderColor: borderColor,
      }}>
      <div className="button">
        <span className="dung-font">{text}</span>
      </div>
    </div>
  );
};

export default BoxButton;
