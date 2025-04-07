import React, { ReactNode, MouseEvent } from 'react';
import '@/domains/spaceship/themes/EditProfileButton.css';

interface EditProfileButtonProps {
  children: ReactNode;
  onClick: (event: MouseEvent<HTMLButtonElement>) => void;
  className?: string;
}

const EditProfileButton: React.FC<EditProfileButtonProps> = ({
  children,
  onClick,
  className = '',
}) => {
  return (
    <button
      className={`btn ${className} `}
      onClick={onClick}>
      {children}
    </button>
  );
};

export default EditProfileButton;
