-injars 'target/Skript.jar'
-outjars 'target/Skript-optimized (EXPERIMENTAL).jar'

# If building with Java 9 or higher, change this to JMods directory.
-libraryjars '<java.home>/lib/rt.jar'

-libraryjars '<user.home>/.m2/repository/org/bukkit/bukkit/1.8.8-R0.1-SNAPSHOT/bukkit-1.8.8-R0.1-SNAPSHOT.jar'
-libraryjars '<user.home>/.m2/repository/org/fusesource/jansi/jansi/1.18/jansi-1.18.jar'
#-libraryjars '<user.home>/.m2/repository/org/eclipse/jdt.annotation/1.1.0/jdt.annotation-1.1.0.jar'

-dontskipnonpubliclibraryclassmembers
-target 1.8
-forceprocessing
#-dontshrink

# Class merging & marking final: Mostly breaks binary compatibility and causes strange errors
# Field propagation, marking private etc.: Breaks binary compatibility and it's bugged
# Enum class unboxing: Gives strange errors on runtime
# Code allocation: Changes variable stack map and causes runtime strange errors
-optimizations !class/merging/*,!class/marking/final,!method/marking/final,!field/*,!method/marking/private,!method/removal/parameter,!class/unboxing/enum,!code/allocation/variable

# We moslty use optimization, and we want a powerful optimization. This maybe increased, but 10 looks powerful enough.
# We also don't want to wait too much when building the project, so don't increase this too much.
# If you want to speed up the optimization and shrinking process, set this to 5 because after 5 there is not so much optimizations.
# Note: Whatever you set this number, it always stop when there is no optimization after a pass. So it still builds fast with high values.
-optimizationpasses 10

-dontobfuscate
-dontusemixedcaseclassnames
-keeppackagenames
-keepattributes **
-keepparameternames
-adaptclassstrings
-adaptresourcefilenames **.properties,META-INF/MANIFEST.MF,**.MF,**.kotlin_module,**.sk,**.yml,**.md,version,**.lang,**.txt,version,LICENSE
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF,**.MF,**.kotlin_module,**.sk,**.yml,**.md,version,**.lang,**.txt,version,LICENSE

# We don't want anything - just optimize whatever possible. But these may be commented out for debugging purposes.
-dontnote
-dontwarn
-ignorewarnings

# Remove comment from this for debugging or analyzing purposes - it gives useful information, not like notes, warnings etc.
-verbose

# Don't allow optimization for kotlin - it gives some strange errors that probably not fixable.
-keep,allowshrinking class ch.njol.libraries.kotlin.** { *; }

# Since Skript is also a library for Skript Add-Ons, we must keep all classes.
# We also load expressions, conditions etc. dynamically with reflaction, so keep them all.
-keep,allowoptimization class ch.njol.skript.** { *; }
-keep,allowoptimization class ch.njol.util.** { *; }
-keep,allowoptimization class ch.njol.yggdrasil.** { *; }

#-keep,allowoptimization class ** { *; }
#-assumenosideeffects class ** { *; }
