#!/usr/bin/env bash

# Instalar Java e configurar JAVA_HOME
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
export PATH=$JAVA_HOME/bin:$PATH

# Instalar Maven manualmente
curl -sL https://archive.apache.org/dist/maven/maven-3/3.8.8/binaries/apache-maven-3.8.8-bin.tar.gz | tar xz
export PATH=$PWD/apache-maven-3.8.8/bin:$PATH

# Compilar o projeto
mvn clean package
