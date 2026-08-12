package app.template.patches.messenger.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── MXY.BdH — media transcoding operation (classes9/X/MXY) ─────────────────
// MXY is the media upload/transcoding service operation class.
// BdH(LX/3ky;)OperationResult is the method that performs the actual transcoding
// decision and dispatch: it reads operation parameters, checks "should_transcode"
// and "transcode" flags, then runs the transcoder pipeline.
//
// Returning OperationResult.A00 (the default static instance, success=false) at
// index 0 causes the transcoding step to be skipped — the media is sent at its
// original quality and resolution without compression.
//
// Fingerprint anchors (all stable strings inside BdH):
//   "transcode"          — operation type string
//   "should_transcode"   — flag key
//   "transcoded_video_larger" — analytics event logged after transcoding
//
// Method: public BdH(LX/3ky;)Lcom/facebook/fbservice/service/OperationResult;
//   (LX/3ky; is an obfuscated OperationContext-like type → use "L")
//
// Verified: classes9/X/MXY.smali, line 363.
// Verified against com.facebook.orca 573.0.0.44.88.
internal val DisableMediaTranscodingFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Lcom/facebook/fbservice/service/OperationResult;",
    parameters = listOf("L"), // LX/3ky; — obfuscated
    strings = listOf("transcode", "should_transcode", "transcoded_video_larger"),
)
