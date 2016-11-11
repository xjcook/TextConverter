package net.xjcook.textconverter.pages;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.tech.freak.wizardpager.model.ModelCallbacks;
import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.model.ReviewItem;

import java.util.ArrayList;


/**
 * A page asking for a filename and an encoding.
 */
public class InputFilePage extends Page {

    public static final String PAGE_TITLE = "Input file";
    public static final String DEFAULT_ENCODING = "windows-1250";

    public static final int REQUEST_CODE = 42;
    public static final String URI_DATA_KEY = "inUri";
    public static final String FILENAME_DATA_KEY = "inFileName";
    public static final String CHARSET_DATA_KEY = "inCharset";

    public InputFilePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return ChooseFileFragment.create(getKey(), CHARSET_DATA_KEY, DEFAULT_ENCODING, REQUEST_CODE);
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Input file", mData.getString(FILENAME_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Input encoding", mData.getString(CHARSET_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(FILENAME_DATA_KEY));
    }

}
