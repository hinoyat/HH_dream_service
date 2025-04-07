import React from 'react';
import '../style/LuckyDrawButton.css';

interface LuckyDrawButtonProps {
  onClick: () => void;
  visible: boolean;
}

const LuckyDrawButton: React.FC<LuckyDrawButtonProps> = ({
  onClick,
  visible,
}) => {
  if (!visible) return null;

  return (
    <button
      className="button"
      onClick={onClick}>
      <div className="lid">
        <span className="side top"></span>
        <span className="side front"></span>
        <span className="side back"></span>
        <span className="side left"></span>
        <span className="side right"></span>
      </div>
      <div className="panels">
        <div className="panel-1">
          <div className="panel-2">
            <div className="btn-trigger">
              <span className="btn-trigger-1"></span>
              <span className="btn-trigger-2"></span>
            </div>
          </div>
        </div>
      </div>
    </button>
  );
};

export default LuckyDrawButton;
