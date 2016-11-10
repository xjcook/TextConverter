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
public class OutputFilePage extends Page {
    public static final int REQUEST_CODE = 43;
    public static final String DEFAULT_ENCODING = "UTF-8";

    public static final String URI_DATA_KEY = "outUri";
    public static final String FILENAME_DATA_KEY = "outFileName";
    public static final String CHARSET_DATA_KEY = "outCharset";

    public OutputFilePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return ChooseFileFragment.create(getKey(), CHARSET_DATA_KEY, DEFAULT_ENCODING, REQUEST_CODE);
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
        dest.add(new ReviewItem("Output file", mData.getString(FILENAME_DATA_KEY), getKey(), -1));
        dest.add(new ReviewItem("Output encoding", mData.getString(CHARSET_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
        return !TextUtils.isEmpty(mData.getString(FILENAME_DATA_KEY));
    }
}
