# 阶段一：构建
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY pom.xml .
# 预先下载依赖（利用缓存层）
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# 阶段二：运行
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=builder /app/target/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
