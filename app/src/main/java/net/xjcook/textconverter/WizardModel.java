package net.xjcook.textconverter;

import android.content.Context;

import com.tech.freak.wizardpager.model.AbstractWizardModel;
import com.tech.freak.wizardpager.model.PageList;

import net.xjcook.textconverter.pages.InputFilePage;
import net.xjcook.textconverter.pages.OutputFilePage;

public class WizardModel extends AbstractWizardModel {
    public WizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList() {
        return new PageList(
                new InputFilePage(this, InputFilePage.PAGE_TITLE).setRequired(true),
                new OutputFilePage(this, OutputFilePage.PAGE_TITLE).setRequired(true)
        );
    }
}
