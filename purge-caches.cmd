@echo off
chcp 65001 > nul
@setlocal enableextensions
@cd /d "%~dp0"

call ./mvnw.cmd install:install-file -Dfile=lib/timings-1.8.8-SNAPSHOT.jar -DgroupId=co.aikar -DartifactId=timings -Dversion=1.8.8-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/vault-1.5.6.jar -DgroupId=net.milkbowl.vault -DartifactId=vault -Dversion=1.5.6 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/ecj-4.12.jar -DgroupId=org.eclipse.jdt.core.compiler -DartifactId=ecj -Dversion=4.12 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/sqlibrary-7.1.jar -DgroupId=patpeter -DartifactId=sqlibrary -Dversion=7.1 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/griefprevention-13.9.1.jar -DgroupId=me.ryanhamshire -DartifactId=griefprevention -Dversion=13.9.1 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/worldguard-6.1.2.jar -DgroupId=com.sk89q -DartifactId=worldguard -Dversion=6.1.2 -Dpackaging=jar -DgeneratePom=true
call ./mvnw.cmd install:install-file -Dfile=lib/worldedit-6.1.9.jar -DgroupId=com.sk89q -DartifactId=worldedit -Dversion=6.1.9 -Dpackaging=jar -DgeneratePom=true

call ./mvnw.cmd -e dependency:purge-local-repository
pause
cmd /k
