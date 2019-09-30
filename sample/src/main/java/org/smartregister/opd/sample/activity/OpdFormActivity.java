package org.smartregister.opd.sample.activity;


import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.smartregister.opd.activity.BaseOpdFormActivity;
import org.smartregister.opd.fragment.OpdFormFragment;

public class OpdFormActivity extends BaseOpdFormActivity {

    private OpdFormFragment opdFormFragment;

    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    protected void initializeFormFragmentCore() {
        opdFormFragment = OpdFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, opdFormFragment).commit();
    }
}