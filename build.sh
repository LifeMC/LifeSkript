#!/usr/bin/env bash

#
#
#     This file is part of Skript.
#
#    Skript is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    Skript is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with Skript. If not, see <https://www.gnu.org/licenses/>.
#
#
#   Copyright 2011-2020 Peter Güttinger and contributors
#
#

./mvnw -nsu install:install-file -Dfile=lib/timings-1.8.8.jar -DgroupId=co.aikar -DartifactId=timings -Dversion=1.8.8 -Dpackaging=jar -DgeneratePom=true
./mvnw -nsu install:install-file -Dfile=lib/vault-plugin-1.5.6.jar -DgroupId=net.milkbowl.vault -DartifactId=vault-plugin -Dversion=1.5.6 -Dpackaging=jar -DgeneratePom=true
./mvnw -nsu install:install-file -Dfile=lib/sqlibrary-7.1.jar -DgroupId=patpeter -DartifactId=sqlibrary -Dversion=7.1 -Dpackaging=jar -DgeneratePom=true
./mvnw -nsu install:install-file -Dfile=lib/griefprevention-13.9.1.jar -DgroupId=me.ryanhamshire -DartifactId=griefprevention -Dversion=13.9.1 -Dpackaging=jar -DgeneratePom=true
./mvnw -nsu install:install-file -Dfile=lib/worldguard-6.1.2.jar -DgroupId=com.sk89q -DartifactId=worldguard -Dversion=6.1.2 -Dpackaging=jar -DgeneratePom=true
./mvnw -nsu install:install-file -Dfile=lib/worldedit-6.1.9.jar -DgroupId=com.sk89q -DartifactId=worldedit -Dversion=6.1.9 -Dpackaging=jar -DgeneratePom=true

./mvnw -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -DcompilerArgument=-O -e -nsu -U clean install package
java -Duser.name="Skript Team" -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Duser.language=en -Duser.country=US -Duser.timezone=Asia/Istanbul -jar lib/proguard.jar @Skript.pro
