// // 꿈분석 감싸는 컴포넌트
// import React, { ReactNode } from 'react';
// import styled from 'styled-components';

// interface GlowFrameProps {
//   children: ReactNode;
// }

// const GlowFrame: React.FC<GlowFrameProps> = ({ children }) => {
//   return (
//     <StyledWrapper>
//       <div className="card">
//         <div className="card-info">
//           <div className="p-4 w-full h-full overflow-auto">{children}</div>
//         </div>
//       </div>
//     </StyledWrapper>
//   );
// };

// const StyledWrapper = styled.div`
//   .card {
//     --background: linear-gradient(to right, #13b37f 0%, #11a3c8 100%);
//     width: 650px;
//     height: 190px;
//     padding: 0.9px;
//     border-radius: 0.7rem;
//     overflow: visible;
//     background: #f7ba2b;
//     background: var(--background);
//     position: relative;
//     z-index: 1;
//   }

//   .card::after {
//     position: absolute;
//     content: '';
//     top: -10px;
//     left: -120px;
//     right: -120px;
//     z-index: -1;
//     height: 100%;
//     width: auto;
//     transform: scale(0.7);
//     filter: blur(34.5px);
//     background: #f7ba2b;
//     background: var(--background);
//     transition: opacity 0.5s;
//   }

//   .card-info {
//     --color: black;
//     background: var(--color);
//     color: rgba(255, 255, 255, 0.9);
//     display: flex;
//     justify-content: center;
//     align-items: center;
//     width: 100%;
//     height: 100%;
//     overflow: visible;
//     border-radius: 0.7rem;
//   }

//   /*Hover*/
//   .card:hover::after {
//     opacity: 0.6;
//     padding: 0.7rem 0;
//     top: 18px;
//     transition: 0.6s;
//   }
// `;

// export default GlowFrame;
