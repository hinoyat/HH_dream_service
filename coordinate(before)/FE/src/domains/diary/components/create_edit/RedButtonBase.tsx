// RedButtonBase.tsx
import styled from 'styled-components';

interface RedButtonBaseProps {
  children: React.ReactNode;
  onClick?: () => void;
  className?: string;
  borderRadius?: string;
  width?: string;
  height?: string;
}

const RedButtonBase: React.FC<RedButtonBaseProps> = ({
  children,
  onClick,
  className,
  borderRadius,
  width,
  height,
}) => {
  return (
    <StyledWrapper
      className={className}
      borderRadius={borderRadius}
      width={width}
      height={height}>
      <button
        className="button-base"
        onClick={onClick}>
        {children}
        <div className="hoverEffect">
          <div />
        </div>
      </button>
    </StyledWrapper>
  );
};

const StyledWrapper = styled.div<{
  borderRadius?: string;
  width?: string;
  height?: string;
}>`
  display: block;
  width: ${(props) => props.width || 'auto'};

  .button-base {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: 9px 23px;
    width: 100%;
    height: ${(props) => props.height || 'auto'};
    border: 0;
    position: relative;
    overflow: hidden;
    border-radius: ${(props) => props.borderRadius || '10rem'};
    transition: all 0.02s;
    font-weight: bold;
    cursor: pointer;
    color: rgb(130, 45, 45);
    z-index: 0;
    box-shadow: 0 0px 7px -5px rgba(0, 0, 0, 0.5);
    background-color: rgba(220, 215, 235, 0.7);
  }

  .button-base:hover {
    background: rgba(190, 180, 280, 0.9);
    color: rgb(30, 30, 80);
  }

  .button-base:active {
    transform: scale(0.97);
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
    background: rgb(230, 190, 60); /* 시작 색상: 골드 */
    background: linear-gradient(
      90deg,
      rgba(230, 190, 60, 1) 0%,
      /* 골드 */ rgba(100, 80, 220, 1) 50%,
      /* 블루 바이올렛 */ rgba(0, 180, 130, 1) 100% /* 에메랄드 */
    );
    border-radius: 40rem;
    width: 10rem;
    height: 10rem;
    transition: 0.4s;
    filter: blur(18px);
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

export default RedButtonBase;
