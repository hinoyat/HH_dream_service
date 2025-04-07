import React from 'react';
import '../../styles/DetailHeader.css';

interface DetailHeaderProps {
  title: string;
  created_at: string;
  isPublic: boolean;
}

const DetailHeader: React.FC<DetailHeaderProps> = ({
  title,
  created_at,
  isPublic,
}) => {
  return (
    <div className="flex flex-nowrap w-full items-center justify-between">
      <div className="flex flex-col">
        {/* 제목 */}
        <h1 className="text-white text-lg font-semibold">{title}</h1>
        {/* 날짜 */}
        <p className="text-[#FFFFFF]70 text-sm">{created_at}</p>
      </div>

      {/* 부모 컨테이너에 relative와 크기 지정 */}
      {/* <div
        className="relative"
        style={{ width: '64px', height: '64px' }}>
        <div className="absolute top-2.5 right-2/3"> */}
      {/* 공개/비공개 상태 표시 */}
      <div className="text-sm">{isPublic ? '공개' : '비공개'}</div>

      {/* <input
            id="inpLock"
            type="checkbox"
            className="hidden"
          /> */}
      {/* <label
            className="btn-lock"
            htmlFor="inpLock">
            <svg
              width="26"
              height="50"
              viewBox="0 -4 26.5 50">
              <path
                className="lockb"
                d="M27 27C27 34.1797 21.1797 40 14 40C6.8203 40 1 34.1797 1 27C1 19.8203 6.8203 14 14 14C21.1797 14 27 19.8203 27 27ZM15.6298 26.5191C16.4544 25.9845 17 25.056 17 24C17 22.3431 15.6569 21 14 21C12.3431 21 11 22.3431 11 24C11 25.056 11.5456 25.9845 12.3702 26.5191L11 32H17L15.6298 26.5191Z"></path>
              <path
                className="lock"
                d="M6 21V10C6 5.58172 9.58172 2 14 2V2C18.4183 2 22 5.58172 22 10V21"></path>
              <path
                className="bling"
                d="M29 20L31 22"></path>
              <path
                className="bling"
                d="M31.5 15H34.5"></path>
              <path
                className="bling"
                d="M29 10L31 8"></path>
            </svg>
          </label> */}
      {/* </div>
      </div> */}
    </div>
  );
};

export default DetailHeader;
