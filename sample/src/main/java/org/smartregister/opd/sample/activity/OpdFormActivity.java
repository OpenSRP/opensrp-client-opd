package org.smartregister.opd.sample.activity;


import org.smartregister.client.utils.constants.JsonFormConstants;
import org.smartregister.opd.activity.BaseOpdFormActivity;
import org.smartregister.opd.sample.fragment.OpdFormFragment;

public class OpdFormActivity extends BaseOpdFormActivity {

    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    protected void initializeFormFragmentCore() {
        OpdFormFragment opdFormFragment = OpdFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, opdFormFragment).commit();
    }
}
