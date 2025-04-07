// 수정하기 버튼

import ButtonBase from '@/domains/diary/components/details/button/ButtonBase';

interface UpdateButtonProps {
  onEdit?: () => void;
  isMySpace?: boolean;
}

const UpdateButton: React.FC<UpdateButtonProps> = ({
  onEdit,
  isMySpace = false,
}) => {
  // 내 우주가 아니면 표시 안함
  if (!isMySpace) {
    return null;
  }

  return (
    <ButtonBase
      onClick={onEdit}
      className="text-[15px]"
      width="100px">
      수정하기
    </ButtonBase>
  );
};

export default UpdateButton;
