// 조회, 생성 모달에 쓰일 배경

import React, { ReactNode } from 'react';
import styled from 'styled-components';

interface ModalBaseProps {
  children: ReactNode;
}

const ModalBase: React.FC<ModalBaseProps> = ({ children }) => {
  return (
    <StyledWrapper>
      <div className="card">
        {[10, 12, 16, 9, 7, 18, 20, 16, 21, 5].map((i, index) => (
          <div
            key={index}
            style={
              {
                '--i': i,
                '--j': [2, 1.8, 2.2, 1.5, 1.7, 2.5, 2, 1.9, 2.1, 1.6][index],
              } as React.CSSProperties
            }
            className="blub"
          />
        ))}
      </div>
      {/* 일기 내용 */}
      <div className="content">{children}</div>
    </StyledWrapper>
  );
};

const StyledWrapper = styled.div`
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: flex;
  border-radius: 15px;
  z-index: hidden;

  .card {
    position: absolute;
    insert: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(45deg, #000000, #0a0a2e);
    display: flex;
    justify-content: space-between;
    overflow: hidden;
  }

  .card:hover {
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(100, 149, 237, 0.3);
  }

  .content {
    position: relative;
    width: 100%;
    height: 100%;
    z-index: 10;
  }

  .blub {
    height: calc(3px * var(--j));
    width: calc(1px * var(--j));
    background: linear-gradient(
      90deg,
      rgba(255, 255, 255, 1) 0%,
      rgba(173, 216, 230, 1) 100%
    );
    box-shadow:
      0 0 20px rgba(255, 255, 255, 0.8),
      0 0 30px rgba(173, 216, 230, 0.6);
    animation: animated linear infinite reverse;
    animation-duration: calc(40s / var(--i));
    rotate: 25deg;
    opacity: 0.8;
    filter: blur(calc(0.5px * var(--j)));
  }

  @keyframes animated {
    0% {
      transform: translateY(250px) scale(0.3) rotate(25deg);
    }
    100% {
      transform: translateY(-40px) scale(1.2) rotate(25deg);
    }
  }
`;

export default ModalBase;
