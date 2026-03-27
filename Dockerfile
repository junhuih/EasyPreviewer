FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm@sha256:0af991049cded4b8d5a90d2614289ceec390779ad6a3e4b70957e2b4f3bb1230

RUN rm -f /etc/apt/sources.list.d/yarn.list /etc/apt/sources.list.d/yarn.sources \
    && rm -f /etc/apt/sources.list.d/debian.sources \
    && printf '%s\n' \
        'deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm main' \
        'deb https://mirrors.tuna.tsinghua.edu.cn/debian/ bookworm-updates main' \
        'deb https://mirrors.tuna.tsinghua.edu.cn/debian-security bookworm-security main' \
        > /etc/apt/sources.list \
    && apt-get -o Acquire::Retries=10 update \
    && DEBIAN_FRONTEND=noninteractive apt-get -o Acquire::Retries=10 install -y --fix-missing --no-install-recommends \
        libreoffice-writer \
        libreoffice-calc \
        libreoffice-impress \
        libreoffice-java-common \
        fonts-noto-cjk \
        fonts-dejavu-core \
        curl \
    && rm -rf /var/lib/apt/lists/*

ENV OFFICE_HOME=/usr/lib/libreoffice
ENV JAVA_OPTS=""

WORKDIR /app
COPY backend/target/preview-backend-0.1.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8080/ > /dev/null || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
