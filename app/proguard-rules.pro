# Room - keep entity classes and DAOs
-keep class com.notifiq.core.database.entity.** { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.* { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.**
{ kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.notifiq.core.model.**$serializer
{ *; }
-keepclassmembers class com.notifiq.core.model.** { *** Companion; }
-keepclasseswithmembers class com.notifiq.core.model.**
{ kotlinx.serialization.KSerializer serializer(...); }

# Domain models - keep all fields for serialization
-keep class com.notifiq.core.model.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }

# Vico charts (if using)
-keep class com.patrykandpatrick.vico.** { *; }

# General Android
-keepattributes Signature
-keepattributes *Annotation*
-keep class * extends android.app.Application { *; }
-keep class * extends android.app.Activity { *; }
-keep class * extends android.app.Service { *; }
-keep class * extends android.content.BroadcastReceiver { *; }