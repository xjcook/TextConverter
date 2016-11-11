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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import net.xjcook.textconverter.ConvertUtility;
import net.xjcook.textconverter.R;

import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.ui.PageFragmentCallbacks;

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

    private TextView mNameView;
    private TextView mEmailView;
    private Spinner mSpinner;
    private Button mButton;

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

        final Bundle args = getArguments();
        final SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        // Populate mSpinner
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner);

        Map<String, Charset> charsetMap = Charset.availableCharsets();
        String[] charsetNames = charsetMap.keySet().toArray(new String[charsetMap.size()]);

        ArrayAdapter spinAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, charsetNames);
        mSpinner.setAdapter(spinAdapter);
        mSpinner.setSelection(spinAdapter.getPosition(
                settings.getString(args.getString(ARG_CHARSET_PREF_KEY), args.getString(ARG_DEFAULT_ENCODE))));

        // Set button
        mButton = (Button) rootView.findViewById(R.id.fileBtn);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        // In a future update to the support library, this should override setUserVisibleHint
        // instead of setMenuVisibility.
        if (mNameView != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            if (!menuVisible) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == InputFilePage.REQUEST_CODE || requestCode == OutputFilePage.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(PREFS_NAME, 0).edit();
                Uri uri = resultData.getData();
                String filename = ConvertUtility.getFileNameFromUri(getActivity(), uri);
                mButton.setText(filename);

                if (requestCode == InputFilePage.REQUEST_CODE) {
                    mPage.getData().putParcelable(InputFilePage.URI_DATA_KEY, uri);
                    mPage.getData().putString(InputFilePage.FILENAME_DATA_KEY, filename);
                    editor.putString(InputFilePage.FILENAME_DATA_KEY, filename);

                } else if (requestCode == OutputFilePage.REQUEST_CODE) {
                    mPage.getData().putParcelable(OutputFilePage.URI_DATA_KEY, uri);
                    mPage.getData().putString(OutputFilePage.FILENAME_DATA_KEY, filename);
                    editor.putString(OutputFilePage.FILENAME_DATA_KEY, filename);
                }

                mPage.notifyDataChanged();
                editor.commit();
            }
        }
    }

}
