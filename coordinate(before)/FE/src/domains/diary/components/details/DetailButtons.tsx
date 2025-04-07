// // 조회 쪽 버튼

// import React from 'react';

// interface DetailButtonsProps {
//   onEdit?: () => void;
//   onClose?: () => void;
//   onDelete?: () => void;
//   isMySpace?: boolean;
// }

// const DetailButtons: React.FC<DetailButtonsProps> = ({
//   onEdit,
//   onClose,
//   onDelete,
//   isMySpace = false, // 기본은 false. 내 우주일때만 버튼 활성화
// }) => {
//   // 수정 버튼 클릭 핸들러
//   const handleEditClick = () => {
//     if (onEdit) {
//       onEdit();
//     }
//   };

//   // 삭제 버튼 클릭 핸들러
//   const handleDeleteClick = () => {
//     console.log('삭제 버튼 직접 클릭');
//     if (onDelete) {
//       if (window.confirm('정말로 이 일기를 삭제하시겠습니까?')) {
//         onDelete();
//       }
//     }
//   };

//   // 꿈 해몽 핸들러
//   const handleDreamInterpretation = () => {
//     console.log('꿈 해몽 페이지로 이동');
//     // 해몽 페이지로 이동하는 로직 추가
//   };

//   if (!isMySpace) {
//     return <div></div>; // 버튼 없이 공간만
//   }

//   return (
//     <div className="flex w-full flex-col gap-3 ">
//       <button
//         className="text-white/90 mt-4 cursor-pointer w-full bg-[#323232]/90 hover:bg-[#282828]/90 py-2 rounded text-sm font-bold"
//         onClick={handleDreamInterpretation}>
//         꿈 해몽 하러가기
//       </button>

//       <button
//         className="text-white/90 mb-6 cursor-pointer w-full bg-[#858484]/90 hover:bg-[#707070]/90 py-2 rounded text-sm font-bold"
//         onClick={handleEditClick}>
//         수정하기
//       </button>

//       <button
//         className="text-white/90 mb-6 cursor-pointer w-full bg-[#ff5757]/90 hover:bg-[#e74c4c]/90 py-2 rounded text-sm font-bold"
//         onClick={handleDeleteClick}>
//         삭제하기
//       </button>
//     </div>
//   );
// };

// export default DetailButtons;
