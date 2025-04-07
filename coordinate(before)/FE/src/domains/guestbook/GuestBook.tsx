import GuestBookList from '@/domains/guestbook/components/GuestBookList';
import { IoClose } from 'react-icons/io5';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { getMyGuestbookEntries } from '@/domains/guestbook/apis/apiUserGuestbook';
import {
  GuestbookOtherapi,
  PaginatedResponse,
} from '@/domains/guestbook/apis/apiOthersGuestBook';

import { useSelector } from 'react-redux';
import { RootState } from '@/stores/store';
import { putGuestbookIntroduction } from '@/domains/guestbook/apis/apiGuestbookIntroduction';
import { setIntro } from '@/stores/auth/authSlice';
import { useAppDispatch } from '@/hooks/hooks';
import ModalBase from '@/domains/diary/components/modalBase';

interface MainPageProps {
  onClose: () => void;
}

const GuestBook: React.FC<MainPageProps> = ({ onClose }) => {
  // 페이지 유저 번호
  const LoginUserNumber = useSelector(
    (state: RootState) => state.auth?.user?.userSeq
  );
  const PageUser = useSelector((state: RootState) => state.userpage);
  const PageUserNumber = useSelector(
    (state: RootState) => state.userpage.userSeq
  );
  const l = useSelector((state: RootState) => state);

  const dispatch = useAppDispatch();

  // 방명록 데이터를 저장할 상태
  const [guestbookEntries, setGuestbookEntries] =
    useState<PaginatedResponse | null>(null);
  // 현재 페이지 상태 추가
  const [currentPage, setCurrentPage] = useState<number>(1);
  // 방명록 입력 상태 추가
  const [newEntry, setNewEntry] = useState('');
  // 로딩 상태 추가
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 소개글 수정 관련 상태 추가
  const [isEditingIntro, setIsEditingIntro] = useState(false);
  const [introduction, setIntroduction] = useState('');
  const [isUpdatingIntro, setIsUpdatingIntro] = useState(false);

  // 입력 값 변경 핸들러
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setNewEntry(e.target.value);
  };

  // 방명록 데이터 가져오기 함수
  const fetchGuestbook = async (page: number = 1) => {
    if (!PageUserNumber) return;

    let data;
    if (PageUserNumber === LoginUserNumber) {
      data = await getMyGuestbookEntries(page);
    } else {
      data = await GuestbookOtherapi.getGuestbookEntries(PageUserNumber, page);
    }

    if (data) {
      setGuestbookEntries(data.data);
      // console.log('📜 엔트리 데이터', data.data);
    }
  };

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
    fetchGuestbook(page);
  };

  // 컴포넌트가 마운트될 때 데이터 가져오기
  useEffect(() => {
    if (PageUserNumber) {
      fetchGuestbook();
    }
  }, [PageUserNumber]);

  // 방명록 작성 제출 핸들러
  const handleSubmit = async () => {
    if (!newEntry.trim() || isSubmitting) return;

    try {
      setIsSubmitting(true);

      // PageUserNumber가 방명록 주인의 번호이므로 이 번호로 작성 API 호출
      const response = await GuestbookOtherapi.createGuestbookEntry(
        PageUserNumber,
        newEntry
      );

      if (response) {
        // console.log('✅ 방명록 작성 성공:', response);
        // 입력 필드 초기화
        setNewEntry('');
        // 방명록 목록 새로고침
        fetchGuestbook();
      }
    } catch (error) {
      // console.error('❌ 방명록 작성 실패:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  // 박명록 삭제 함수
  const handleDeleteEntry = async (guestbookSeq: number) => {
    try {
      const response =
        await GuestbookOtherapi.deleteGuestbookEntry(guestbookSeq);

      fetchGuestbook();
    } catch (error) {
      console.error('❌ 방명록 삭제 실패:', error);
    }
  };

  // 방명록 소개글 디폴트 소개
  const defaultIntroduction = `안녕하세요 ${PageUser.nickname}의 방명록입니다.`;

  // 소개글 초기화
  useEffect(() => {
    setIntroduction(PageUser.introduction || defaultIntroduction);
  }, [PageUser.introduction]);

  // 소개글 수정 핸들러
  const handleEditIntro = () => {
    setIntroduction('');
    setIsEditingIntro(true);
  };

  // 소개글 변경 핸들러
  const handleChangeIntro = (e: React.ChangeEvent<HTMLInputElement>) => {
    setIntroduction(e.target.value);
  };

  // 소개글 저장 핸들러
  const handleSaveIntro = async () => {
    setIsUpdatingIntro(true);
    try {
      const response = await putGuestbookIntroduction({ introduction });
      if (response) {
        console.log('✅ 소개글 수정 성공', response);
        setIsEditingIntro(false);
        dispatch(setIntro(introduction));
      }
    } catch (error) {
      console.error('❌ 소개글 수정 실패', error);
    } finally {
      setIsUpdatingIntro(false);
    }
  };

  const nav = useNavigate();
  const onClickHome = () => {
    nav('/');
  };

  return (
    <div className="absolute z-999 inset-0">
      {/* <MainPage /> */}
      <div className="absolute inset-0 backdrop-blur-lg"></div>
      {/* 모달 가장 바깥 부분 */}
      {/* <div className="absolute w-[60%] h-[75%] rounded-lg bg-[#6E6E6E]/75 top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2"></div> */}
      {/* 모달 콘텐츠 전체 */}
      <div className="absolute w-[60%] h-[75%] top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2">
        <ModalBase>
          {/* 닫기버튼 */}
          <div
            className="absolute z-40 top-[2%] right-[1%] cursor-pointer"
            onClick={onClose}>
            <IoClose className="text-gray-200 text-3xl hover:text-gray-400" />
          </div>
          <div className="flex flex-col p-10 gap-10 w-full">
            {/*헤더 부분 */}
            <div className="flex items-center justify-between ml-3">
              <div className="flex flex-col gap-1">
                <h1 className="text-white font-semibold text-xl">
                  {PageUser.nickname} 님의 방명록
                </h1>
                {/* 소개글 */}
                <div className="flex gap-7 justify-center text-sm">
                  {PageUserNumber === LoginUserNumber ? (
                    isEditingIntro ? (
                      <div className="flex flex-row w-full items-center gap-2">
                        <input
                          type="text"
                          value={introduction}
                          onChange={handleChangeIntro}
                          className="flex-grow p-1 border-b border-gray-300 text-gray-300 outline-none text-[15px]"
                        />
                        <button
                          className="bg-neutral-700 px-3 py-1 text-white rounded text-[13px] whitespace-nowrap cursor-pointer hover:bg-neutral-400"
                          onClick={handleSaveIntro}
                          disabled={isUpdatingIntro}>
                          저장
                        </button>
                      </div>
                    ) : (
                      <div className="flex flex-row w-full items-center gap-2">
                        <p className="flex-grow text-white font-light text-[15px]">
                          {introduction}
                        </p>
                        <button
                          className="bg-neutral-700 px-3 py-1 text-white rounded text-[13px] whitespace-nowrap cursor-pointer hover:bg-neutral-400"
                          onClick={handleEditIntro}>
                          수정
                        </button>
                      </div>
                    )
                  ) : (
                    <div className="flex flex-row w-full items-center gap-2">
                      <p className="flex-grow text-white font-light text-[15px]">
                        {introduction}
                      </p>
                    </div>
                  )}
                </div>
              </div>
              {/* 프로필 아이콘 */}
              <img
                src={PageUser.iconSeq}
                alt=""
                className="w-13 "
              />
            </div>

            {/*방명록 남기는 부분 */}
            <div className="flex gap-7 justify-center text-[15px]">
              <input
                type="text"
                placeholder="내용을 입력해 주세요."
                className="w-[90%] p-1 border-b border-gray-200 text-gray-200 outline-none"
                value={newEntry}
                onChange={handleInputChange}
                disabled={isSubmitting}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    handleSubmit();
                  }
                }}
              />
              <button
                className="bg-white/90 px-2 py-1 text-[#06061E] font-semibold rounded text-[16px] w-20 cursor-pointer hover:bg-[#cdcccc]"
                onClick={handleSubmit}
                disabled={isSubmitting}>
                남기기
              </button>
            </div>

            <GuestBookList
              data={
                guestbookEntries || {
                  guestbooks: [],
                  currentPage: 1,
                  totalPages: 1,
                  totalElements: 0,
                  last: false,
                }
              }
              onDelete={handleDeleteEntry}
              onPageChange={handlePageChange}
            />
          </div>
        </ModalBase>
      </div>
    </div>
  );
};

export default GuestBook;
