@echo off
cd /d %~dp0
echo Starting website...
call mvn spring-boot:run
pause