#!/usr/bin/env bash

# Instalar Java manualmente (caso o Render não tenha)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

# Instalar Maven manualmente
curl -sL https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz | tar xz
export PATH=$PWD/apache-maven-3.8.8/bin:$PATH

# Mostrar versões para depuração
java -version
mvn -version

# Compilar o projeto
mvn clean package
