#!/usr/bin/env bash

# Atualizar pacotes e instalar Java 17
apt-get update && apt-get install -y openjdk-17-jdk

# Definir JAVA_HOME manualmente
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Verificar se Java foi instalado corretamente
java -version

# Instalar Maven manualmente
curl -sL https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz | tar xz
export PATH=$PWD/apache-maven-3.8.8/bin:$PATH

# Verificar instalação do Maven
mvn -version

# Compilar o projeto
mvn clean package
