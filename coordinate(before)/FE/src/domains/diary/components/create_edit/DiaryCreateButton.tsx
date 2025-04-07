// 일기 생성/ 수정 버튼

import RedButtonBase from '@/domains/diary/components/create_edit/RedButtonBase';
import ButtonBase from '@/domains/diary/components/details/button/ButtonBase';
import React from 'react';

interface DiaryCreateButtonProps {
  onCreate?: () => void;
  isEditing: boolean;
  onDelete?: () => void;
}

const DiaryCreateButton: React.FC<DiaryCreateButtonProps> = ({
  onCreate,
  isEditing = false,
  onDelete,
}) => {
  return (
    <div className="w-full flex flex-col gap-3">
      {isEditing ? (
        <>
          <ButtonBase
            onClick={onCreate}
            borderRadius="6px">
            수정완료
          </ButtonBase>

          {onDelete && (
            <RedButtonBase
              onClick={() => {
                if (window.confirm('정말로 이 일기를 삭제하시겠습니까?')) {
                  onDelete();
                }
              }}
              borderRadius="6px">
              삭제하기
            </RedButtonBase>
          )}
        </>
      ) : (
        <ButtonBase
          onClick={onCreate}
          borderRadius="6px"
          height="46px">
          꿈 일기 등록하기
        </ButtonBase>
      )}
    </div>
  );
};

export default DiaryCreateButton;
