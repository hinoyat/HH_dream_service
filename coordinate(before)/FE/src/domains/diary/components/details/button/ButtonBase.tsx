import styled from 'styled-components';

interface ButtonBaseProps {
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
  borderRadius?: string;
  width?: string;
  height?: string;
  disabled?: boolean;
}

const ButtonBase: React.FC<ButtonBaseProps> = ({
  children,
  onClick,
  className,
  borderRadius,
  width,
  height,
  disabled = false,
}) => {
  return (
    <StyledWrapper
      className={className}
      $borderRadius={borderRadius}
      $width={width}
      $height={height}
      $disabled={disabled}>
      <button
        className="button-base"
        onClick={onClick}
        disabled={disabled}>
        {children}
        <div className="hoverEffect">
          <div />
        </div>
      </button>
    </StyledWrapper>
  );
};

const StyledWrapper = styled.div<{
  $borderRadius?: string;
  $width?: string;
  $height?: string;
  $disabled?: boolean;
}>`
  display: block;
  width: ${(props) => props.$width || 'auto'};

  .button-base {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 9px 23px;
    width: 100%;
    height: ${(props) => props.$height || 'auto'};
    border: 0;
    position: relative;
    overflow: hidden;
    border-radius: ${(props) => props.$borderRadius || '10rem'};
    transition: all 0.02s;
    font-weight: bold;
    cursor: ${(props) => (props.$disabled ? 'not-allowed' : 'pointer')};
    opacity: ${(props) => (props.$disabled ? 0.5 : 1)};
    color: rgb(37, 37, 37);
    z-index: 0;
    box-shadow: 0 0px 7px -5px rgba(0, 0, 0, 0.5);
    background-color: rgba(255, 255, 255, 0.6);
  }

  .button-base:hover {
    background: ${(props) =>
      props.$disabled ? 'inherit' : 'rgb(193, 228, 248)'};
    color: ${(props) => (props.$disabled ? 'inherit' : 'rgb(33, 0, 85)')};
  }

  .button-base:active {
    transform: ${(props) => (props.$disabled ? 'none' : 'scale(0.97)')};
  }

  .hoverEffect {
    position: absolute;
    bottom: 0;
    top: 0;
    left: 0;
    right: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: -1;
  }

  .hoverEffect div {
    background: rgb(222, 0, 75);
    background: linear-gradient(
      90deg,
      rgba(222, 0, 75, 1) 0%,
      rgba(191, 70, 255, 1) 49%,
      rgba(0, 212, 255, 1) 100%
    );
    border-radius: 40rem;
    width: 10rem;
    height: 10rem;
    transition: 0.4s;
    filter: blur(20px);
    animation: effect infinite 3s linear;
    opacity: 0.5;
  }

  .button-base:hover .hoverEffect div {
    width: 8rem;
    height: 8rem;
  }

  @keyframes effect {
    0% {
      transform: rotate(0deg);
    }

    100% {
      transform: rotate(360deg);
    }
  }
`;

export default ButtonBase;
