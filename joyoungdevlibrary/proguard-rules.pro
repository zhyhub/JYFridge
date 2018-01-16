# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-libraryjars 'C:\Program Files\Java\jdk1.8.0_91\jre\lib\rt.jar'

-libraryjars 'D:\sdk\platforms\android-26\android.jar'


#指定代码的压缩级别
-optimizationpasses 5
#混淆时不使用大小写混合类名
-dontusemixedcaseclassnames
#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

#不优化输入的类文件
-dontoptimize
#预校验
-dontpreverify
#混淆时是否记录日志
-verbose
#忽略警告
-ignorewarning
#混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#优化时允许访问并修改类和类的成员的 访问修饰符，可能作用域会变大。
-allowaccessmodification
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,InnerClasses
#保护注解
-keepattributes Annotation

-keep public class com.joyoungdevlibrary.utils.URLGenerateUtil{*;}
-keep class com.joyoungdevlibrary.utils.JoyoungLinkSDK{*;}
-keep class com.joyoungdevlibrary.utils.JoyoungDevkSDK{*;}

-dontwarn  com.joyoungdevlibrary.config.**
-keep class com.joyoungdevlibrary.config.**{*;}

-dontwarn  com.joyoungdevlibrary.http.**
-keep class com.joyoungdevlibrary.http.**{*;}

-dontwarn  com.joyoungdevlibrary.info.**
-keep class com.joyoungdevlibrary.info.**{*;}

-dontwarn  com.joyoungdevlibrary.interface_sdk.**
-keep class com.joyoungdevlibrary.interface_sdk.**{*;}

-keep class okhttp3.Callback{*;}
# 保持哪些类不被混淆
-keep public class * extends android.app.Service

-keepclasseswithmembernames class * {
    native <methods>;
}

#保持 Serializable 不被混淆并且enum 类也不被混淆
-keep class * implements java.io.Serializable{
    public protected private *;
}

-dontwarn org.apache.http.entity.mime.**
-keep class org.apache.http.entity.mime.** { *;}

-dontwarn org.apache.commons.codec.**
-keep class org.apache.commons.codec.** { *;}

-dontwarn org.apache.james.mime4j.**
-keep class org.apache.james.mime4j.** { *;}



-keep class android.support.v4.** { *; }
-dontwarn android.support.v4.**
# OkHttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase