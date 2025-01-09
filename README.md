# Cách 1. Dùng image did(docker in docker) để có thể dùng docker-cli trong jenkins docker
**Cách này tạp vì mỗi lần build bạn phải expose cổng từ docker in docker: `jenkins-docker` container ra để truy cập vào**

<br><br>

Tạo file `Dockerfile`
```shell
nano Dockerfile
```
<br><br>


Dán nội dung bên dưới `Ctrl + V` và lưu lại `Ctrl + X` -> Nhấn `Y`


```Dockerfile
FROM jenkins/jenkins:lts-jdk21
USER root
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) \
signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
https://download.docker.com/linux/debian \
$(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli
USER jenkins
RUN jenkins-plugin-cli --plugins "blueocean docker-workflow"
```


<br><br>


Xây dựng image jenkins với tên `jenkins-app:lts-jdk21`
```shell
docker build -t jenkins-app:lts-jdk21 .
```

<br><br>


Tạo network trên docker với tên là `jenkins`

```shell
docker network create jenkins
```

<br><br>


Chạy docker jenkins custom

```shell
docker run \
--name jenkins-app \
--restart=on-failure \
--detach \
--network jenkins \
--env DOCKER_HOST=tcp://docker:2376 \
--env DOCKER_CERT_PATH=/certs/client \
--env DOCKER_TLS_VERIFY=1 \
--publish 8080:8080 \
--publish 50000:50000 \
--volume jenkins_home:/var/jenkins_home \
--volume jenkins_docker_certs:/certs/client:ro \
jenkins-app:lts-jdk21
```


<br><br>


Chạy did(docker in docker) để jenkins có thể sử dụng
<br>
`-p 8888:8888`: port app build ra bởi jenkins có thể thay đổi (Có thể thêm nhiều port khác)
`--publish 2376:2376`: là port cho did và jenkins liên lạc
`--volume jenkins_home:/var/jenkins_home`: Nơi lưu trữ lúc gán cho container `jenkins-app`



```shell
docker run \
--name jenkins-docker \
--detach \
--privileged \
--network jenkins \
--network-alias docker \
--env DOCKER_TLS_CERTDIR=/certs \
--volume jenkins_docker_certs:/certs/client \
--volume jenkins_home:/var/jenkins_home \
-p 8888:8888 \
--publish 2376:2376 \
docker:dind \
--storage-driver overlay2
```

<br><br><br><br><br>


---
# 2. Cách 2: Tự xây dựng image jenkins

Tạo file `Dockerfile` để xây dựng image jenkins tùy chỉnh có các 
```shell
cd && mkdir jenkins-custom && cd jenkins-custom && nano Dockerfile
```


<br><br>



Dán nội dung bên dưới `Ctrl + V` và lưu lại `Ctrl + X` -> Nhấn `Y`

```Dockerfile
# Dùng jenkins lts jdk21
FROM jenkins/jenkins:lts-jdk21

# Chuyển sang root cài đặt package
USER root

# Cài đặt các package cần thiết cho docker
RUN apt-get update && apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    software-properties-common \
    unzip \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Cài đặt Docker
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg && \
    echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian \
    $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce docker-ce-cli containerd.io && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Cài đặt Docker Compose
RUN curl -L "https://github.com/docker/compose/releases/download/v2.24.1/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose \
    && chmod +x /usr/local/bin/docker-compose

# Cài đặt Gradle
RUN curl -L "https://services.gradle.org/distributions/gradle-8.12-bin.zip" -o gradle.zip \
    && unzip gradle.zip \
    && mv gradle-8.12 /opt/gradle \
    && rm gradle.zip
ENV PATH=$PATH:/opt/gradle/bin

# Thêm user:jenkins vào group:docker để có thể sử dụng Docker
RUN groupadd docker || true && \
    usermod -aG docker jenkins

# Tải trước các gói vào images
RUN echo '#!/bin/bash\n\
# Pull required Docker images\n\
docker pull eclipse-temurin:21-jdk\n\
docker pull eclipse-temurin:21-jre\n\
docker pull redis/redis-stack:latest\n\
docker pull postgres:17.0-bookworm\n\
docker pull minio/minio:latest\n\
\n\
# Start Jenkins\n\
exec /usr/local/bin/jenkins.sh' > /usr/local/bin/init.sh && \
    chmod +x /usr/local/bin/init.sh

# Chuyển về user:jenkins
USER jenkins

# Cài đặt thêm các plugins Jenkins 
RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    docker-plugin \
    gradle \
    git \
    workflow-aggregator \
    pipeline-stage-view \
    blueocean \
    locale \
    configuration-as-code

# Dùng script đã tạo để tải trước các image cho docker
ENTRYPOINT ["/usr/local/bin/init.sh"]
```

<br><br>


Xây dựng image jenkins với tên `jenkins-app:lts-jdk21`

```shell
docker build -t jenkins-app:jiag .
```

<br><br>


Chạy image vừa xây dựng với với tên `jenkins-app-jiag` để sử dụng

```shell
docker run -itd \
  --privileged \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock:rw \
  --group-add $(getent group docker | cut -d: -f3) \
  --name jenkins-app-jiag \
  jenkins-app:jiag
```

