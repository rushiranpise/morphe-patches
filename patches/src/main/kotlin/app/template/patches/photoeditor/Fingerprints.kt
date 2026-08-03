package app.template.patches.photoeditor

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Targets the license cache check method inside the obfuscated SLC (Software License
// Cache) class. This class is responsible for reading the "License.no.advertisement"
// key from a SharedPreferences file named "license" and returning true if the user
// holds a valid license (premium + no ads).
//
// The class name shifts every update (v13.3: Lc81;, v13.4: Lj81;, v13.5: Lo61;)
// so definingClass/name are intentionally omitted. We rely on stable application-
// level string constants and the Java standard library call they feed into:
//
//   const-string v1, "license"                              ← SharedPrefs filename
//   invoke-virtual {...}, Context;->getSharedPreferences    ← stable API
//   const-string p0, "License.no.advertisement"            ← license key name (stable)
//   invoke-virtual {v0, p0}, HashMap;->get                 ← cache lookup gate
//
// Smali verified (v13.5, classes.dex, Lo61;->a(Landroid/content/Context;)Z):
//   .method public static a(Landroid/content/Context;)Z
//   sget-object v0, Lo61;->c:Ljava/util/HashMap;
//   monitor-enter v0
//   sget-wide v1, Lo61;->b:J
//   const-wide/16 v3, 0
//   cmp-long v1, v1, v3
//   const/4 v2, 0
//   if-gtz v1, :L2
//   const-string v1, "license"                             ← filter 1
//   invoke-virtual {p0,v1,v2}, Context;->getSharedPreferences(...)  ← filter 2
//   ...
//   const-string p0, "License.no.advertisement"            ← filter 3
//   invoke-virtual {v0, p0}, HashMap;->get(Object)Object;  ← filter 4
//   ...
//   return v2
val LicenseCacheCheckFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("license"),
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getSharedPreferences",
        ),
        string("License.no.advertisement"),
        methodCall(
            definingClass = "Ljava/util/HashMap;",
            name = "get",
        ),
    ),
)
