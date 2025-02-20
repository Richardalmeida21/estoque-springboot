# Usar uma imagem oficial com Java e Maven instalados
FROM maven:3.8.8-eclipse-temurin-17

# Definir diretório de trabalho
WORKDIR /app

# Copiar os arquivos do projeto para dentro do container
COPY . .

# Compilar o projeto
RUN mvn clean package

# Comando para rodar a aplicação
CMD ["java", "-jar", "target/*.jar"]
