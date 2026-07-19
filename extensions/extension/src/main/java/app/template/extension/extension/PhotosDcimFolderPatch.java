package app.template.extension.extension;

import java.lang.reflect.Field;
import java.util.Locale;

@SuppressWarnings("unused")
public final class PhotosDcimFolderPatch {
    private PhotosDcimFolderPatch() {}

    public static boolean fixInCameraFolder(Object builder, boolean inCameraFolder) {
        if (!inCameraFolder || builder == null) {
            return inCameraFolder;
        }

        String filePath = findDcimPath(builder);
        if (filePath == null) {
            return inCameraFolder;
        }

        return isCameraFolderPath(filePath);
    }

    public static boolean isCameraFolderPath(String filePath) {
        if (filePath == null) {
            return false;
        }

        String normalized = filePath.toLowerCase(Locale.US);
        return normalized.contains("/dcim/camera/") || normalized.endsWith("/dcim/camera");
    }

    private static String findDcimPath(Object builder) {
        Class<?> current = builder.getClass();
        while (current != null) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(builder);
                    String path = readPath(value);
                    if (path != null) {
                        return path;
                    }
                } catch (Throwable ignored) {}
            }
            current = current.getSuperclass();
        }

        return null;
    }

    private static String readPath(Object value) {
        if (value instanceof String) {
            String path = (String) value;
            return containsDcim(path) ? path : null;
        }

        if (value == null || !value.getClass().getName().contains("Optional")) {
            return null;
        }

        try {
            Object present = value.getClass().getMethod("isPresent").invoke(value);
            if (!Boolean.TRUE.equals(present)) {
                return null;
            }

            Object item = value.getClass().getMethod("get").invoke(value);
            if (item instanceof String && containsDcim((String) item)) {
                return (String) item;
            }
        } catch (Throwable ignored) {}

        return null;
    }

    private static boolean containsDcim(String path) {
        return path != null && path.toLowerCase(Locale.US).contains("/dcim/");
    }
}
