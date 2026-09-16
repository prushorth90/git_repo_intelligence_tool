FROM node:22-alpine AS build
WORKDIR /workspace
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend ./
RUN npm run build

FROM nginx:1.27-alpine
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html
EXPOSE 80
