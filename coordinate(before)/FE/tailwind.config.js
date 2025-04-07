/** @type {import('tailwindcss').Config} */
import defaultTheme from 'tailwindcss/defaultTheme';

// 아래 extend는 tailwind 커스텀 설정입니다. 예전에 했던 거 참고하려고 그대로 올리니
// 실제로 사용할 때는 수정이 필요합니다
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      fontFamily: {
        pretendard: ['Pretendard Variable', 'sans-serif'],
      },
      screens: {
        tall: { raw: '(min-height: 768px)' },
      },
      colors: {
        main: {
          background: '#FFF7E1',
          beige: '#ECCDB4',
          btn: '#FFDEA9',
          carrot: '#FF8462',
          choose: '#E86161',
          pink: '#FFEDED',
          point: '#FFC8A4',
          point2: '#FFBF78',
          strawberry: '#FFB0B0',
          success: '#A0EE6F',
          kakao: '#FEE500',
          authInput: '#F6F3EA',
          authWrong: '#F6EAEA',
        },
        text: {
          first: '#222222',
          second: '#515151',
          third: '#B1B1B1',
          white: '#FFFFFF',
        },
        gray: {
          50: '#FFFFFF',
          100: '#E7E7E7',
          200: '#CFCFCF',
          300: '#9F9F9F',
          400: '#787878',
          500: '#4B4B4B',
          600: '#000000',
        },
      },
    },
  },
  plugins: [],
};
