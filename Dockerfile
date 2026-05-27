FROM eclipse-temurin:17

WORKDIR /app

COPY . .

RUN javac -d out -encoding UTF-8 @sources.txt

CMD ["sh", "-c", "java -cp out Main"]