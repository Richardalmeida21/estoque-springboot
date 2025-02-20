#!/usr/bin/env bash

# Instalar Maven manualmente
curl -sL https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz | tar xz
export PATH=$PWD/apache-maven-3.8.8/bin:$PATH

# Compilar o projeto
mvn clean package
