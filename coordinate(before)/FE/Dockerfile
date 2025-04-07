FROM node:18-alpine AS build
WORKDIR /app

# 의존성 파일 복사 및 설치
COPY package.json package-lock.json* ./
RUN npm ci

# 소스 파일 복사 및 빌드
COPY . .
RUN npm run build

# Nginx를 사용하여 정적 파일 제공
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]