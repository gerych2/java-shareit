@echo off
echo Building the project...
call mvn clean install -DskipTests

echo.
echo Starting ShareIt Server on port 9090...
start "ShareIt Server" cmd /k "cd shareit-server && mvn spring-boot:run"

timeout /t 10 /nobreak

echo.
echo Starting ShareIt Gateway on port 8080...
start "ShareIt Gateway" cmd /k "cd shareit-gateway && mvn spring-boot:run"

echo.
echo Both servers are starting...
echo Server will be ready in about 20-30 seconds
echo Gateway: http://localhost:8080
echo Server: http://localhost:9090

