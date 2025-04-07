import React, { ChangeEvent } from 'react';

interface DiaryInputProps {
  title?: string; // 제목 초기값
  content?: string; // 내용 초기값
  onTitleChange?: (title: string) => void; // 제목 변경 핸들러
  onContentChange?: (content: string) => void; // 내용 변경 핸들러
}

const DiaryInput: React.FC<DiaryInputProps> = ({
  title = '',
  content = '',
  onTitleChange,
  onContentChange,
}) => {
  // 제목 변경 핸들러
  const handleTitleChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (onTitleChange) {
      onTitleChange(e.target.value);
    }
  };

  // 내용 변경 핸들러
  const handleContentChange = (e: ChangeEvent<HTMLTextAreaElement>) => {
    // 250자 제한
    const newContent = e.target.value.slice(0, 250);

    if (onContentChange) {
      onContentChange(newContent);
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <input
        type="text"
        placeholder="제목을 입력해주세요"
        className="border-b border-white p-2 text-white placeholder-[#FFFFFF]/70 text-[17px] outline-none"
        value={title}
        onChange={handleTitleChange}
      />
      <textarea
        placeholder="내용을 작성해주세요"
        className="p-3 border border-white rounded-md focus:border-2 placeholder-[#FFFFFF]/70 text-[15px] h-60 text-[#FFFFFF]/90 w-full resize-none  outline-none"
        value={content}
        onChange={handleContentChange}
      />
      <div className="text-right text-white/70 text-sm">
        {content.length}/250
      </div>
    </div>
  );
};

export default DiaryInput;
