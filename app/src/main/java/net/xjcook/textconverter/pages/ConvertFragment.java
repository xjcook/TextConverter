package net.xjcook.textconverter.pages;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.xjcook.textconverter.ConvertUtility;
import net.xjcook.textconverter.R;

import java.io.IOException;

public class ConvertFragment extends Fragment {

    private ProgressBar mProgressBar;
    private TextView mInfoView;

    private Bundle mInBundle;
    private Bundle mOutBundle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_convert, container, false);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        mInfoView = (TextView) rootView.findViewById(R.id.infoText);

        mInBundle = getArguments().getBundle(InputFilePage.PAGE_TITLE);
        mOutBundle = getArguments().getBundle(OutputFilePage.PAGE_TITLE);

        if (mInBundle != null && mOutBundle != null) {
            new ConvertTask().execute();
        }

        return rootView;
    }

    private class ConvertTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Uri inUri = mInBundle.getParcelable(InputFilePage.URI_DATA_KEY);
            String inCharset = mInBundle.getString(InputFilePage.CHARSET_DATA_KEY);
            Uri outUri = mOutBundle.getParcelable(OutputFilePage.URI_DATA_KEY);
            String outCharset = mOutBundle.getString(OutputFilePage.CHARSET_DATA_KEY);

            try {
                ConvertUtility.convertText(getActivity(), inUri, inCharset, outUri, outCharset);
            } catch (IOException e) {
                Log.getStackTraceString(e);
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void res) {
            mProgressBar.setVisibility(ProgressBar.GONE);
            mInfoView.setText(getResources().getString(R.string.convert_success));
        }

        @Override
        protected void onCancelled() {
            mProgressBar.setVisibility(ProgressBar.GONE);
            mInfoView.setText(getResources().getString(R.string.convert_failed));
        }
    }

}
