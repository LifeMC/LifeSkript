-injars 'target/Skript.jar'
-outjars 'target/Skript-optimized (EXPERIMENTAL).jar'

# Java 8 or older - All in one jar file
-libraryjars '<java.home>/lib/rt.jar'

# Java 9 or higher - Add required modules one by one
#-libraryjars <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
#-libraryjars <java.home>/jmods/java.logging.jmod(!**.jar;!module-info.class)

# We use sql module for the database stuff, variables etc.
#-libraryjars <java.home>/jmods/java.sql.jmod(!**.jar;!module-info.class)

# We use method handles, and it's not part of the java base module.
#-libraryjars <java.home>/jmods/jdk.dynalink.jmod(!**.jar;!module-info.class)

# We use management to get detailed information about threads etc.
#-libraryjars <java.home>/jmods/java.management.jmod(!**.jar;!module-info.class)

# Bukkit, Jansi and Timings - Bukkit is required, Jansi and Timings are optional.
-libraryjars '<user.home>/.m2/repository/org/bukkit/bukkit/1.8.8-R0.1-SNAPSHOT/bukkit-1.8.8-R0.1-SNAPSHOT.jar'
-libraryjars '<user.home>/.m2/repository/com/google/guava/guava/17.0/guava-17.0.jar'
-libraryjars '<user.home>/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar'
-libraryjars '<user.home>/.m2/repository/org/fusesource/jansi/jansi/1.18/jansi-1.18.jar'
-libraryjars '<user.home>/.m2/repository/co/aikar/timings/1.8.8/timings-1.8.8.jar'

# Hooks of Skript - All of them are optional.
-libraryjars '<user.home>/.m2/repository/net/milkbowl/vault/vault-plugin/1.5.6/vault-plugin-1.5.6.jar'
-libraryjars '<user.home>/.m2/repository/me/ryanhamshire/griefprevention/13.9.1/griefprevention-13.9.1.jar'
-libraryjars '<user.home>/.m2/repository/com/sk89q/worldguard/6.1.2/worldguard-6.1.2.jar'
-libraryjars '<user.home>/.m2/repository/com/sk89q/worldedit/6.1.9/worldedit-6.1.9.jar'
-libraryjars '<user.home>/.m2/repository/patpeter/sqlibrary/7.1/sqlibrary-7.1.jar'
-libraryjars '<user.home>/.m2/repository/fr/neatmonster/nocheatplus/3.16.1-SNAPSHOT/nocheatplus-3.16.1-SNAPSHOT.jar'

# Annotations used by Skript - All of them are optional.
-libraryjars '<user.home>/.m2/repository/org/eclipse/jdt/org.eclipse.jdt.annotation/1.1.400/org.eclipse.jdt.annotation-1.1.400.jar'
-libraryjars '<user.home>/.m2/repository/com/github/spotbugs/spotbugs-annotations/4.0.0-beta3/spotbugs-annotations-4.0.0-beta3.jar'
#-libraryjars '<user.home>/.m2/repository/javax/annotation/javax.annotation-api/1.3.2/javax.annotation-api-1.3.2.jar'
-libraryjars '<user.home>/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar'

-dontskipnonpubliclibraryclassmembers
-allowaccessmodification

-target 8
-forceprocessing

