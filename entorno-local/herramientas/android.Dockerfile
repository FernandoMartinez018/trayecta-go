# Compila la app Android dentro de Docker. Solo amd64: build-tools y aapt2 no tienen binarios arm64 confiables.
FROM --platform=linux/amd64 eclipse-temurin:21.0.13_9-jdk-noble

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:/opt/android-sdk/cmdline-tools/latest/bin

ARG CLT=commandlinetools-linux-15859902_latest.zip
ARG CLT_SHA256=4e4c464f145a7512b57d088ac6c278c03c9eea610886b35a5e0804e74eedf583

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl unzip \
 && rm -rf /var/lib/apt/lists/*

RUN curl -fsSLo /tmp/clt.zip "https://dl.google.com/android/repository/${CLT}" \
 && echo "${CLT_SHA256}  /tmp/clt.zip" | sha256sum -c - \
 && mkdir -p "${ANDROID_HOME}/cmdline-tools" \
 && unzip -q /tmp/clt.zip -d /tmp/clt \
 && mv /tmp/clt/cmdline-tools "${ANDROID_HOME}/cmdline-tools/latest" \
 && rm -rf /tmp/clt /tmp/clt.zip

# Construir esta imagen acepta las licencias del Android SDK.
RUN yes | sdkmanager --licenses > /dev/null \
 && sdkmanager "platforms;android-37.0" "build-tools;36.0.0"
