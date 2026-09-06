package app.template.patches.moviebox.tv

// All targets verified against smali for com.community.mbox.tv v1.1.10.0901.03
// No fingerprint objects — TV patch uses mutableClassDefByOrNull directly.
// VIP singleton: z.a()Z (v1.1.8) → c0.a()Z (v1.1.10), accessed via TvServiceLocator.k0()Z.
