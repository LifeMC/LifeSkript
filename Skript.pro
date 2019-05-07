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
-optimizations !class/merging/*,!field/*,!method/marking/private,!method/removal/*,!code/removal/advanced,!code/removal/simple,!code/allocation/*

# We only use optimization, and we want a powerful optimization. This maybe increased, but 10 looks powerful enough.
# We also don't want to wait too much when building the project, so don't increase this too much.
# If you want to speed up the optimization and shrinking process, set this to 5 because after 5 there is no real optimization.
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

# Why are you keeping the unused libraries, what rule it causes to keep?
-whyareyoukeeping class ch.njol.libraries.* { *; }

-keep,allowoptimization class ch.njol.skript.* { *; }
-keep,allowoptimization class ch.njol.util.* { *; }
-keep,allowoptimization class ch.njol.yggdrasil.* { *; }

#-keep,allowoptimization class **

# More Android - R classes. Keep all fields of Android R classes.
-keepclassmembers,allowshrinking,allowoptimization class **.R$**.R$* {
    public static <fields>;
}

# Android libraries - Design support libraries. Keep setters for design support libraries.
-keep,allowshrinking,allowoptimization !abstract class android.support.design.widget.android.support.design.widget.* extends android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context,android.util.AttributeSet);
}

# Android libraries - RxJava. Keep classes for RxJava.
-keepclassmembers,allowshrinking,allowoptimization class rx.internal.util.unsafe.rx.internal.util.unsafe.*QueueQueue {
    long producerIndex;
    long consumerIndex;
    rx.internal.util.atomic.LinkedQueueNode producerNode;
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# Android libraries - Butterknife code. Keep the classes that Butterknife accesses
# by reflection.
-keepclasseswithmembers,allowshrinking,allowoptimization class butterknife.* {
    @butterknife.*
    <fields>;
}

-keepclasseswithmembers,allowshrinking,allowoptimization class butterknife.* {
    @butterknife.*
    <methods>;
}

-keepclasseswithmembers,allowshrinking,allowoptimization class butterknife.* {
    @butterknife.On*
    <methods>;
}

-keep,allowshrinking,allowobfuscation,allowoptimization @interface  butterknife.butterknife.*

# Android libraries - Design support libraries. Keep setters for design support libraries.
-kee,allowshrinking,allowoptimization !abstract class android.support.design.widget.android.support.design.widget.* extends android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context,android.util.AttributeSet);
}

# Android libraries - Design support libraries. Keep setters for design support libraries.
-keep,allowshrinking,allowoptimization !abstract class android.support.design.widget.android.support.design.widget.* extends android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context,android.util.AttributeSet);
}

# Android libraries - Design support libraries. Keep setters for design support libraries.
-keep,allowshrinking,allowoptimization !abstract class android.support.design.widget.android.support.design.widget.* extends android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context,android.util.AttributeSet);
}

# Android libraries - Design support libraries. Keep setters for design support libraries.
-keep,allowshrinking,allowoptimization !abstract class android.support.design.widget.android.support.design.widget.* extends android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context,android.util.AttributeSet);
}

# Android libraries - Google Cloud Messaging. Keep classes for Google Cloud Messaging.

-keep,allowshrinking,allowoptimization class com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.*ApiApi

-keep,allowshrinking,allowoptimization class com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.*ApiApi

-keep,allowshrinking,allowoptimization class com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.*ApiApi

-keep,allowshrinking,allowoptimization class com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.*ApiApi

-keep,allowshrinking,allowoptimization class com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.*ApiApi

# Keep - Applications. Keep all application classes, along with their 'main' methods.
-keepclasseswithmembers,allowshrinking,allowoptimization public class * {
    public static void main(java.lang.String[]);
}

# Keep - Applets. Keep all extensions of java.applet.Applet.
-keep,allowshrinking,allowoptimization public class * extends java.applet.Applet

# Keep - Servlets. Keep all extensions of javax.servlet.Servlet.
-keep,allowshrinking,allowoptimization public class * extends javax.servlet.Servlet

# Keep - Midlets. Keep all extensions of javax.microedition.midlet.MIDlet.
-keep,allowshrinking,allowoptimization public class * extends javax.microedition.midlet.MIDlet

# Keep - Xlets. Keep all extensions of javax.tv.xlet.Xlet.
-keep,allowshrinking,allowoptimization public class * extends javax.tv.xlet.Xlet

# Keep - Libraries. Keep all public and protected classes, fields, and methods.
-keep,allowshrinking,allowoptimization public class * {
    public protected <fields>;
    public protected <methods>;
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Also keep - Serialization code. Keep all fields and methods that are used for
# serialization.
-keepclassmembers,allowshrinking,allowoptimization class * extends java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Also keep - BeanInfo classes. Keep all implementations of java.beans.BeanInfo.
-keep,allowshrinking,allowoptimization class * extends java.beans.BeanInfo

# Also keep - Bean classes. Keep all specified classes, along with their getters
# and setters.
-keep,allowshrinking,allowoptimization class * {
    void set*(***);
    void set*(int,***);
    boolean is*();
    boolean is*(int);
    *** get*();
    *** get*(int);
}

# Also keep - Database drivers. Keep all implementations of java.sql.Driver.
-keep,allowshrinking,allowoptimization class * extends java.sql.Driver

# Also keep - Swing UI L&F. Keep all extensions of javax.swing.plaf.ComponentUI,
# along with the special 'createUI' method.
-keep,allowshrinking,allowoptimization class * extends javax.swing.plaf.ComponentUI {
    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent);
}

# Also keep - RMI interfaces. Keep all interfaces that extend the
# java.rmi.Remote interface, and their methods.
-keep,allowshrinking,allowoptimization interface  * extends java.rmi.Remote {
    <methods>;
}

# Also keep - RMI implementations. Keep all implementations of java.rmi.Remote,
# including any explicit or implicit implementations of Activatable, with their
# two-argument constructors.
-keep,allowshrinking,allowoptimization class * extends java.rmi.Remote {
    <init>(java.rmi.activation.ActivationID,java.rmi.MarshalledObject);
}

# Android - Android activities. Keep all extensions of Android activities.
-keep,allowshrinking,allowoptimization public class * extends android.app.Activity

# Android - Android applications. Keep all extensions of Android applications.
-keep,allowshrinking,allowoptimization public class * extends android.app.Application

# Android - Android services. Keep all extensions of Android services.
-keep,allowshrinking,allowoptimization public class * extends android.app.Service

# Android - Broadcast receivers. Keep all extensions of Android broadcast receivers.
-keep,allowshrinking,allowoptimization public class * extends android.content.BroadcastReceiver

# Android - Content providers. Keep all extensions of Android content providers.
-keep,allowshrinking,allowoptimization public class * extends android.content.ContentProvider

# More Android - View classes. Keep all Android views and their constructors and setters.
-keep,allowshrinking,allowoptimization public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
    public void set*(...);
}

# More Android - Layout classes. Keep classes with constructors that may be referenced from Android layout
# files.
-keepclasseswithmembers,allowshrinking,allowoptimization class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers,allowshrinking,allowoptimization class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

# More Android - Contexts. Keep all extensions of Android Context.
-keepclassmembers,allowshrinking,allowoptimization class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

# More Android - Parcelables. Keep all extensions of Android Parcelables.
-keepclassmembers,allowshrinking,allowoptimization class * extends android.os.Parcelable {
    static ** CREATOR;
}

# Android annotations - Support annotations. Support annotations for Android.
-keep,allowshrinking,allowoptimization @android.support.annotation.Keep class *

-keepclassmembers,allowshrinking,allowoptimization class * {
    @android.support.annotation.Keep
    <fields>;
    @android.support.annotation.Keep
    <methods>;
}

# Android annotations - Facebook keep annotations. Keep annotations for Facebook.
-keep,allowshrinking,allowoptimization @com.facebook.proguard.annotations.DoNotStrip class *

-keepclassmembers,allowshrinking,allowoptimization class * {
    @com.facebook.proguard.annotations.DoNotStrip
    <fields>;
    @com.facebook.proguard.annotations.DoNotStrip
    <methods>;
}

-keep,allowshrinking,allowoptimization @com.facebook.proguard.annotations.KeepGettersAndSetters class *

-keepclassmembers,allowshrinking,allowoptimization class * {
    @com.facebook.proguard.annotations.KeepGettersAndSetters
    <fields>;
    @com.facebook.proguard.annotations.KeepGettersAndSetters
    <methods>;
}

# Android annotations - ProGuard annotations. Keep annotations for ProGuard.
-keep,allowshrinking,allowoptimization @proguard.annotation.Keep class *

-keepclassmembers,allowshrinking,allowoptimization class * {
    @proguard.annotation.Keep
    <fields>;
    @proguard.annotation.Keep
    <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization class * {
    @proguard.annotation.KeepName
    <fields>;
    @proguard.annotation.KeepName
    <methods>;
}

-keep,allowshrinking,allowoptimization class * extends @proguard.annotation.KeepImplementations *

-keep,allowshrinking,allowoptimization public class * extends @proguard.annotation.KeepPublicImplementations *

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepClassMembers class * {
    <fields>;
    <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepPublicClassMembers class * {
    public <fields>;
    public <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepPublicProtectedClassMembers class * {
    public protected <fields>;
    public protected <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepClassMemberNames class * {
    <fields>;
    <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepPublicClassMemberNames class * {
    public <fields>;
    public <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepPublicProtectedClassMemberNames class * {
    public protected <fields>;
    public protected <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepGettersSetters class * {
    void set*(***);
    void set*(int,***);
    boolean is*();
    boolean is*(int);
    *** get*();
    *** get*(int);
}

-keepclassmembers,allowshrinking,allowoptimization @proguard.annotation.KeepPublicGettersSetters class * {
    public void set*(***);
    public void set*(int,***);
    public boolean is*();
    public boolean is*(int);
    public *** get*();
    public *** get*(int);
}

-keep,allowshrinking,allowoptimization @proguard.annotation.KeepName class *

# Android libraries - Design support libraries. Keep setters for design support libraries.
-keep,allowshrinking,allowoptimization !abstract class android.support.design.widget.* extends android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context,android.util.AttributeSet);
}

-keep,allowshrinking,allowoptimization class android.support.design.widget.CoordinatorLayout

# Android libraries - Google Play Services. Keep classes for Google Play Services.
-keep,allowshrinking,allowoptimization class com.google.android.gms.tagmanager.TagManagerService

-keep,allowshrinking,allowoptimization class com.google.android.gms.measurement.AppMeasurement

-keep,allowshrinking,allowoptimization class com.google.android.gms.measurement.AppMeasurementReceiver

-keep,allowshrinking,allowoptimization class com.google.android.gms.measurement.AppMeasurementService

-keepclassmembers,allowshrinking,allowoptimization class com.google.android.gms.ads.identifier.AdvertisingIdClient,com.google.android.gms.ads.identifier.AdvertisingIdClient$Info,com.google.android.gms.common.GooglePlayServicesUtil {
    public <methods>;
}

-keep,allowobfuscation,allowshrinking,allowoptimization class com.google.android.gms.ads.identifier.AdvertisingIdClient,com.google.android.gms.ads.identifier.AdvertisingIdClient$Info,com.google.android.gms.common.GooglePlayServicesUtil

-keep,allowshrinking,allowshrinking,allowoptimization class com.google.android.gms.iid.MessengerCompat,com.google.android.gms.location.ActivityRecognitionResult,com.google.android.gms.maps.GoogleMapOptions

-keep,allowshrinking,allowshrinking,allowoptimization class com.google.android.gms.ads.AdActivity,com.google.android.gms.ads.purchase.InAppPurchaseActivity,com.google.android.gms.gcm.GoogleCloudMessaging,com.google.android.gms.location.places.*Api

-keepclassmember,allowshrinking,allowoptimization class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final java.lang.String NULL;
}

# Android libraries - Firebase. Keep classes for Firebase for Android.
-keep,allowshrinking,allowoptimization class com.google.firebase.FirebaseApp

-keep,allowshrinking,allowoptimization class com.google.firebase.auth.FirebaseAuth

-keep,allowshrinking,allowoptimization class com.google.firebase.crash.FirebaseCrash

-keep,allowshrinking,allowoptimization class com.google.firebase.database.connection.idl.IPersistentConnectionImpl

-keep,allowshrinking,allowoptimization class com.google.firebase.iid.FirebaseInstanceId

# Android libraries - Guava. Keep classes for the Guava libraries.
-keepclassmembers,allowshrinking,allowoptimization class com.google.common.primitives.UnsignedBytes$LexicographicalComparatorHolder$UnsafeComparator {
    sun.misc.Unsafe theUnsafe;
}

# Android libraries - ActionBarSherlock. Keep classes for ActionBarSherlock.
-keepclassmembers,allowshrinking,allowoptimization !abstract class * extends com.actionbarsherlock.ActionBarSherlock {
    <init>(android.app.Activity,int);
}

# Android libraries - GSON. Keep classes for the GSON library.
-keepclassmembers,allowshrinking,allowoptimization class * {
    @com.google.gson.annotations.Expose
    <fields>;
}

-keepclassmembers,allowshrinking,allowoptimization enum  * {
    @com.google.gson.annotations.SerializedName
    <fields>;
}

-keepclasseswithmembers,includedescriptorclasses,allowshrinking,allowobfuscation,allowoptimization class * {
    @com.google.gson.annotations.Expose
    <fields>;
}

-keepclasseswithmembers,includedescriptorclasses,allowshrinking,allowobfuscation,allowoptimization class * {
    @com.google.gson.annotations.SerializedName
    <fields>;
}

# Android libraries - Dagger code. Keep the classes that Dagger accesses
# by reflection.
-keep,allowshrinking,allowoptimization class **$$ModuleAdapter

-keep,allowshrinking,allowoptimization class **$$InjectAdapter

-keep,allowshrinking,allowoptimization class **$$StaticInjection

-if class **$$ModuleAdapter

-keep,allowshrinking,allowoptimization class <1>

-if class **$$InjectAdapter

-keep,allowshrinking,allowoptimization class <1>

-if class **$$StaticInjection

-keep,allowshrinking,allowoptimization class <1>

-keep,allowshrinking,allowoptimization class dagger.Lazy

-keepclassmembers,allowobfuscation,allowshrinking,allowoptimization class * {
    @dagger.**
    <fields>;
    @dagger.**
    <methods>;
}

# Android libraries - Roboguice. Keep classes for RoboGuice.
-keepclassmembers,allowshrinking,allowoptimization class * extends com.google.inject.Module {
    <init>(android.content.Context);
    <init>();
}

-keepclassmembers,allowshrinking,allowoptimization class android.support.v4.app.Fragment {
    public android.view.View getView();
}

-keepclassmembers,allowshrinking,allowoptimization class android.support.v4.app.FragmentManager {
    public android.support.v4.app.Fragment findFragmentById(int);
    public android.support.v4.app.Fragment findFragmentByTag(java.lang.String);
}

-keep,allowobfuscation,allowshrinking,allowoptimization class roboguice.activity.event.OnCreateEvent

-keep,allowobfuscation,allowshrinking,allowoptimization class roboguice.inject.SharedPreferencesProvider$PreferencesNameHolder

# Android libraries - Greenrobot EventBus V2.
-keepclassmembers,allowshrinking,allowoptimization class * {
    public void onEvent*(***);
}

# Android libraries - Greenrobot EventBus V3.
-keep,allowshrinking,allowoptimization enum  org.greenrobot.eventbus.ThreadMode {
    <fields>;
    <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keep,allowobfuscation,allowshrinking,allowoptimization class org.greenrobot.eventbus.Subscribe {
    <fields>;
    <methods>;
}

-keepclassmembers,allowobfuscation,allowshrinking,allowoptimization class ** {
    @org.greenrobot.eventbus.Subscribe
    <methods>;
}

# Android libraries - Google API. Keep classes and field for Google API.
-keepclassmembers,allowshrinking,allowoptimization class * {
    @com.google.api.client.util.Key
    <fields>;
    @com.google.api.client.util.Value
    <fields>;
    @com.google.api.client.util.NullValue
    <fields>;
}

-keep,allowobfuscation,allowshrinking,allowoptimization class com.google.api.client.util.Types {
    java.lang.IllegalArgumentException handleExceptionForNewInstance(java.lang.Exception,java.lang.Class);
}

# Android libraries - Facebook API. Keep methods for the Facebook API.
-keepclassmembers,allowshrinking,allowoptimization interface  com.facebook.model.GraphObject {
    <methods>;
}

# Android libraries - Javascript interfaces. Keep all methods from Android Javascripts.
-keepclassmembers,allowshrinking,allowoptimization class * {
    @android.webkit.JavascriptInterface
    <methods>;
}

# Android libraries - Retrofit. Keep classes for Retrofit.
-keepclassmembers,allowshrinking,allowoptimization @retrofit.http.RestMethod @interface  * {
    <methods>;
}

-keepclassmembers,allowobfuscation,allowshrinking,allowoptimization interface  * {
    @retrofit.http.**
    <methods>;
}

-keep,allowobfuscation,allowshrinking,allowoptimization @retrofit.http.RestMethod @interface  *

-keep,allowobfuscation,allowshrinking,allowoptimization @interface  retrofit2.http.**

# Android libraries - Google inject. Keep classes for Google inject.
-keepclassmembers,allowshrinking,allowoptimization class * {
    void finalizeReferent();
}

-keepclassmembers,allowshrinking,allowoptimization class com.google.inject.internal.util.$Finalizer {
    public static java.lang.ref.ReferenceQueue startFinalizer(java.lang.Class,java.lang.Object);
}

-keep,allowshrinking,allowoptimization class com.google.inject.internal.util.$FinalizableReference

# Android libraries - Jackson. Keep classes for Jackson.
-keepclassmembers,allowshrinking,allowoptimization class * {
    @org.codehaus.jackson.annotate.*
    <methods>;
}

-keepclassmembers,allowshrinking,allowoptimization @org.codehaus.jackson.annotate.JsonAutoDetect class * {
    void set*(***);
    *** get*();
    boolean is*();
}

# Android libraries - Crashlytics. Keep classes for Crashlytics.
-keep,allowshrinking,allowoptimization class * extends io.fabric.sdk.android.Kit

# Keep - Native method names. Keep all native class/method names.
-keepclasseswithmembers,includedescriptorclasses,allowshrinking,allowoptimization class * {
    native <methods>;
}

# Keep - _class method names. Keep all .class method names. This may be
# useful for libraries that will be obfuscated again with different obfuscators.
-keepclassmembers,allowshrinking,allowoptimization class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String,boolean);
}

# Android annotations - Google keep annotations. Keep annotations for Google.
-keepclassmembers,allowshrinking,allowoptimization class * {
    @com.google.android.gms.common.annotation.KeepName
    <fields>;
    @com.google.android.gms.common.annotation.KeepName
    <methods>;
}

-keep,allowshrinking,allowoptimization @com.google.android.gms.common.annotation.KeepName class *

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
-assumenosideeffects public class java.lang.* extends java.lang.Number {
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

-assumenosideeffects class **
