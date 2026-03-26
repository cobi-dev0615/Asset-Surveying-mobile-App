# Moshi
-keep class com.seretail.inventarios.data.remote.dto.** { *; }
-keepclassmembers class com.seretail.inventarios.data.remote.dto.** { *; }

# Room entities
-keep class com.seretail.inventarios.data.local.entity.** { *; }

# RT501 UHF RFID SDK
-keep class com.lckj.lcrrgxmodule.** { *; }
-keep class com.rfid.** { *; }
-keep class com.gg.reader.** { *; }
-keep class android.bld.** { *; }
-keep class cn.pda.serialport.** { *; }
