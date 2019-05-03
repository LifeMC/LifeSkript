@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"
call mvn install -Dversions.skip=true -Dmaven.test.skip=true -P !deploy
pause