# Class merging & marking final: Mostly breaks binary compatibility and causes strange errors
# Field propagation, marking private etc.: Breaks binary compatibility and it's bugged
# Code allocation: Changes variable stack map and causes runtime strange errors
-optimizations !class/merging/**,!class/marking/final,!method/marking/final,!method/propagation/**,!field/**,!method/marking/private,!method/removal/parameter,!code/allocation/variable

# We mostly use optimization, and we want a powerful optimization. This maybe increased, but 10 looks powerful enough.
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
-adaptresourcefilenames **
-adaptresourcefilecontents **

# Method handles are giving false warnings in ProGuard - we must use this to exclude it from the warnings.
-dontwarn java.lang.invoke.MethodHandle

# We don't want anything - just optimize whatever possible. But these may be commented out for debugging purposes.
-dontnote

# Remove comment from this for debugging or analyzing purposes - it gives useful information, not like notes, warnings etc.
-verbose

# Android option has better optimization and default settings, use that.
-android

# Don't allow optimization for kotlin - it gives some strange errors that probably not fixable.
-keep,allowshrinking class ch.njol.libraries.kotlin.** { *; }

# Since Skript is also a library for Skript Add-Ons, we must keep all classes.
# We also load expressions, conditions etc. dynamically with reflaction, so keep them all.
-keep,allowoptimization class ch.njol.skript.** { *; }
-keep,allowoptimization class ch.njol.util.** { *; }
-keep,allowoptimization class ch.njol.yggdrasil.** { *; }

# For enumeration classes, without this, it gives runtime errors.
-keepclassmembers,allowoptimization enum ** { *; }

# For serializable classes - we use serialization.
-keepclassmembers,allowoptimization class ** implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep native methods - we don't use them directly, but libraries may use.
-keepclasseswithmembernames,allowoptimization,includedescriptorclasses class ** {
    native <methods>;
}

# Ignore kotlin null checks at runtime - we have compile time checks.
-assumenosideeffects class ch.njol.skript.libraries.kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Remove - System method calls. Remove all invocations of System
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.System {
    public static long currentTimeMillis();
    static java.lang.Class getCallerClass();
    public static int identityHashCode(java.lang.Object);
    public static java.lang.SecurityManager getSecurityManager();
    public static java.util.Properties getProperties();
    public static java.lang.String getProperty(java.lang.String);
    public static java.lang.String getenv(java.lang.String);
    public static java.lang.String mapLibraryName(java.lang.String);
    public static java.lang.String getProperty(java.lang.String,java.lang.String);
}

# Remove - Math method calls. Remove all invocations of Math
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.Math {
    public static double sin(double);
    public static double cos(double);
    public static double tan(double);
    public static double asin(double);
    public static double acos(double);
    public static double atan(double);
    public static double toRadians(double);
    public static double toDegrees(double);
    public static double exp(double);
    public static double log(double);
    public static double log10(double);
    public static double sqrt(double);
    public static double cbrt(double);
    public static double IEEEremainder(double,double);
    public static double ceil(double);
    public static double floor(double);
    public static double rint(double);
    public static double atan2(double,double);
    public static double pow(double,double);
    public static int round(float);
    public static long round(double);
    public static double random();
    public static int abs(int);
    public static long abs(long);
    public static float abs(float);
    public static double abs(double);
    public static int max(int,int);
    public static long max(long,long);
    public static float max(float,float);
    public static double max(double,double);
    public static int min(int,int);
    public static long min(long,long);
    public static float min(float,float);
    public static double min(double,double);
    public static double ulp(double);
    public static float ulp(float);
    public static double signum(double);
    public static float signum(float);
    public static double sinh(double);
    public static double cosh(double);
    public static double tanh(double);
    public static double hypot(double,double);
    public static double expm1(double);
    public static double log1p(double);
}

# Remove - Number method calls. Remove all invocations of Number
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.** extends java.lang.Number {
    public static java.lang.String toString(byte);
    public static java.lang.Byte valueOf(byte);
    public static byte parseByte(java.lang.String);
    public static byte parseByte(java.lang.String,int);
    public static java.lang.Byte valueOf(java.lang.String,int);
    public static java.lang.Byte valueOf(java.lang.String);
    public static java.lang.Byte decode(java.lang.String);
    public int compareTo(java.lang.Byte);
    public static java.lang.String toString(short);
    public static short parseShort(java.lang.String);
    public static short parseShort(java.lang.String,int);
    public static java.lang.Short valueOf(java.lang.String,int);
    public static java.lang.Short valueOf(java.lang.String);
    public static java.lang.Short valueOf(short);
    public static java.lang.Short decode(java.lang.String);
    public static short reverseBytes(short);
    public int compareTo(java.lang.Short);
    public static java.lang.String toString(int,int);
    public static java.lang.String toHexString(int);
    public static java.lang.String toOctalString(int);
    public static java.lang.String toBinaryString(int);
    public static java.lang.String toString(int);
    public static int parseInt(java.lang.String,int);
    public static int parseInt(java.lang.String);
    public static java.lang.Integer valueOf(java.lang.String,int);
    public static java.lang.Integer valueOf(java.lang.String);
    public static java.lang.Integer valueOf(int);
    public static java.lang.Integer getInteger(java.lang.String);
    public static java.lang.Integer getInteger(java.lang.String,int);
    public static java.lang.Integer getInteger(java.lang.String,java.lang.Integer);
    public static java.lang.Integer decode(java.lang.String);
    public static int highestOneBit(int);
    public static int lowestOneBit(int);
    public static int numberOfLeadingZeros(int);
    public static int numberOfTrailingZeros(int);
    public static int bitCount(int);
    public static int rotateLeft(int,int);
    public static int rotateRight(int,int);
    public static int reverse(int);
    public static int signum(int);
    public static int reverseBytes(int);
    public int compareTo(java.lang.Integer);
    public static java.lang.String toString(long,int);
    public static java.lang.String toHexString(long);
    public static java.lang.String toOctalString(long);
    public static java.lang.String toBinaryString(long);
    public static java.lang.String toString(long);
    public static long parseLong(java.lang.String,int);
    public static long parseLong(java.lang.String);
    public static java.lang.Long valueOf(java.lang.String,int);
    public static java.lang.Long valueOf(java.lang.String);
    public static java.lang.Long valueOf(long);
    public static java.lang.Long decode(java.lang.String);
    public static java.lang.Long getLong(java.lang.String);
    public static java.lang.Long getLong(java.lang.String,long);
    public static java.lang.Long getLong(java.lang.String,java.lang.Long);
    public static long highestOneBit(long);
    public static long lowestOneBit(long);
    public static int numberOfLeadingZeros(long);
    public static int numberOfTrailingZeros(long);
    public static int bitCount(long);
    public static long rotateLeft(long,int);
    public static long rotateRight(long,int);
    public static long reverse(long);
    public static int signum(long);
    public static long reverseBytes(long);
    public int compareTo(java.lang.Long);
    public static java.lang.String toString(float);
    public static java.lang.String toHexString(float);
    public static java.lang.Float valueOf(java.lang.String);
    public static java.lang.Float valueOf(float);
    public static float parseFloat(java.lang.String);
    public static boolean isNaN(float);
    public static boolean isInfinite(float);
    public static int floatToIntBits(float);
    public static int floatToRawIntBits(float);
    public static float intBitsToFloat(int);
    public static int compare(float,float);
    public boolean isNaN();
    public boolean isInfinite();
    public int compareTo(java.lang.Float);
    public static java.lang.String toString(double);
    public static java.lang.String toHexString(double);
    public static java.lang.Double valueOf(java.lang.String);
    public static java.lang.Double valueOf(double);
    public static double parseDouble(java.lang.String);
    public static boolean isNaN(double);
    public static boolean isInfinite(double);
    public static long doubleToLongBits(double);
    public static long doubleToRawLongBits(double);
    public static double longBitsToDouble(long);
    public static int compare(double,double);
    public boolean isNaN();
    public boolean isInfinite();
    public int compareTo(java.lang.Double);
    public byte byteValue();
    public short shortValue();
    public int intValue();
    public long longValue();
    public float floatValue();
    public double doubleValue();
    public int compareTo(java.lang.Object);
    public boolean equals(java.lang.Object);
    public int hashCode();
    public java.lang.String toString();
}

# Remove - String method calls. Remove all invocations of String
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.String {
    public static java.lang.String copyValueOf(char[]);
    public static java.lang.String copyValueOf(char[],int,int);
    public static java.lang.String valueOf(boolean);
    public static java.lang.String valueOf(char);
    public static java.lang.String valueOf(char[]);
    public static java.lang.String valueOf(char[],int,int);
    public static java.lang.String valueOf(double);
    public static java.lang.String valueOf(float);
    public static java.lang.String valueOf(int);
    public static java.lang.String valueOf(java.lang.Object);
    public static java.lang.String valueOf(long);
    public boolean contentEquals(java.lang.StringBuffer);
    public boolean endsWith(java.lang.String);
    public boolean equalsIgnoreCase(java.lang.String);
    public boolean equals(java.lang.Object);
    public boolean matches(java.lang.String);
    public boolean regionMatches(boolean,int,java.lang.String,int,int);
    public boolean regionMatches(int,java.lang.String,int,int);
    public boolean startsWith(java.lang.String);
    public boolean startsWith(java.lang.String,int);
    public byte[] getBytes();
    public byte[] getBytes(java.lang.String);
    public char charAt(int);
    public char[] toCharArray();
    public int compareToIgnoreCase(java.lang.String);
    public int compareTo(java.lang.Object);
    public int compareTo(java.lang.String);
    public int hashCode();
    public int indexOf(int);
    public int indexOf(int,int);
    public int indexOf(java.lang.String);
    public int indexOf(java.lang.String,int);
    public int lastIndexOf(int);
    public int lastIndexOf(int,int);
    public int lastIndexOf(java.lang.String);
    public int lastIndexOf(java.lang.String,int);
    public int length();
    public java.lang.CharSequence subSequence(int,int);
    public java.lang.String concat(java.lang.String);
    public java.lang.String replaceAll(java.lang.String,java.lang.String);
    public java.lang.String replace(char,char);
    public java.lang.String replaceFirst(java.lang.String,java.lang.String);
    public java.lang.String[] split(java.lang.String);
    public java.lang.String[] split(java.lang.String,int);
    public java.lang.String substring(int);
    public java.lang.String substring(int,int);
    public java.lang.String toLowerCase();
    public java.lang.String toLowerCase(java.util.Locale);
    public java.lang.String toString();
    public java.lang.String toUpperCase();
    public java.lang.String toUpperCase(java.util.Locale);
    public java.lang.String trim();
}

# Remove - StringBuffer method calls. Remove all invocations of StringBuffer
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.StringBuffer {
    public java.lang.String toString();
    public char charAt(int);
    public int capacity();
    public int codePointAt(int);
    public int codePointBefore(int);
    public int indexOf(java.lang.String,int);
    public int lastIndexOf(java.lang.String);
    public int lastIndexOf(java.lang.String,int);
    public int length();
    public java.lang.String substring(int);
    public java.lang.String substring(int,int);
}

# Remove - StringBuilder method calls. Remove all invocations of StringBuilder
# methods without side effects whose return values are not used.
-assumenosideeffects public class java.lang.StringBuilder {
    public java.lang.String toString();
    public char charAt(int);
    public int capacity();
    public int codePointAt(int);
    public int codePointBefore(int);
    public int indexOf(java.lang.String,int);
    public int lastIndexOf(java.lang.String);
    public int lastIndexOf(java.lang.String,int);
    public int length();
    public java.lang.String substring(int);
    public java.lang.String substring(int,int);
}
