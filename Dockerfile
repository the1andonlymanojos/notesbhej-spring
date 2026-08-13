FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1. copy only pom (cache deps)
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -e -DskipTests dependency:go-offline

# 2. copy source separately
COPY src ./src

# 3. build (no clean)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -DskipTests package

# ---- runtime image ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
