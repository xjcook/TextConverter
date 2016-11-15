package net.xjcook.textconverter.pages;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tech.freak.wizardpager.model.ModelCallbacks;
import com.tech.freak.wizardpager.model.Page;

import net.xjcook.textconverter.R;

public class ConvertFragment extends ListFragment implements ModelCallbacks {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_convert, container, false);

        return rootView;
    }

    @Override
    public void onPageDataChanged(Page page) {

    }

    @Override
    public void onPageTreeChanged() {

    }
}
