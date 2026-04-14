# 1. Sử dụng phiên bản Node.js nhẹ (alpine) để tiết kiệm dung lượng
FROM node:20-alpine

# 2. Set thư mục làm việc bên trong container
WORKDIR /usr/src/app

# 3. Copy package.json vào trước để tận dụng cache của Docker
COPY package*.json ./

# 4. Cài đặt các thư viện (chỉ cài môi trường Production, bỏ qua thư viện Dev)
RUN npm install --production

# 5. Copy toàn bộ source code còn lại vào container (sẽ tự động bỏ qua các file trong .dockerignore)
COPY . .

# 6. Tạo thư mục uploads bên trong container nếu chưa có
RUN mkdir -p uploads

# 7. Mở port 3000 để giao tiếp với bên ngoài
EXPOSE 3000

# 8. Mặc định container sẽ chạy file server.js
CMD ["node", "server.js"]