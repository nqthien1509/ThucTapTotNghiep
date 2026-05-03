# 1. Sử dụng phiên bản Node.js nhẹ (alpine) để tiết kiệm dung lượng
FROM node:20-alpine

# =========================================================================
# [CẬP NHẬT QUAN TRỌNG]: Cài đặt các thư viện lõi hệ thống cho thư viện pdf2pic
# Thư viện pdf2pic cần Ghostscript và GraphicsMagick để đọc và cắt file PDF.
# Lệnh apk add là trình quản lý gói của hệ điều hành Alpine Linux.
# =========================================================================
RUN apk update && \
    apk add --no-cache \
    graphicsmagick \
    ghostscript

# 2. Set thư mục làm việc bên trong container
WORKDIR /usr/src/app

# 3. Copy file package.json và package-lock.json vào trước để tận dụng cache của Docker
COPY package*.json ./

# 4. Cài đặt các thư viện Node.js (chỉ cài môi trường Production, bỏ qua thư viện Dev)
RUN npm install --production

# 5. Copy toàn bộ source code còn lại vào container (sẽ tự động bỏ qua các file trong .dockerignore)
COPY . .

# 6. Tạo thư mục uploads và thư mục con thumbnails bên trong container
# Bước này cực kỳ quan trọng để đảm bảo quyền ghi file (Write Permission) khi ứng dụng chạy
RUN mkdir -p uploads/thumbnails

# Phân quyền cho thư mục uploads để Node.js có thể ghi file vào đó
RUN chmod -R 777 uploads

# 7. Mở port 3000 để giao tiếp với bên ngoài (cho API)
EXPOSE 3000

# 8. Mặc định container sẽ chạy file server.js
CMD ["node", "server.js"]