// 별에 호버했을 때 메뉴 세 개 뜨는 거 컴포넌트
// 였는데 별을 '클릭' 했을 때 메뉴 세 개 뜨는 컴포넌트

import React, { useState, useRef, useEffect } from 'react';

//          이미지 import          //
import hover_delete from '@/assets/main/hover-delete.svg';
import hover_diary from '@/assets/main/hover-diary.svg';
import hover_edit from '@/assets/main/hover-edit.svg';

interface StarMenuProps {
  position?: { x: number; y: number };
  onEdit?: () => void;
  onDelete?: () => void;
  onView?: () => void;
}

const StarHoverMenu = ({
  position,
  onEdit,
  onDelete,
  onView,
}: StarMenuProps) => {
  // 별을 클릭해서 메뉴가 나타나기 때문에 isVisible를 true로 시작
  const [isVisible, setIsVisible] = useState(true);
  const [hoveredButton, setHoveredButton] = useState<string | null>(null);
  const menuContainerRef = useRef<HTMLDivElement>(null);

  // 메뉴 토글 및 마우스 이동 영영역 계산
  const [safePath, setSafePath] = useState(false);

  // 부모 컴포넌트에서 메뉴를 다시 표시할 수 있도록 prop 추가
  useEffect(() => {
    setIsVisible(true);
  }, [position]); // position이 변경될 때마다 메뉴 다시 표시

  // 별에서 메뉴 컨테이너로 마우스가 이동할 수 있는 안전 경로 설정
  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      const menuRect = menuContainerRef.current?.getBoundingClientRect();
      if (!menuRect) return;

      // 메뉴 주변의 안전 영역 정의 (버튼들 포함)
      const safeArea = {
        left: menuRect.left - 40,
        right: menuRect.right - 140,
        top: menuRect.top - 40,
        bottom: menuRect.bottom - 110,
      };

      // 마우스가 안전 영역 안에 있는지 확인
      const isInSafeArea =
        e.clientX >= safeArea.left &&
        e.clientX <= safeArea.right &&
        e.clientY >= safeArea.top &&
        e.clientY <= safeArea.bottom;

      if (!isInSafeArea) {
        setIsVisible(false);
      }
    };

    // 메뉴가 보이는 상태일 때만 이벤트 리스너 추가
    if (isVisible) {
      document.addEventListener('mousemove', handleMouseMove);
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
    };
  }, [isVisible]); // isVisible 상태에 따라 이펙트 재실행

  //          버튼위치          //
  const buttonLayout = [
    { top: '5px', left: '-42px' }, // 편집 버튼 위치 (좌측)
    { top: '-15px', left: '-12px' }, // 삭제 버튼 위치 (중앙)
    { top: '5px', left: '18px' }, // 일기보기 버튼 위치 (우측)
  ];

  //          툴팁관련          //
  const tooltipLayout = [
    { top: '-20px', left: '-45px' }, // 편집 버튼 툴팁
    { top: '-40px', left: '-12px' }, // 삭제 버튼 툴팁
    { top: '-20px', left: '21px' }, // 일기보기 버튼 툴팁
  ];

  return (
    <div className="relative inline-block">
      {isVisible && (
        <div
          ref={menuContainerRef}
          className="absolute z-10"
          style={{
            left: '0px',
            top: '0px',
            width: '200px',
            height: '150px', // 영역 확장
            pointerEvents: 'auto', // 이벤트 캡처 허용
          }}>
          {/* 별과 메뉴 사이 안전 영역 () 확인하려면 background 색상 변경해서 보기*/}
          {/* <div
            className="absolute"
            style={{
              left: '-40px',
              top: '-40px',
              right: '140px',
              bottom: '130px',
              background: 'yellow',
            }}
          /> */}

          {/* ------------------------툴팁들------------------------ */}
          {/* 툴팁 */}
          {hoveredButton === 'edit' && (
            <div
              className="absolute text-white/90 text-xs px-2 py-1 rounded whitespace-nowrap z-20"
              style={{
                ...tooltipLayout[0],
                transform: 'translateX(-25%)',
              }}>
              수정하기
            </div>
          )}

          {hoveredButton === 'delete' && (
            <div
              className="absolute text-white/90 text-xs px-2 py-1 rounded whitespace-nowrap z-20"
              style={{
                ...tooltipLayout[1],
                transform: 'translateX(-25%)',
              }}>
              삭제하기
            </div>
          )}

          {hoveredButton === 'view' && (
            <div
              className="absolute text-white/90 text-xs px-2 py-1 rounded whitespace-nowrap z-20"
              style={{
                ...tooltipLayout[2],
                transform: 'translateX(-25%)',
              }}>
              일기보기
            </div>
          )}
          {/* ------------------------버튼들------------------------ */}
          {/* 편집 버튼 - 좌측 */}
          <div
            className="absolute"
            style={{
              ...buttonLayout[0],
              transition: 'opacity 0.2s, transform 0.2s',
              opacity: isVisible ? 0.5 : 0,
              transform: `translateY(${isVisible ? '0' : '10px'})`,
            }}>
            <button
              onClick={onEdit}
              onMouseEnter={() => setHoveredButton('edit')}
              onMouseLeave={() => setHoveredButton(null)}
              className="w-7 h-7 bg-gray-700 rounded-full flex items-center justify-center transition-transform hover:scale-110 group relative">
              {/* 원 부분: 원 자체는 항상 반투명 */}
              <div className="w-full h-full bg-gray-700 rounded-full opacity-50 absolute top-0 left-0"></div>

              {/* 아이콘 부분: 호버 시에만 아이콘 투명도 변경 */}
              <img
                src={hover_edit}
                alt="edit"
                className="w-5 h-5 opacity-90 transition-opacity group-hover:opacity-100 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"
              />
            </button>
          </div>
          {/* 삭제 버튼 - 중앙 */}
          <div
            className="absolute"
            style={{
              ...buttonLayout[1],
              transition: 'opacity 0.3s, transform 0.3s',
              opacity: isVisible ? 0.5 : 0,
              transform: `translateY(${isVisible ? '0' : '10px'})`,
              transitionDelay: '0.05s',
            }}>
            <button
              onClick={onDelete}
              onMouseEnter={() => setHoveredButton('delete')}
              onMouseLeave={() => setHoveredButton(null)}
              className="w-7 h-7 bg-gray-700 rounded-full flex items-center justify-center transition-transform hover:scale-110 group relative">
              {/* 원 부분: 원 자체는 항상 반투명 */}
              <div className="w-full h-full bg-gray-700 rounded-full opacity-50 absolute top-0 left-0"></div>

              {/* 아이콘 부분: 호버 시에만 아이콘 투명도 변경 */}
              <img
                src={hover_delete}
                alt="delete"
                className="w-4 h-4 opacity-80 transition-opacity group-hover:opacity-100 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"
              />
            </button>
          </div>
          {/* 일기보기 버튼 - 우측 */}
          <div
            className="absolute"
            style={{
              ...buttonLayout[2],
              transition: 'opacity 0.4s, transform 0.4s',
              opacity: isVisible ? 0.5 : 0,
              transform: `translateY(${isVisible ? '0' : '10px'})`,
              transitionDelay: '0.1s',
            }}>
            <button
              onClick={onView}
              onMouseEnter={() => setHoveredButton('view')}
              onMouseLeave={() => setHoveredButton(null)}
              className="w-7 h-7 bg-gray-700 rounded-full flex items-center justify-center transition-transform hover:scale-110 group relative">
              {/* 원 부분: 원 자체는 항상 반투명 */}
              <div className="w-full h-full bg-gray-700 rounded-full opacity-50 absolute top-0 left-0"></div>

              {/* 아이콘 부분: 호버 시에만 아이콘 투명도 변경 */}
              <img
                src={hover_diary}
                alt="view"
                className="w-5 h-5 opacity-90 transition-opacity group-hover:opacity-100 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"
              />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default StarHoverMenu;
