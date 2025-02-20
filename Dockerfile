# Usar imagem oficial do Maven com Java 17
FROM maven:3.8.8-eclipse-temurin-17 AS build

# Definir diretório de trabalho
WORKDIR /app

# Copiar os arquivos do projeto para dentro do container
COPY . .

# Compilar o projeto e gerar o JAR
RUN mvn clean package -DskipTests

# Segunda etapa: Usar uma imagem leve do Java para rodar o JAR
FROM eclipse-temurin:17-jdk

# Definir diretório de trabalho
WORKDIR /app

# Copiar o JAR gerado da etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Comando para rodar a aplicação
CMD ["java", "-jar", "app.jar"]
