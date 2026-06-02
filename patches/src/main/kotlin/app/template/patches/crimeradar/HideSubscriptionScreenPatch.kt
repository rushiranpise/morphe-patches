package app.template.patches.crimeradar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CRIMERADAR_COMPATIBILITY

// SubscriptionOthersActivity.onCreate() is the entry point for the subscription/paywall screen.
// Patching onCreate() to call finish() immediately makes the Activity self-dismiss the instant
// Android creates it — the user sees no flash, no transition, nothing.
//
// Inheritance chain (confirmed via smali):
//   SubscriptionOthersActivity
//     └── com.particlemedia.infra.ui.r   (.super Lcom/particlemedia/infra/ui/p;)
//           └── com.particlemedia.infra.ui.p  (.super Lt/c;)
//                 └── t/c                        (.super Landroidx/fragment/app/p;)
//                       └── androidx.fragment.app.FragmentActivity
//
// The original onCreate() calls:
//   invoke-super {p0, p1}, Lcom/particlemedia/infra/ui/p;->onCreate(Landroid/os/Bundle;)V
//
// We must use the same direct superclass (com.particlemedia.infra.ui.r → .super is p).
// Using Landroid/app/Activity; directly would bypass the entire AppCompat/Fragment
// initialization chain and risk SuperNotCalledException or missing lifecycle setup.
//
// After the super call we immediately call finish() so the paywall screen never renders.

private const val SUBSCRIPTION_ACTIVITY_CLASS =
    "Lcom/particlemedia/feature/subscription/SubscriptionOthersActivity;"

// Direct superclass of SubscriptionOthersActivity confirmed from .super in its smali.
private const val ACTIVITY_SUPER_CLASS =
    "Lcom/particlemedia/infra/ui/p;"

object SubscriptionOthersOnCreateFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_ACTIVITY_CLASS,
    name = "onCreate"
)

@Suppress("unused")
val hideSubscriptionScreenPatch = bytecodePatch(
    name = "Hide Subscription Screen",
    description = "Hide subscription screens in-app."
) {
    compatibleWith(CRIMERADAR_COMPATIBILITY)

    execute {
        val method = SubscriptionOthersOnCreateFingerprint
            .match(classDefBy(SUBSCRIPTION_ACTIVITY_CLASS))
            .method

        method.removeInstructions(0, method.instructions.count())
        method.addInstructions(
            0,
            """
                invoke-super {p0, p1}, ${ACTIVITY_SUPER_CLASS}->onCreate(Landroid/os/Bundle;)V
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
            """.trimIndent()
        )
    }
}