package net.xjcook.textconverter.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.ui.PageFragmentCallbacks;

import net.xjcook.textconverter.ConvertUtility;
import net.xjcook.textconverter.R;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static net.xjcook.textconverter.MainActivity.PREFS_NAME;

public class ChooseFileFragment extends Fragment {
    private static final String LOG_TAG = "ChooseFileFragment";

    private static final String ARG_KEY = "key";
    private static final String ARG_CHARSET_PREF_KEY = "charsetPrefKey";
    private static final String ARG_DEFAULT_ENCODE = "defaultEncode";
    private static final String ARG_REQUEST_CODE = "requestCode";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private Page mPage;
    private Uri mUri;

    private Spinner mSpinner;
    private Button mButton;
    private TextView mPreviewLabel;
    private TextView mPreviewView;

    public static ChooseFileFragment create(String key, String prefKey, String defaultEncode,
                                            int requestCode) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        args.putString(ARG_CHARSET_PREF_KEY, prefKey);
        args.putString(ARG_DEFAULT_ENCODE, defaultEncode);
        args.putInt(ARG_REQUEST_CODE, requestCode);

        ChooseFileFragment fragment = new ChooseFileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ChooseFileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = mCallbacks.onGetPage(mKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_page_file, container, false);
        ((TextView) rootView.findViewById(android.R.id.title)).setText(mPage.getTitle());

        Bundle args = getArguments();
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        mButton = (Button) rootView.findViewById(R.id.fileBtn);
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner);
        mPreviewLabel = (TextView) rootView.findViewById(R.id.previewLabel);
        mPreviewView = (TextView) rootView.findViewById(R.id.previewText);

        // Populate charset spinner
        Map<String, Charset> charsetMap = Charset.availableCharsets();
        String[] charsetNames = charsetMap.keySet().toArray(new String[charsetMap.size()]);

        ArrayAdapter spinAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, charsetNames);
        mSpinner.setAdapter(spinAdapter);
        mSpinner.setSelection(spinAdapter.getPosition(
                settings.getString(args.getString(ARG_CHARSET_PREF_KEY), args.getString(ARG_DEFAULT_ENCODE))));

        if (args.getInt(ARG_REQUEST_CODE) == OutputFilePage.REQUEST_CODE) {
            mButton.setText(getResources().getString(R.string.save_as));
        }

        // Handle screen rotation
        if (savedInstanceState != null) {
            mUri = savedInstanceState.getParcelable("uri");
            mButton.setText(savedInstanceState.getString("fileBtn"));

            if (args.getInt(ARG_REQUEST_CODE) == InputFilePage.REQUEST_CODE) {
                mPreviewLabel.setVisibility(TextView.VISIBLE);
                mPreviewView.setText(savedInstanceState.getString("previewText"));
                mPreviewView.setVisibility(TextView.VISIBLE);
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(context instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("uri", mUri);
        outState.putString("fileBtn", (String) mButton.getText());
        outState.putString("previewText", (String) mPreviewView.getText());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
                int requestCode = getArguments().getInt(ARG_REQUEST_CODE);

                if (requestCode == InputFilePage.REQUEST_CODE) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, requestCode);

                } else if (requestCode == OutputFilePage.REQUEST_CODE) {
                    String filename = settings.getString(InputFilePage.FILENAME_DATA_KEY, null);
                    if (filename != null) {
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_TITLE, "enc_" + filename);
                        startActivityForResult(intent, requestCode);
                    }
                }
            }
        });

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Bundle args = getArguments();
                String charset = (String) mSpinner.getSelectedItem();

                mPage.getData().putString(args.getString(ARG_CHARSET_PREF_KEY), charset);
                mPage.notifyDataChanged();

                SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, 0).edit();
                editor.putString(args.getString(ARG_CHARSET_PREF_KEY), charset);
                editor.commit();

                if (mUri != null) {
                    try {
                        mPreviewView.setText(ConvertUtility.readTextFromUri(getActivity(), mUri, charset));
                    } catch (IOException e) {
                        Log.getStackTraceString(e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == InputFilePage.REQUEST_CODE || requestCode == OutputFilePage.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, 0).edit();
                mUri = resultData.getData();
                String filename = ConvertUtility.getFileNameFromUri(getActivity(), mUri);
                String charset = (String) mSpinner.getSelectedItem();
                mButton.setText(filename);

                if (requestCode == InputFilePage.REQUEST_CODE) {
                    mPage.getData().putParcelable(InputFilePage.URI_DATA_KEY, mUri);
                    mPage.getData().putString(InputFilePage.FILENAME_DATA_KEY, filename);
                    editor.putString(InputFilePage.FILENAME_DATA_KEY, filename);

                    // Show preview
                    try {
                        mPreviewLabel.setVisibility(TextView.VISIBLE);
                        mPreviewView.setText(ConvertUtility.readTextFromUri(getActivity(), mUri, charset));
                        mPreviewView.setVisibility(TextView.VISIBLE);
                    } catch (IOException e) {
                        Log.getStackTraceString(e);
                    }

                } else if (requestCode == OutputFilePage.REQUEST_CODE) {
                    mPage.getData().putParcelable(OutputFilePage.URI_DATA_KEY, mUri);
                    mPage.getData().putString(OutputFilePage.FILENAME_DATA_KEY, filename);
                    editor.putString(OutputFilePage.FILENAME_DATA_KEY, filename);
                }

                mPage.notifyDataChanged();
                editor.commit();
            }
        }
    }

}
