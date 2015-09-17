package net.xjcook.textconverter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.nio.charset.Charset;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Populate spinners
        Spinner inEncodingSpn = (Spinner) findViewById(R.id.inEncodingSpn);
        Spinner outEncodingSpn = (Spinner) findViewById(R.id.outEncodingSpn);

        Map<String, Charset> charsetMap = Charset.availableCharsets();
        String[] charsetNames = charsetMap.keySet().toArray(new String[charsetMap.size()]);

        ArrayAdapter spinAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, charsetNames);

        inEncodingSpn.setAdapter(spinAdapter);
        outEncodingSpn.setAdapter(spinAdapter);

        inEncodingSpn.setSelection(spinAdapter.getPosition("windows-1250"));
        outEncodingSpn.setSelection(spinAdapter.getPosition("UTF-8"));
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
}
