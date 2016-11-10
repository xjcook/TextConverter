package net.xjcook.textconverter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tech.freak.wizardpager.model.AbstractWizardModel;
import com.tech.freak.wizardpager.model.ModelCallbacks;
import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.ui.PageFragmentCallbacks;
import com.tech.freak.wizardpager.ui.ReviewFragment;
import com.tech.freak.wizardpager.ui.StepPagerStrip;

import net.xjcook.textconverter.pages.InputFilePage;
import net.xjcook.textconverter.pages.OutputFilePage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements
        PageFragmentCallbacks, ReviewFragment.Callbacks, ModelCallbacks {

    private static final String LOG_TAG = "MainActivity";
    public static final String PREFS_NAME = "TextConverterPrefs";
    private static final String DEFAULT_IN_ENCODING = "windows-1250";
    private static final String DEFAULT_OUT_ENCODING = "UTF-8";

    private static final int BUFFER_SIZE = 8192;
    private static final int PREVIEW_LINES = 25;
    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST_CODE = 43;

    private Spinner inEncodingSpn;
    private Spinner outEncodingSpn;
    private Button inFileBtn;
    private Button outFileBtn;
    private TextView previewText;

    private Uri inUri;
    private Uri outUri;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;

    private boolean mEditingAfterReview;

    private AbstractWizardModel mWizardModel = new WizardModel(this);

    private boolean mConsumePageSelectedEvent;

    private Button mNextButton;
    private Button mPrevButton;

    private List<Page> mCurrentPageSequence;
    private StepPagerStrip mStepPagerStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mWizardModel.load(savedInstanceState.getBundle("model"));
        }

        mWizardModel.registerListener(this);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip
                .setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
                    @Override
                    public void onPageStripSelected(int position) {
                        position = Math.min(mPagerAdapter.getCount() - 1,
                                position);
                        if (mPager.getCurrentItem() != position) {
                            mPager.setCurrentItem(position);
                        }
                    }
                });

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (mConsumePageSelectedEvent) {
                    mConsumePageSelectedEvent = false;
                    return;
                }

                mEditingAfterReview = false;
                updateBottomBar();
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPager.getCurrentItem() == mCurrentPageSequence.size()) {
                    DialogFragment dg = new DialogFragment() {
                        @Override
                        public Dialog onCreateDialog(Bundle savedInstanceState) {
                            return new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.submit_confirm_message)
                                    .setPositiveButton(
                                            R.string.submit_confirm_button,
                                            null)
                                    .setNegativeButton(android.R.string.cancel,
                                            null).create();
                        }
                    };
                    dg.show(getSupportFragmentManager(), "place_order_dialog");
                } else {
                    if (mEditingAfterReview) {
                        mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
                    } else {
                        mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                    }
                }
            }
        });

        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        });

        onPageTreeChanged();
        updateBottomBar();

//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
//        previewText = (TextView) findViewById(R.id.previewText);
//
//        // Populate spinners
//        inEncodingSpn = (Spinner) findViewById(R.id.inEncodingSpn);
//        outEncodingSpn = (Spinner) findViewById(R.id.outEncodingSpn);
//
//        Map<String, Charset> charsetMap = Charset.availableCharsets();
//        String[] charsetNames = charsetMap.keySet().toArray(new String[charsetMap.size()]);
//
//        ArrayAdapter spinAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, charsetNames);
//        inEncodingSpn.setAdapter(spinAdapter);
//        outEncodingSpn.setAdapter(spinAdapter);
//
//        inEncodingSpn.setSelection(
//                spinAdapter.getPosition(settings.getString("inCharset", DEFAULT_IN_ENCODING)));
//        outEncodingSpn.setSelection(
//                spinAdapter.getPosition(settings.getString("outCharset", DEFAULT_OUT_ENCODING)));
//
//        // Set buttons
//        inFileBtn = (Button) findViewById(R.id.inFileBtn);
//        outFileBtn = (Button) findViewById(R.id.outFileBtn);
//
//        inFileBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//                startActivityForResult(intent, READ_REQUEST_CODE);
//            }
//        });
//
//        outFileBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (inUri != null) {
//                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    intent.putExtra(Intent.EXTRA_TITLE, "enc_" + inFileBtn.getText());
//                    startActivityForResult(intent, WRITE_REQUEST_CODE);
//                }
//            }
//        });
//
//        // Handle screen rotation
//        if (savedInstanceState != null) {
//            inUri = savedInstanceState.getParcelable("inUri");
//            outUri = savedInstanceState.getParcelable("outUri");
//            inFileBtn.setText(savedInstanceState.getString("inFileBtn"));
//            outFileBtn.setText(savedInstanceState.getString("outFileBtn"));
//            previewText.setText(savedInstanceState.getString("previewText"));
//        }
    }

    private void updateBottomBar() {
        int position = mPager.getCurrentItem();
        if (position == mCurrentPageSequence.size()) {
            mNextButton.setText(R.string.finish);
            mNextButton.setBackgroundResource(R.drawable.finish_background);
            mNextButton.setTextAppearance(this, R.style.TextAppearanceFinish);
        } else {
            mNextButton.setText(mEditingAfterReview ? R.string.review
                    : R.string.next);
            mNextButton
                    .setBackgroundResource(R.drawable.selectable_item_background);
            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.textAppearanceMedium, v,
                    true);
            mNextButton.setTextAppearance(this, v.resourceId);
            mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        }

        mPrevButton
                .setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();

