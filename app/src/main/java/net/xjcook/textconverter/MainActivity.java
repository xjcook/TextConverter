package net.xjcook.textconverter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private static final String DEFAULT_IN_ENCODING = "windows-1250";
    private static final String DEFAULT_OUT_ENCODING = "UTF-8";

    private static final int BUFFER_SIZE = 8192;
    private static final int PREVIEW_LINES = 20;
    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST_CODE = 43;

    private Spinner inEncodingSpn;
    private Spinner outEncodingSpn;
    private Button inFileBtn;
    private Button outFileBtn;
    private TextView previewText;

    private Uri inUri;
    private Uri outUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewText = (TextView) findViewById(R.id.previewText);

        // Populate spinners
        inEncodingSpn = (Spinner) findViewById(R.id.inEncodingSpn);
        outEncodingSpn = (Spinner) findViewById(R.id.outEncodingSpn);

        Map<String, Charset> charsetMap = Charset.availableCharsets();
        String[] charsetNames = charsetMap.keySet().toArray(new String[charsetMap.size()]);

        ArrayAdapter spinAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, charsetNames);

        inEncodingSpn.setAdapter(spinAdapter);
        outEncodingSpn.setAdapter(spinAdapter);

        inEncodingSpn.setSelection(spinAdapter.getPosition(DEFAULT_IN_ENCODING));
        outEncodingSpn.setSelection(spinAdapter.getPosition(DEFAULT_OUT_ENCODING));

        // Set buttons
        inFileBtn = (Button) findViewById(R.id.inFileBtn);
        outFileBtn = (Button) findViewById(R.id.outFileBtn);

        inFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        outFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inUri != null) {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_TITLE, "enc_" + inFileBtn.getText());
                    startActivityForResult(intent, WRITE_REQUEST_CODE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                inUri = resultData.getData();
                String inCharset = (String) inEncodingSpn.getSelectedItem();

                try {
                    inFileBtn.setText(getFileNameFromUri(inUri));
                    previewText.setText(readTextFromUri(inUri, inCharset, PREVIEW_LINES));
                } catch (IOException e) {
                    Log.getStackTraceString(e);
                }
            }
        }

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                outUri = resultData.getData();
                String inCharset = (String) inEncodingSpn.getSelectedItem();
                String outCharset = (String) outEncodingSpn.getSelectedItem();

                try {
                    outFileBtn.setText(getFileNameFromUri(outUri));
                    convertText(inUri, inCharset, outUri, outCharset);
                    Toast.makeText(this, R.string.convert_success, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.getStackTraceString(e);
                }

                clean();
            }
        }
    }

    private String readTextFromUri(Uri uri, String charset, int maxLines) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        int current = 0;
        while ((line = reader.readLine()) != null) {
            if (current < maxLines) {
                stringBuilder.append(line);
                stringBuilder.append(System.getProperty("line.separator"));
                current++;
            }
        }

        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    private void convertText(Uri inUri, String inCharset, Uri outUri, String outCharset) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(inUri);
        OutputStream outputStream = getContentResolver().openOutputStream(outUri);

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

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }

        return fileName;
    }

    private void clean() {
        inUri = null;
        outUri = null;
        inFileBtn.setText(R.string.choose_file);
        outFileBtn.setText(R.string.save_as);
        previewText.setText(R.string.preview);
    }
}
