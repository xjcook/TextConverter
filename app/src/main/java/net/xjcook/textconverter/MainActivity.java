package net.xjcook.textconverter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST_CODE = 43;

    private Spinner inEncodingSpn;
    private Spinner outEncodingSpn;

    private EditText previewText;

    private String buffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewText = (EditText) findViewById(R.id.previewText);

        // Populate spinners
        inEncodingSpn = (Spinner) findViewById(R.id.inEncodingSpn);
        outEncodingSpn = (Spinner) findViewById(R.id.outEncodingSpn);

        Map<String, Charset> charsetMap = Charset.availableCharsets();
        String[] charsetNames = charsetMap.keySet().toArray(new String[charsetMap.size()]);

        ArrayAdapter spinAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, charsetNames);

        inEncodingSpn.setAdapter(spinAdapter);
        outEncodingSpn.setAdapter(spinAdapter);

        inEncodingSpn.setSelection(spinAdapter.getPosition("windows-1250"));
        outEncodingSpn.setSelection(spinAdapter.getPosition("UTF-8"));

        // Set buttons
        Button inFileBtn = (Button) findViewById(R.id.inFileBtn);
        Button outFileBtn = (Button) findViewById(R.id.outFileBtn);

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
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, WRITE_REQUEST_CODE);
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
                Uri uri = resultData.getData();
                String charset = (String) inEncodingSpn.getSelectedItem();
                Log.i(TAG, "Uri: " + uri.toString() + " Charset: " + charset);

                try {
                    buffer = readTextFromUri(uri, charset);
                    previewText.setText(buffer);
                } catch (IOException e) {
                    Log.getStackTraceString(e);
                }
            }
        }

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                String charset = (String) outEncodingSpn.getSelectedItem();
                Log.i(TAG, "Uri: " + uri.toString() + " Charset: " + charset);

                try {
                    writeTextToUri(uri, charset, buffer);
                } catch (IOException e) {
                    Log.getStackTraceString(e);
                }
            }
        }
    }

    private String readTextFromUri(Uri uri, String charset) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        inputStream.close();
        reader.close();
        return stringBuilder.toString();
    }

    private void writeTextToUri(Uri uri, String charset, String buffer) throws IOException {
        OutputStream outputStream = getContentResolver().openOutputStream(uri);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset));

        writer.write(buffer);

        outputStream.close();
        writer.close();
    }
}
