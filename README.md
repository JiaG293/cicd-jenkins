**#1. Create ```Dockerfile```**
<br>
```shell
nano Dockerfile
```

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
---

**#2. Build jenkins container custom**
<br>
```shell
docker build -t jenkins-app:lts-jdk21 .
```
---

**#3. Run jenkins container custom**
<br>

```shell
docker network create jenkins
```

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

---

**#4. Run docker in docker**
<br>

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

---
