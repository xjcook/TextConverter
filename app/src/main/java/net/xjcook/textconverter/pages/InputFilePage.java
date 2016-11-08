package net.xjcook.textconverter.pages;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.tech.freak.wizardpager.model.ModelCallbacks;
import com.tech.freak.wizardpager.model.Page;
import com.tech.freak.wizardpager.model.ReviewItem;

import java.util.ArrayList;


/**
 * A page asking for a filename and an encoding.
 */
public class InputFilePage extends Page {
    public static final String DEFAULT_ENCODING = "windows-1250";
    public static final String CHARSET_PREF_KEY = "inCharset";
    public static final int REQUEST_CODE = 42;

    public static final String FILENAME_DATA_KEY = "filename";
    public static final String CHARSET_DATA_KEY = "charset";

    public InputFilePage(ModelCallbacks callbacks, String title) {
        super(callbacks, title);
    }

    @Override
    public Fragment createFragment() {
        return ChooseFileFragment.create(getKey(), CHARSET_PREF_KEY, DEFAULT_ENCODING, REQUEST_CODE);
    }

    @Override
    public void getReviewItems(ArrayList<ReviewItem> dest) {
//        dest.add(new ReviewItem("Your name", mData.getString(NAME_DATA_KEY), getKey(), -1));
//        dest.add(new ReviewItem("Your email", mData.getString(EMAIL_DATA_KEY), getKey(), -1));
    }

    @Override
    public boolean isCompleted() {
//        return !TextUtils.isEmpty(mData.getString(NAME_DATA_KEY));
        return true;
    }
}
