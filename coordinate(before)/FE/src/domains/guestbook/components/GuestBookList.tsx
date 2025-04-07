import { useEffect, useState } from 'react';
import { Guestbookdata, PaginatedResponse } from '../apis/apiOthersGuestBook';
import trash from '@/assets/images/trash.svg';
import { useSelector } from 'react-redux';
import { RootState } from '@/stores/store';
import { getIconById } from '@/hooks/ProfileIcons';

// props로 받아온 타입지정
interface GuestBookListProps {
  data?: PaginatedResponse;
  onDelete: (id: number) => void;
  onPageChange: (page: number) => void;
}

const GuestBookList: React.FC<GuestBookListProps> = ({
  data,
  onDelete,
  onPageChange,
}) => {
  const LoginUserNumber = useSelector(
    (state: RootState) => state.auth?.user?.userSeq
  );

  // props로 받아온 데이터
  const guestbooksData = data?.guestbooks || []; // 방명록 데이터
  const totalPages = data?.totalPages || 1; // 전체 페이지
  const totalElements = data?.totalElements || 0; // 전체 방명록 글 개수
  const isLastPage = data?.last || false; // 마지막 페이지 여부

  // 현재 페이지 상태 관리
  const [currentPage, setCurrentPage] = useState<number>(
    data?.currentPage || 1
  );

  // 데이터가 변경될 때 현재 페이지 업데이트
  useEffect(() => {
    if (data?.currentPage) {
      setCurrentPage(data.currentPage);
    }
  }, [data]);

  // 페이지 변경
  const handlePageChange = (pageNumber: number) => {
    setCurrentPage(pageNumber);
    onPageChange(pageNumber);
  };

  // 이전 페이지
  const handlePrevPage = () => {
    const newPage = Math.max(currentPage - 1, 1);
    setCurrentPage(newPage);
    onPageChange(newPage);
  };

  // 다음 페이지
  const handleNextPage = () => {
    const newPage = Math.min(currentPage + 1, totalPages);
    setCurrentPage(newPage);
    onPageChange(newPage);
  };

  // 페에지 최대 표시 번호

  return (
    <div className="w-full text-white px-3">
      {/* 내용 부분 */}
      {guestbooksData.map((item) => (
        <div
          key={item.guestbookSeq}
          className="flex items-center gap-4 p-2 w-full text-[14px]">
          {/* 남긴 글 */}
          <p className="w-[78%] truncate text-[15px] mr-2">{item.content}</p>
          {/* 프로필과 닉네임 영역 */}
          <div className="flex items-center gap-2 w-32">
            <img
              src={getIconById(item.writerIconSeq)}
              alt="profile"
              className="w-6 h-7 object-contain"
            />
            <p className="truncate">{item.writerNickname}</p>
          </div>
          {/* 작성일 영역 */}
          <div className="flex gap-3">
            <p className="w-24">
              {item.createdAt
                ? `${item.createdAt.slice(0, 4)}-${item.createdAt.slice(4, 6)}-${item.createdAt.slice(6, 8)}`
                : item.createdAt}
            </p>
            <div className="w-8 text-center">
              {LoginUserNumber === item.writerSeq ? (
                <img
                  src={trash}
                  className="w-3.5 hover:animate-pulse cursor-pointer"
                  onClick={() => onDelete && onDelete(item.guestbookSeq)}
                />
              ) : (
                ''
              )}
            </div>
          </div>
        </div>
      ))}
      {/* 페이지네이션 영역 */}
      <div className="flex items-center gap-3 justify-center absolute bottom-12 left-1/2 transform -translate-x-1/2">
        {/* 이전 페이지 버튼 */}
        <button
          onClick={handlePrevPage}
          disabled={currentPage === 1}
          className="text-[#FBFBFB] text-xl disabled:opacity-50 cursor-pointer">
          &laquo;
        </button>

        {/* 페이지 번호들 */}
        {(() => {
          // 표시할 페이지 번호 계산 로직
          const maxButtons = 5; // 최대 표시할 버튼 수
          let startPage = Math.max(1, currentPage - Math.floor(maxButtons / 2));
          let endPage = Math.min(totalPages, startPage + maxButtons - 1);

          // startPage 조정 (endPage가 totalPages에 도달하면 startPage를 왼쪽으로 이동)
          if (endPage === totalPages) {
            startPage = Math.max(1, endPage - maxButtons + 1);
          }

          // 페이지 버튼 배열 생성
          const pages = [];
          for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
          }

          return pages.map((page) => (
            <button
              key={page}
              onClick={() => handlePageChange(page)}
              className={`w-8 h-8 rounded-md flex items-center justify-center text-black text-sm cursor-pointer  
              ${page === currentPage ? 'bg-[#fbfbfb91] hover:bg-[#c8c7c791]' : 'bg-[#FBFBFB]  hover:bg-[#b1b1b1]'} `}>
              {page}
            </button>
          ));
        })()}

        {/* 다음 페이지 버튼 */}
        <button
          onClick={handleNextPage}
          disabled={currentPage === totalPages || isLastPage}
          className="text-[#FBFBFB] text-xl disabled:opacity-50 cursor-pointer">
          &raquo;
        </button>
      </div>
    </div>
  );
};

export default GuestBookList;
