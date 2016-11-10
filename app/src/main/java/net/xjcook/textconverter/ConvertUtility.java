package net.xjcook.textconverter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class ConvertUtility {

    private static final int BUFFER_SIZE = 8192;
    private static final int PREVIEW_LINES = 25;

    public static String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }

        return fileName;
    }

    public static String readTextFromUri(Context context, Uri uri, String charset) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        int current = 0;
        while ((line = reader.readLine()) != null) {
            if (current < PREVIEW_LINES) {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
                current++;
            }
        }

        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    public static void convertText(Context context, Uri inUri, String inCharset, Uri outUri,
                             String outCharset) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(inUri);
        OutputStream outputStream = context.getContentResolver().openOutputStream(outUri);

        Reader reader = new InputStreamReader(inputStream, inCharset);
        Writer writer = new OutputStreamWriter(outputStream, outCharset);

        char[] buffer = new char[BUFFER_SIZE];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, read);
        }

        writer.close();
        reader.close();
        outputStream.close();
        inputStream.close();
    }

}
