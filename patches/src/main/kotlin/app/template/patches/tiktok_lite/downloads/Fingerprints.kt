package app.template.patches.tiktok_lite.downloads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// X/5mk.LD(Aweme)Z -- ACL code gate. Returns true only when code==0.
// Fingerprinted by stable iget-object on AwemeACLShare->downloadGeneral then
// iget on ACLCommonShare->code in sequence.
internal object DownloadAllowedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/ss/android/ugc/aweme/feed/model/Aweme;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lcom/ss/android/ugc/aweme/feed/model/AwemeACLShare;",
            name = "downloadGeneral",
        ),
        fieldAccess(
            opcode = Opcode.IGET,
            definingClass = "Lcom/ss/android/ugc/aweme/feed/model/ACLCommonShare;",
            name = "code",
        ),
    ),
)

// X/7ZV.LB(Aweme)X/8k3 -- download params builder.
// Reads Video->downloadAddr via iget-object directly (not via getter).
// Patched to swap Video->downloadAddr with newDownloadAddr at entry when non-null,
// so all downstream iget-object reads in this method get the clean URL.
// Fingerprinted by: fieldAccess on Aweme->video then Video->downloadAddr in sequence.
// .registers 7, enough scratch registers for the swap injection.
internal object VideoGetDownloadAddrFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
            name = "video",
        ),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Video;",
            name = "downloadAddr",
        ),
    ),
    custom = { _, classDef -> classDef.type == "LX/7ZV;" },
)

// X/5mk.LCI(Aweme)Z -- video status gate.
// .registers 3 (p0=Aweme, v0=AwemeStatus, v1=boolean scratch).
// Only (Aweme)Z method in X/5mk with registerCount==3.
// No parameters list: custom alone is sufficient and avoids parameters+custom issues.
internal object VideoStatusGateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type.endsWith("/5mk;") &&
            method.parameterTypes.size == 1 &&
            method.parameterTypes[0] == "Lcom/ss/android/ugc/aweme/feed/model/Aweme;" &&
            (method.implementation?.registerCount ?: 0) == 3
    },
)

// X/5mk.LFFFF(Aweme)Z -- music copyright gate.
// Unique: only PUBLIC STATIC FINAL (Aweme)Z method in X/5mk whose first
// iget-object reads Aweme->music:Music (vs LF/LFFL which read Aweme->status).
// Use fieldAccess filter only (no parameters list to avoid filters+params conflict).
// custom narrows to X/5mk class.
internal object MusicCopyrightGateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
            name = "music",
        ),
    ),
    custom = { _, classDef -> classDef.type.endsWith("/5mk;") },
)

// X/7hr.L(Aweme, I, X/8Xi)String -- transcode URL selector (photo mode).
// Unique: PUBLIC STATIC, String return, 3 params where [0]=Aweme, [1]=I, [2]=obfuscated obj.
internal object DownloadTranscodeFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/String;",
    custom = { method, _ ->
        method.parameterTypes.size == 3 &&
            method.parameterTypes[0] == "Lcom/ss/android/ugc/aweme/feed/model/Aweme;" &&
            method.parameterTypes[1] == "I" &&
            method.parameterTypes[2].startsWith("L")
    },
)
