package app.template.patches.canva

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// getWatermarked()Z — present in proto DTOs that flag a media file as watermarked.
// Forcing these to return false strips the watermark from rendered exports/previews.

val VideoFile2WatermarkedFingerprint = Fingerprint(
    definingClass = "Lcom/canva/video/dto/VideoProto\$VideoFile2;",
    name = "getWatermarked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val DashVideoFileWatermarkedFingerprint = Fingerprint(
    definingClass = "Lcom/canva/video/dto/VideoProto\$DashFile\$DashVideoFile;",
    name = "getWatermarked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val ImageFileReferenceWatermarkedFingerprint = Fingerprint(
    definingClass = "Lcom/canva/document/dto/DocumentBaseProto\$ImageFileReference;",
    name = "getWatermarked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val VideoFileReferenceWatermarkedFingerprint = Fingerprint(
    definingClass = "Lcom/canva/document/dto/DocumentBaseProto\$VideoFileReference;",
    name = "getWatermarked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MediaFileWatermarkedFingerprint = Fingerprint(
    definingClass = "Lcom/canva/media/dto/MediaProto\$MediaFile;",
    name = "getWatermarked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val DashVideoFileReferenceWatermarkedFingerprint = Fingerprint(
    definingClass = "Lcom/canva/document/dto/DocumentBaseProto\$DashFileReference\$DashVideoFileReference;",
    name = "getWatermarked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// getRestrictedAccess()Z — gates "restricted/locked" UI on premium media.
val VideoRestrictedAccessFingerprint = Fingerprint(
    definingClass = "Lcom/canva/video/dto/VideoProto\$Video;",
    name = "getRestrictedAccess",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val MediaRestrictedAccessFingerprint = Fingerprint(
    definingClass = "Lcom/canva/media/dto/MediaProto\$Media;",
    name = "getRestrictedAccess",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)