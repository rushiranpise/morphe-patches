package app.template.extension.extension;

import app.template.extension.BuildConfig;

final class ApiKeys {
    // Flightradar24 keys its map to a user-supplied key (see UseMapsApiKeyPatch),
    // so it stays on its own dedicated key rather than the shared one.
    static final String FLIGHTRADAR_MAPS = BuildConfig.FLIGHTRADAR_MAPS_API_KEY;
    static final String SHARED_MAPS = BuildConfig.SHARED_MAPS_API_KEY;

    private ApiKeys() {
    }
}
