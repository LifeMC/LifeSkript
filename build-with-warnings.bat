@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn -e -DcompilerArgument=-Xlint:all clean install package
pause
cmd /k