//        // Save preferences
//        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
//        editor.putString("inCharset", (String) inEncodingSpn.getSelectedItem());
//        editor.putString("outCharset", (String) outEncodingSpn.getSelectedItem());
//
//        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWizardModel.unregisterListener(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("model", mWizardModel.save());

//        outState.putParcelable("inUri", inUri);
//        outState.putParcelable("outUri", outUri);
//        outState.putString("inFileBtn", (String) inFileBtn.getText());
//        outState.putString("outFileBtn", (String) outFileBtn.getText());
//        outState.putString("previewText", (String) previewText.getText());
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
//        Log.d(LOG_TAG, "onActivityResult");
//        if (requestCode == InputFilePage.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Log.d(LOG_TAG, "inputFile");
//            if (resultData != null) {
//                /*inUri = resultData.getData();
//                String inCharset = (String) inEncodingSpn.getSelectedItem();
//
//                try {
//                    inFileBtn.setText(getFileNameFromUri(inUri));
//                    previewText.setText(readTextFromUri(inUri, inCharset, PREVIEW_LINES));
//                } catch (IOException e) {
//                    Log.getStackTraceString(e);
//                }*/
//            }
//        }
//
//        if (requestCode == OutputFilePage.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Log.d(LOG_TAG, "outputFile");
//            if (resultData != null) {
//                /*outUri = resultData.getData();
//                String inCharset = (String) inEncodingSpn.getSelectedItem();
//                String outCharset = (String) outEncodingSpn.getSelectedItem();
//
//                try {
//                    outFileBtn.setText(getFileNameFromUri(outUri));
//                    convertText(inUri, inCharset, outUri, outCharset);
//                    Toast.makeText(this, R.string.convert_success, Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    Log.getStackTraceString(e);
//                }
//
//                clean();*/
//            }
//        }
//    }

    private void clean() {
//        inUri = null;
//        outUri = null;
//        inFileBtn.setText(R.string.choose_file);
//        outFileBtn.setText(R.string.save_as);
//        previewText.setText(R.string.preview);
    }

    @Override
    public void onPageDataChanged(Page page) {
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
                updateBottomBar();
            }
        }
    }

    @Override
    public void onPageTreeChanged() {
        mCurrentPageSequence = mWizardModel.getCurrentPageSequence();
        recalculateCutOffPage();
        mStepPagerStrip.setPageCount(mCurrentPageSequence.size() + 1); // + 1 =
        // review
        // step
        mPagerAdapter.notifyDataSetChanged();
        updateBottomBar();
    }

    @Override
    public Page onGetPage(String key) {
        return mWizardModel.findByKey(key);
    }

    @Override
    public AbstractWizardModel onGetModel() {
        return mWizardModel;
    }

    @Override
    public void onEditScreenAfterReview(String pageKey) {
        for (int i = mCurrentPageSequence.size() - 1; i >= 0; i--) {
            if (mCurrentPageSequence.get(i).getKey().equals(pageKey)) {
                mConsumePageSelectedEvent = true;
                mEditingAfterReview = true;
                mPager.setCurrentItem(i);
                updateBottomBar();
                break;
            }
        }
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mCurrentPageSequence.size() + 1;
        for (int i = 0; i < mCurrentPageSequence.size(); i++) {
            Page page = mCurrentPageSequence.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private int mCutOffPage;
        private Fragment mPrimaryItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (i >= mCurrentPageSequence.size()) {
                return new ReviewFragment();
            }

            return mCurrentPageSequence.get(i).createFragment();
        }

        @Override
        public int getItemPosition(Object object) {
            // TODO: be smarter about this
            if (object == mPrimaryItem) {
                // Re-use the current fragment (its position never changes)
                return POSITION_UNCHANGED;
            }

            return POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position,
                                   Object object) {
            super.setPrimaryItem(container, position, object);
            mPrimaryItem = (Fragment) object;
        }

        @Override
        public int getCount() {
            return Math.min(mCutOffPage + 1, mCurrentPageSequence == null ? 1
                    : mCurrentPageSequence.size() + 1);
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }
}
