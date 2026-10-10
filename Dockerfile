# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src ./src

RUN mvn -B -DskipTests package


# ---------- Extract Spring Boot layers ----------
FROM eclipse-temurin:21-jre-jammy AS extractor

WORKDIR /workspace

COPY --from=build /workspace/target/*.jar application.jar

RUN java \
    -Djarmode=tools \
    -jar application.jar \
    extract \
    --layers \
    --destination extracted


# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy AS runtime

WORKDIR /application

RUN groupadd --system commerce \
    && useradd \
       --system \
       --gid commerce \
       --home-dir /application \
       --shell /usr/sbin/nologin \
       commerce

COPY --from=extractor /workspace/extracted/dependencies/ ./
COPY --from=extractor /workspace/extracted/spring-boot-loader/ ./
COPY --from=extractor /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extractor /workspace/extracted/application/ ./

RUN chown -R commerce:commerce /application

USER commerce:commerce

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]