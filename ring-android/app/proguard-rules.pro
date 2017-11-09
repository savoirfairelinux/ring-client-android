-keepclassmembers class * {
  public <init>(android.content.Context);
}

-keepattributes Signature

-keep class android.support.v7.widget.LinearLayoutManager { *; }

-keep,includedescriptorclasses class cx.ring.** { *; }
-keepclassmembers class cx.ring.** { *; }

#OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

# Keep the helper class and its constructor
-keep class * extends com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper

# Keep all model classes that are used by OrmLite
# Also keep their field names and the constructor
-keep @com.j256.ormlite.table.DatabaseTable class * {
    @com.j256.ormlite.field.DatabaseField <fields>;
    @com.j256.ormlite.field.ForeignCollectionField <fields>;
    <init>();
}

-dontwarn ezvcard.io.json.JCardModule
-dontwarn com.fasterxml.jackson.**
-dontwarn org.jsoup.**
-dontwarn freemarker.**

-keep,includedescriptorclasses class ezvcard.io.json.JCardModule { *; }
-keepclassmembers class ezvcard.io.json.JCardModule { *; }
-keep,includedescriptorclasses enum ezvcard.io.json.JCardModule { *; }
-keepclassmembers enum ezvcard.io.json.JCardModule { *; }
-keep,includedescriptorclasses interface ezvcard.io.json.JCardModule { *; }
-keepclassmembers interface ezvcard.io.json.JCardModule { *; }

-keep,includedescriptorclasses class com.journeyapps.barcodescanner.** { *; }
-keepclassmembers class com.journeyapps.barcodescanner.** { *; }

-dontwarn kotlin.reflect.jvm.internal.impl.descriptors.CallableDescriptor
-dontwarn kotlin.reflect.jvm.internal.impl.descriptors.ClassDescriptor
-dontwarn kotlin.reflect.jvm.internal.impl.descriptors.ClassifierDescriptorWithTypeParameters
-dontwarn kotlin.reflect.jvm.internal.impl.descriptors.annotations.AnnotationDescriptor
-dontwarn kotlin.reflect.jvm.internal.impl.descriptors.impl.PropertyDescriptorImpl
-dontwarn kotlin.reflect.jvm.internal.impl.load.java.JavaClassFinder
-dontwarn kotlin.reflect.jvm.internal.impl.resolve.OverridingUtil
-dontwarn kotlin.reflect.jvm.internal.impl.types.DescriptorSubstitutor
-dontwarn kotlin.reflect.jvm.internal.impl.types.DescriptorSubstitutor
-dontwarn kotlin.reflect.jvm.internal.impl.types.TypeConstructor
