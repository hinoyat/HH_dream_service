// 일기 미리보기 컴포넌트

interface Tag {
  tagSeq: number;
  name: string;
}

interface DiaryPreviewProps {
  title: string;
  content: string;
  tags: Tag[];
  emotion: string;
}

const DiaryPreview = ({
  title,
  content,
  tags = [],
  emotion,
}: DiaryPreviewProps) => {
  // 내용이 길 경우 자르기 (미리보기에서는 일부만 표시)
  const truncatedContent =
    content && content.length > 50
      ? content.substring(0, 50) + '...'
      : content || '';

  return (
    <div className="relative w-[420px] h-[150px] bg-white/21 bg-opacity-20 backdrop-blur-sm rounded-lg p-4 flex">
      <div className="flex flex-col basis-9/10">
        <h2 className="text-base font-bold text-white px-3 ">{title}</h2>
        <p className="text-xs text-white mt-2 line-clamp px-3">
          {truncatedContent}
        </p>
      </div>

      {/* 감정 표시 */}
      <div className=" text-xs text-white bg-[#D9D9D9]/29 px-2 py-1 rounded-[7px] basis-1/10 h-[21%] ">
        {/* {console.log('✅렌더링되는 감정:', emotion)} */}
        {emotion}
      </div>

      <div className="absolute bottom-4 left-7 flex space-x-2">
        {tags.map((tag) => (
          <div
            key={tag.tagSeq}
            className="px-2 py-1 bg-[#D9D9D9]/29 bg-opacity-30 backdrop-blur-sm rounded-[7px] text-xs text-white">
            {tag.name}
          </div>
        ))}
      </div>
    </div>
  );
};

export default DiaryPreview;
