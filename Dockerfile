# 使用官方 Maven 镜像构建应用
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 使用官方 Java 运行环境镜像作为运行容器
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /build/target/ecommerce-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
