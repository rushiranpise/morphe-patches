package app.template.patches.depthlive

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Layer 1: Category.isPremium() — "free" string gate
val IsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/jndapp/depth/live/wallpaper/model/Category;",
    strings = listOf("free"),
    returnType = "Z",
    parameters = listOf()
)

// Layer 2: PairIP LicenseClient static init(Context)
val LicenseClientFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;")
)

// Layer 3: IsPremiumOwned — ()Z with premium_lifetime + Set.contains (l40.e equivalent)
val IsPremiumOwnedFingerprint = Fingerprint(
    strings = listOf("premium_lifetime"),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        methodCall(name = "contains", returnType = "Z")
    )
)

// Layer 4: PairIP LicenseActivity kill-switch
val LicenseActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// Layer 5: PremiumSetter — ()V with premium_lifetime + Set.contains + Boolean.valueOf (l40.j)
val PremiumSetterFingerprint = Fingerprint(
    strings = listOf("premium_lifetime"),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(name = "contains", returnType = "Z"),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "valueOf",
            parameters = listOf("Z"),
            returnType = "Ljava/lang/Boolean;"
        )
    )
)
