import api from '@/apis/apiClient';
import '../styles/DiarySearch.css';
import UserList from './UserList';
import { useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import React from 'react';

// User 인터페이스 정의
interface User {
  userSeq: number;
  subscribedSeq?: number;
  username: string;
  nickname: string;
  iconSeq: number;
  birthDate?: string;
  introduction?: string | null;
  isSubscribed?: boolean;
}

// 페이지네이션 응답 인터페이스 정의
interface PaginatedResponse {
  timestamp: string;
  status: number;
  message: string;
  data: {
    content: User[];
    currentPage: number;
    totalPages: number;
    totalElements: number;
    size: number;
    first: boolean;
    last: boolean;
  };
}

interface UserSearchProps {
  onClose: () => void;
}

const UserSearch: React.FC<UserSearchProps> = ({ onClose }) => {
  const navigate = useNavigate();
  const [paginatedData, setPaginatedData] = useState<PaginatedResponse>({
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
  const [searchText, setSearchText] = useState('');
  const [searchFilters, setSearchFilters] = useState({
    searchNickname: true,
    searchUsername: false,
  });
  const [friends, setFriends] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [filteredUsers, setFilteredUsers] = useState<User[]>([]);

  // 초기 로딩 시 친구 목록 가져오기
  useEffect(() => {
    const fetchFriends = async () => {
      try {
        const response = await api.get('/subscription');
        const friendData = response.data.data;
        console.log('내 친구 목록:', friendData);
        setFriends(friendData);
      } catch (error) {
        console.error('친구 목록 가져오기 오류:', error);
      }
    };
    fetchFriends();
  }, []);

  // 검색어가 없을 때는 내 친구 목록 표시
  useEffect(() => {
    if (!searchText.trim()) {
      setFilteredUsers(friends);
      setPaginatedData({
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
  }, [searchText, friends]);

  // 검색 함수: 페이지 번호를 인자로 받음
  const loadSearchData = async (page: number) => {
    if (!searchText.trim()) {
      setFilteredUsers(friends);
      return;
    }

    setIsLoading(true);
    try {
      // 검색 조건 적용
      const params = {
        keyword: searchText,
        page: page, // API는 1부터 시작하는 페이지 번호 사용
        size: 20, // 페이지당 아이템 수
      };

      // 쿼리 스트링으로 변환
      const queryString = Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
        .join('&');

      // /users/search 엔드포인트로 검색 요청
      const response = await api.get(`/users/search?${queryString}`);

      // 응답 데이터 확인 및 저장
      if (
        response.data &&
        response.data.data &&
        Array.isArray(response.data.data.content)
      ) {
        // 검색 결과에 친구 여부 표시 추가
        const usersWithSubscriptionStatus = response.data.data.content.map(
          (user: User) => {
            // 친구 목록에 있는지 확인해서 isSubscribed 필드 추가
            const isFriend = friends.some(
              (friend) =>
                friend.userSeq === user.userSeq ||
                friend.nickname === user.nickname
            );
            return { ...user, isSubscribed: isFriend };
          }
        );

        // 필터 적용
        const filtered = usersWithSubscriptionStatus.filter((user: User) => {
          const matchNickname =
            searchFilters.searchNickname &&
            user.nickname.toLowerCase().includes(searchText.toLowerCase());

          const matchUsername =
            searchFilters.searchUsername &&
            user.username.toLowerCase().includes(searchText.toLowerCase());

          return matchNickname || matchUsername;
        });

        // 응답 데이터 업데이트
        const updatedResponse = {
          ...response.data,
          data: {
            ...response.data.data,
            content: usersWithSubscriptionStatus,
          },
        };

        setPaginatedData(updatedResponse);
        setFilteredUsers(filtered);
      } else {
        console.error('응답 데이터가 예상된 형식이 아닙니다:', response);
        setPaginatedData({
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
        setFilteredUsers([]);
      }
    } catch (error) {
      console.error('사용자 검색 오류:', error);
      setPaginatedData({
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
      setFilteredUsers([]);
    } finally {
      setIsLoading(false);
    }
  };

  // 검색 실행
  const handleSearch = async () => {
    await loadSearchData(1); // 검색 시 첫 페이지로 리셋
  };

  // Enter 키로 검색 실행
  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // 페이지 변경 핸들러
  const handlePageChange = (newPage: number) => {
    if (
      newPage < 1 ||
      newPage > paginatedData.data.totalPages ||
      newPage === paginatedData.data.currentPage
    ) {
      return;
    }

    loadSearchData(newPage);
  };

  // 구독하기 핸들러
  const handleSubscribe = async (userSeq: number) => {
    try {
      await api.patch(`/subscription/${userSeq}`);

      // 구독 성공 후 검색 결과에서 해당 사용자 상태 업데이트
      const updatedContent = paginatedData.data.content.map((user) =>
        user.userSeq === userSeq ? { ...user, isSubscribed: true } : user
      );

      setPaginatedData({
        ...paginatedData,
        data: {
          ...paginatedData.data,
          content: updatedContent,
        },
      });

      // 필터링된 사용자도 업데이트
      const updatedFilteredUsers = filteredUsers.map((user) =>
        user.userSeq === userSeq ? { ...user, isSubscribed: true } : user
      );
      setFilteredUsers(updatedFilteredUsers);
    } catch (error) {
      console.error('구독 오류:', error);
    }
  };

  // 구독 취소 핸들러
  const handleUnsubscribe = async (subscribedSeq: number) => {
    try {
      await api.patch(`/subscription/${subscribedSeq}`);

      // 구독 취소 후 친구 목록에서 제거
      const updatedFriends = friends.filter(
        (friend) => friend.subscribedSeq !== subscribedSeq
      );
      setFriends(updatedFriends);

      // 현재 필터링된 목록도 업데이트
      const updatedFilteredUsers = filteredUsers.filter(
        (user) => user.subscribedSeq !== subscribedSeq
      );
      setFilteredUsers(updatedFilteredUsers);
    } catch (error) {
      console.error('구독 취소 오류:', error);
    }
  };

  // 놀러가기 핸들러
  const handleVisit = (userSeq: number) => {
    console.log('버튼 눌렸따.');

    const takeNickname = async () => {
      try {
        const response = await api.get(`/users/seq/${userSeq}`);
        const paramsNickname = response.data.data.nickname;
        onClose();
        navigate(`/${paramsNickname}`);
      } catch (error) {
        console.error(error, '닉네임 추출에 실패하였습니다.');
      }
    };

    // 함수 정의 후 실행
    takeNickname();
  };

  // 필터 변경 처리
  const handleFilterChange = (filter: string) => {
    setSearchFilters((prev) => ({
      ...prev,
      [filter]: !prev[filter],
    }));

    // 필터 변경 시 현재 검색 결과에 새 필터 적용
    if (paginatedData.data.content.length > 0) {
      const filtered = paginatedData.data.content.filter((user) => {
        const matchNickname =
          (filter === 'searchNickname'
            ? !searchFilters.searchNickname
            : searchFilters.searchNickname) &&
          user.nickname.toLowerCase().includes(searchText.toLowerCase());

        const matchUsername =
          (filter === 'searchUsername'
            ? !searchFilters.searchUsername
            : searchFilters.searchUsername) &&
          user.username.toLowerCase().includes(searchText.toLowerCase());

        return matchNickname || matchUsername;
      });

      setFilteredUsers(filtered);
    }
  };

  // 페이지 버튼 렌더링 함수
  const renderPagination = () => {
    const { totalPages, currentPage } = paginatedData.data;

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
      <div className="flex justify-center mt-4 mb-4 p-2 rounded z-10 relative">
        {pageButtons}
      </div>
    );
  };

  return (
    <div className="w-full flex flex-col gap-4 overflow-hidden">
      <div className="flex gap-5 w-full justify-center">
        <input
          type="text"
          placeholder="내용을 입력해 주세요"
          className="border-b border-white p-1 basis-63/100 placeholder:text-[#FFFFFF]/70 outline-none text-white"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          onKeyPress={handleKeyPress}
        />
        <button
          className="bg-[#545454] text-white rounded px-3 w-20 hover:bg-neutral-400 cursor-pointer"
          onClick={handleSearch}
          disabled={isLoading}>
          {isLoading ? '검색 중...' : '검색'}
        </button>
      </div>

      {/* 체크 박스 시작 */}
      <div className="flex gap-4 w-full justify-start pl-20 text-[15px]">
        <div className="flex gap-1.5 items-center">
          <label className="cont">
            <input
              type="checkbox"
              checked={searchFilters.searchNickname}
              onChange={() => handleFilterChange('searchNickname')}
              value="닉네임"
            />
            <span></span>
          </label>
          <p className="text-white">닉네임</p>
        </div>

        <div className="flex gap-1.5 items-center">
          <label className="cont">
            <input
              type="checkbox"
              checked={searchFilters.searchUsername}
              onChange={() => handleFilterChange('searchUsername')}
              value="아이디"
            />
            <span></span>
          </label>
          <p className="text-white">아이디</p>
        </div>
      </div>
      {/* 체크 박스 끝 */}

      {/* 검색 결과 */}
      <div className="w-full pr-4 overflow-y-scroll custom-scrollbar">
        {isLoading && filteredUsers.length === 0 ? (
          <div className="text-center text-white">검색 중...</div>
        ) : filteredUsers.length > 0 ? (
          <>
            <UserList
              data={filteredUsers}
              onSubscribe={handleSubscribe}
              onUnsubscribe={handleUnsubscribe}
              onVisit={handleVisit}
            />
            {searchText.trim() !== '' && (
              <div className="w-full block">{renderPagination()}</div>
            )}
          </>
        ) : (
          <div className="text-center text-white mt-4">
            {searchText.trim() !== '' ? '검색 결과가 없습니다.' : ''}
          </div>
        )}
      </div>
    </div>
  );
};

export default UserSearch;
