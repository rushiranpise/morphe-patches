package app.template.extension.extension;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;

import java.io.File;
import java.io.FileNotFoundException;

@SuppressWarnings("unused")
public final class InternalDataDocumentsProvider extends DocumentsProvider {
    private static final String ROOT_ID = "internal-data-root";
    private static final String ROOT_DOCUMENT_ID = "/";

    private File baseDir() throws FileNotFoundException {
        File files = getContext() == null ? null : getContext().getFilesDir();
        File base = files == null ? null : files.getParentFile();
        if (base == null) throw new FileNotFoundException("Internal data directory unavailable");
        return base;
    }

    private File resolve(String documentId) throws FileNotFoundException {
        File base = baseDir();
        File file = ROOT_DOCUMENT_ID.equals(documentId) ? base : new File(base, documentId);
        String basePath = base.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (!filePath.equals(basePath) && !filePath.startsWith(basePath + File.separator)) {
            throw new FileNotFoundException(documentId);
        }
        return file;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        MatrixCursor cursor = new MatrixCursor(projectionOrDefault(projection, new String[] {
                DocumentsContract.Root.COLUMN_ROOT_ID,
                DocumentsContract.Root.COLUMN_DOCUMENT_ID,
                DocumentsContract.Root.COLUMN_TITLE,
                DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.COLUMN_MIME_TYPES
        }));
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID);
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_DOCUMENT_ID);
        row.add(DocumentsContract.Root.COLUMN_TITLE, "Internal data");
        row.add(DocumentsContract.Root.COLUMN_FLAGS,
                DocumentsContract.Root.FLAG_SUPPORTS_CREATE |
                        DocumentsContract.Root.FLAG_SUPPORTS_SEARCH |
                        DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD);
        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "*/*");
        return cursor;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        MatrixCursor cursor = new MatrixCursor(projectionOrDefault(projection, documentProjection()));
        includeFile(cursor, resolve(documentId), documentId);
        return cursor;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        MatrixCursor cursor = new MatrixCursor(projectionOrDefault(projection, documentProjection()));
        File parent = resolve(parentDocumentId);
        File[] files = parent.listFiles();
        if (files == null) return cursor;
        for (File file : files) includeFile(cursor, file, toDocumentId(file));
        return cursor;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        int accessMode = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(resolve(documentId), accessMode);
    }

    private void includeFile(MatrixCursor cursor, File file, String documentId) {
        MatrixCursor.RowBuilder row = cursor.newRow();
        row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, documentId);
        row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.getName().isEmpty() ? "data" : file.getName());
        row.add(DocumentsContract.Document.COLUMN_SIZE, file.isFile() ? file.length() : 0);
        row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified());
        row.add(DocumentsContract.Document.COLUMN_MIME_TYPE,
                file.isDirectory() ? DocumentsContract.Document.MIME_TYPE_DIR : "application/octet-stream");
        row.add(DocumentsContract.Document.COLUMN_FLAGS,
                DocumentsContract.Document.FLAG_SUPPORTS_WRITE |
                        DocumentsContract.Document.FLAG_SUPPORTS_DELETE |
                        DocumentsContract.Document.FLAG_SUPPORTS_RENAME);
    }

    private String toDocumentId(File file) throws FileNotFoundException {
        String basePath = baseDir().getAbsolutePath();
        String path = file.getAbsolutePath();
        if (path.equals(basePath)) return ROOT_DOCUMENT_ID;
        return path.substring(basePath.length() + 1);
    }

    private static String[] projectionOrDefault(String[] projection, String[] fallback) {
        return projection == null ? fallback : projection;
    }

    private static String[] documentProjection() {
        return new String[] {
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_SIZE,
                DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
                DocumentsContract.Document.COLUMN_FLAGS
        };
    }
}
