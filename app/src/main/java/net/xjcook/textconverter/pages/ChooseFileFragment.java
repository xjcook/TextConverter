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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import net.xjcook.textconverter.ConvertUtility;
import net.xjcook.textconverter.R;

import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.ui.PageFragmentCallbacks;

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

//        mNameView = ((TextView) rootView.findViewById(R.id.your_name));
//        mNameView.setText(mPage.getData().getString(InputFilePage.NAME_DATA_KEY));
//
//        mEmailView = ((TextView) rootView.findViewById(R.id.your_email));
//        mEmailView.setText(mPage.getData().getString(InputFilePage.EMAIL_DATA_KEY));

        final Bundle args = getArguments();
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

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

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, args.getInt(ARG_REQUEST_CODE));
            }
        });

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

//        mNameView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
//                                          int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                mPage.getData().putString(InputFilePage.NAME_DATA_KEY,
//                        (editable != null) ? editable.toString() : null);
//                mPage.notifyDataChanged();
//            }
//        });
//
//        mEmailView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1,
//                                          int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                mPage.getData().putString(InputFilePage.EMAIL_DATA_KEY,
//                        (editable != null) ? editable.toString() : null);
//                mPage.notifyDataChanged();
//            }
//        });
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
        if (requestCode == InputFilePage.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                mButton.setText(ConvertUtility.getFileNameFromUri(getActivity(), uri));
            }
        }

        if (requestCode == OutputFilePage.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                mButton.setText(ConvertUtility.getFileNameFromUri(getActivity(), uri));
            }
        }
    }
}
