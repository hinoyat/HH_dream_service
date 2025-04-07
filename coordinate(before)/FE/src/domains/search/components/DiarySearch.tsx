import { useEffect, useState } from 'react';
import '../styles/DiarySearch.css';
import DiaryList from './DiaryList';
import api from '@/apis/apiClient';

interface DiarySearchProps {
  onClose: () => void;
}

// 페이지네이션이 포함된 API 응답 타입 정의
interface PaginatedResponse {
  timestamp: string;
  status: number;
  message: string;
  data: {
    content: any[];
    currentPage: number; // API 응답의 페이지 번호 (1부터 시작)
    totalPages: number;
    totalElements: number;
    size: number;
    first: boolean;
    last: boolean;
  };
}

const DiarySearch: React.FC<DiarySearchProps> = ({ onClose }) => {
  const [diaryData, setDiaryData] = useState<PaginatedResponse>({
    timestamp: '',
    status: 200,
    message: '',
    data: {
      content: [],
      currentPage: 1, // API는 1부터 시작하므로 초기값도 1로 설정
      totalPages: 1,
      totalElements: 0,
      size: 20,
      first: true,
      last: true,
    },
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [searchOptions, setSearchOptions] = useState({
    currentUserOnly: true,
    searchTitle: true,
    searchContent: true,
    searchTag: true,
  });
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // 컴포넌트 마운트 시 데이터 로드
    loadSearchData(1); // API 페이지는 1부터 시작
  }, []);

  const loadSearchData = async (page: number) => {
    setIsLoading(true);

    try {
      // 검색 조건 적용
      const params = {
        keyword: searchTerm,
        searchTitle: searchOptions.searchTitle,
        searchContent: searchOptions.searchContent,
        searchTag: searchOptions.searchTag,
        currentUserOnly: searchOptions.currentUserOnly,
        page: page, // API는 1부터 시작하는 페이지 번호 사용
        size: 20, // 페이지당 아이템 수
      };

      // 쿼리 스트링으로 변환
      const queryString = Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
        .join('&');

      const response = await api.get(`/diaries/search?${queryString}`);

      // 응답 구조 확인 및 데이터 저장
      if (
        response.data &&
        response.data.data &&
        Array.isArray(response.data.data.content)
      ) {
        setDiaryData(response.data);
      } else {
        console.error('응답 데이터가 예상된 형식이 아닙니다:', response);
        setDiaryData({
          timestamp: '',
          status: 200,
          message: '',
          data: {
            content: [],
            currentPage: 1,
            totalPages: 1,
            totalElements: 0,
            size: 20,
            first: true,
            last: true,
          },
        });
      }
    } catch (error) {
      console.error('목록을 불러오는데 에러가 발생하였습니다.', error);
      setDiaryData({
        timestamp: '',
        status: 500,
        message: '에러가 발생했습니다',
        data: {
          content: [],
          currentPage: 1,
          totalPages: 1,
          totalElements: 0,
          size: 20,
          first: true,
          last: true,
        },
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleSearch = async () => {
    await loadSearchData(1); // 검색 시 첫 페이지로 리셋
  };

  const handleCheckboxChange = (option) => {
    setSearchOptions((prev) => ({
      ...prev,
      [option]: !prev[option],
    }));
  };

  const handlePageChange = (newPage: number) => {
    if (newPage < 1 || newPage > diaryData.data.totalPages) {
      return;
    }

    loadSearchData(newPage);
  };

  // 페이지 버튼 렌더링 함수
  const renderPagination = () => {
    const { totalPages, currentPage } = diaryData.data;

    if (!totalPages || totalPages <= 1) {
      return null;
    }

    // 현재 페이지 주변 페이지 버튼만 표시 (최대 5개)
    const pageButtons = [];
    const startPage = Math.max(1, currentPage - 2);
    const endPage = Math.min(totalPages, startPage + 4);

    // 이전 페이지 버튼
    pageButtons.push(
      <button
        key="prev"
        onClick={() => handlePageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="px-3 py-1 text-white bg-[#545454] rounded mx-1 disabled:opacity-50 cursor-pointer hover:bg-[#808080]">
        &lt;
      </button>
    );

    // 페이지 번호 버튼
    for (let i = startPage; i <= endPage; i++) {
      pageButtons.push(
        <button
          key={i}
          onClick={() => handlePageChange(i)}
          className={`px-3 py-1 mx-1 rounded cursor-pointer ${
            i === currentPage
              ? 'bg-[#808080] text-white'
              : 'bg-[#545454] text-white hover:bg-[#808080]'
          }`}>
          {i}
        </button>
      );
    }

    // 다음 페이지 버튼
    pageButtons.push(
      <button
        key="next"
        onClick={() => handlePageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="px-3 py-1 text-white bg-[#545454] rounded mx-1 disabled:opacity-50 cursor-pointer hover:bg-[#808080]">
        &gt;
      </button>
    );

    return (
      <div className="flex justify-center mt-4 mb-4  p-2 rounded z-10 relative">
        {pageButtons}
      </div>
    );
  };

  return (
    <div className="w-full flex flex-col gap-4 overflow-hidden ">
      <div className="flex gap-5 w-full justify-center">
        <input
          type="text"
          placeholder="검색어를 입력해 주세요"
          className="text-[16px] border-b border-white p-1 basis-63/100 placeholder:text-[#FFFFFF]/70 text-white outline-none"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
        />
        <button
          className="bg-[#545454] text-white rounded px-3 w-20 text-base cursor-pointer hover:bg-neutral-400"
          onClick={handleSearch}>
          검색
        </button>
      </div>

      {/*체크 박스 시작 */}
      <div className="flex gap-4 w-full justify-start pl-20 text-[15px]">
        <div className="flex gap-1.5 items-center">
          <label className="cont">
            <input
              type="checkbox"
              checked={searchOptions.currentUserOnly}
              onChange={() => handleCheckboxChange('currentUserOnly')}
              value="현재 우주"
            />
            <span></span>
          </label>
          <p className="text-white">현재 우주</p>
        </div>

        <div className="flex gap-1.5 items-center">
          <label className="cont">
            <input
              type="checkbox"
              checked={searchOptions.searchTitle}
              onChange={() => handleCheckboxChange('searchTitle')}
              value="제목"
            />
            <span></span>
          </label>
          <p className="text-white">제목</p>
        </div>

        <div className="flex gap-1.5 items-center">
          <label className="cont">
            <input
              type="checkbox"
              checked={searchOptions.searchContent}
              onChange={() => handleCheckboxChange('searchContent')}
              value="내용"
            />
            <span></span>
          </label>
          <p className="text-white">내용</p>
        </div>

        <div className="flex gap-1.5 items-center">
          <label className="cont">
            <input
              type="checkbox"
              checked={searchOptions.searchTag}
              onChange={() => handleCheckboxChange('searchTag')}
              value="태그"
            />
            <span></span>
          </label>
          <p className="text-white">태그</p>
        </div>
      </div>
      {/*체크 박스 끝 */}

      {/* 검색 결과 보이는 부분 */}
      <div className="w-full overflow-y-auto pr-4 custom-scrollbar">
        {isLoading ? (
          <div className="flex justify-center items-center h-full">
            <p className="text-white">로딩 중...</p>
          </div>
        ) : diaryData.data && diaryData.data.content.length > 0 ? (
          <>
            <DiaryList
              data={{
                ...diaryData,
                data: diaryData.data.content, // DiaryList에 content 배열만 전달
              }}
            />
            <div className="w-full block">{renderPagination()}</div>
          </>
        ) : (
          <div className="flex justify-center items-center h-full">
            <p className="text-white">검색 결과가 없습니다.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default DiarySearch;
