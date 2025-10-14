# Sử dụng base image có JDK 25
FROM openjdk:25-jdk-slim

# Đặt thư mục làm việc trong container
WORKDIR /app

# Copy file .jar từ máy host vào container
COPY target/*.jar app.jar

# Expose port 8080 để có thể truy cập từ ngoài
EXPOSE 8080

# Lệnh chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "/app/app.jar"]