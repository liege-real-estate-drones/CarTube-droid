# Add project specific ProGuard rules here.
# By default, the flags in this file are applied to all build types.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If you want to enable ProGuard for some build types but not others, you can
# edit the build.gradle file.
#
# See http://proguard.sourceforge.net/manual/usage.html for a complete list
# of flags.
#
# -keep class com.your.package.YourClass {
#   public <init>();
#   void yourMethod(java.lang.String);
# }

# Rules for NewPipeExtractor
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**
