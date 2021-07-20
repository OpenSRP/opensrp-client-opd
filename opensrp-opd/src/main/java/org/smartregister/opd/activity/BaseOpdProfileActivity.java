package org.smartregister.opd.activity;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.R;
import org.smartregister.opd.adapter.ViewPagerAdapter;
import org.smartregister.opd.configuration.OpdRegisterSwitcher;
import org.smartregister.opd.contract.OpdProfileActivityContract;
import org.smartregister.opd.fragment.NewOpdProfileOverviewFragment;
import org.smartregister.opd.fragment.NewOpdProfileVisitsFragment;
import org.smartregister.opd.fragment.OnViewStateChanged;
import org.smartregister.opd.fragment.OpdProfileOverviewFragment;
import org.smartregister.opd.fragment.OpdProfileVisitsFragment;
import org.smartregister.opd.listener.OnSendActionToFragment;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.opd.presenter.OpdProfileActivityPresenter;
import org.smartregister.opd.utils.ConfigurationInstancesHelper;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.view.activity.BaseProfileActivity;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-27
 */

public class BaseOpdProfileActivity extends BaseProfileActivity implements OpdProfileActivityContract.View {

    private TextView nameView;
    private TextView ageView;
    private TextView genderView;
    private TextView ancIdView;
    private ImageView imageView;
    private String baseEntityId;
    private OnSendActionToFragment sendActionListenerForProfileOverview;
    private OnSendActionToFragment sendActionListenerToVisitsFragment;

    private CommonPersonObjectClient commonPersonObjectClient;
    private Button switchRegBtn;
    private String registerType;

    @Override
    protected void initializePresenter() {
        presenter = new OpdProfileActivityPresenter(this);
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        ageView = findViewById(R.id.textview_detail_two);
        genderView = findViewById(R.id.textview_detail_three);
        ancIdView = findViewById(R.id.textview_detail_one);
        nameView = findViewById(R.id.textview_name);
        imageView = findViewById(R.id.imageview_profile);
        switchRegBtn = findViewById(R.id.btn_opdActivityBaseProfile_switchRegView);

        setTitle("");
    }

    public void setSendActionListenerForProfileOverview(OnSendActionToFragment sendActionListenerForProfileOverview) {
        this.sendActionListenerForProfileOverview = sendActionListenerForProfileOverview;
    }

    public void setSendActionListenerToVisitsFragment(OnSendActionToFragment sendActionListenerToVisitsFragment) {
        this.sendActionListenerToVisitsFragment = sendActionListenerToVisitsFragment;
    }

    @Override
    protected void fetchProfileData() {
        CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) getIntent()
                .getSerializableExtra(OpdConstants.IntentKey.CLIENT_OBJECT);
        ((OpdProfileActivityPresenter) presenter).refreshProfileTopSection(commonPersonObjectClient.getColumnmaps());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        // When user click home menu item then quit this activity.
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.opd_menu_item_close_client) {
            openCloseForm();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void openCloseForm() {
        if (commonPersonObjectClient != null) {
            ((OpdProfileActivityPresenter) presenter).startForm(OpdConstants.Form.OPD_CLOSE, commonPersonObjectClient);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_opd_profile_activity, menu);
        MenuItem closeMenu = menu.findItem(R.id.opd_menu_item_close_client);
        if (closeMenu != null && OpdConstants.RegisterType.OPD.equalsIgnoreCase(getRegisterType())) {
            closeMenu.setEnabled(true);
        }
        return true;
    }

    private ViewPager setupLegacyViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        OpdProfileOverviewFragment profileOverviewFragment = OpdProfileOverviewFragment.newInstance(this.getIntent().getExtras());
        setSendActionListenerForProfileOverview(profileOverviewFragment);

        OpdProfileVisitsFragment profileVisitsFragment = OpdProfileVisitsFragment.newInstance(this.getIntent().getExtras());
        setSendActionListenerToVisitsFragment(profileVisitsFragment);

        adapter.addFragment(profileOverviewFragment, this.getString(R.string.overview));
        adapter.addFragment(profileVisitsFragment, this.getString(R.string.visits));

        viewPager.setAdapter(adapter);
        return viewPager;
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        commonPersonObjectClient = (CommonPersonObjectClient) getIntent()
                .getSerializableExtra(OpdConstants.IntentKey.CLIENT_OBJECT);
        baseEntityId = commonPersonObjectClient.getCaseId();
        ((OpdProfileActivityPresenter) presenter).refreshProfileTopSection(commonPersonObjectClient.getColumnmaps());

        // Enable switcher
        configureRegisterSwitcher();

        // Disable the registration info button if the client is not in OPD
        if (commonPersonObjectClient != null) {
            registerType = commonPersonObjectClient.getDetails().get(OpdConstants.ColumnMapKey.REGISTER_TYPE);
            View view = findViewById(R.id.btn_profile_registration_info);
            view.setEnabled(OpdConstants.RegisterType.OPD.equalsIgnoreCase(registerType));
        }
    }

    private void configureRegisterSwitcher() {
        Class<? extends OpdRegisterSwitcher> opdRegisterSwitcherClass = OpdLibrary.getInstance().getOpdConfiguration().getOpdRegisterSwitcher();
        if (opdRegisterSwitcherClass != null) {
            final OpdRegisterSwitcher opdRegisterSwitcher = ConfigurationInstancesHelper.newInstance(opdRegisterSwitcherClass);

            switchRegBtn.setVisibility(opdRegisterSwitcher.showRegisterSwitcher(commonPersonObjectClient) ? View.VISIBLE : View.GONE);
            switchRegBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    opdRegisterSwitcher.switchFromOpdRegister(commonPersonObjectClient, BaseOpdProfileActivity.this);
                }
            });
        } else {
            switchRegBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy(isChangingConfigurations());

    }

    @Override
    public void setProfileName(@NonNull String fullName) {
        this.patientName = fullName;
        nameView.setText(fullName);
    }

    @Override
    public void setProfileID(@NonNull String registerId) {
        ancIdView.setText(String.format(getString(R.string.id_detail), registerId));
    }

    @Override
    public void setProfileAge(@NonNull String age) {
        genderView.setText(String.format(getString(R.string.age_details), age));

    }

    @Override
    public void setProfileGender(@NonNull String gender) {
        ageView.setText(gender);
    }

    @Override
    public void setProfileImage(@NonNull String baseEntityId, int defaultImage) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView, defaultImage);
    }

    @Override
    public void openDiagnoseAndTreatForm() {
        if (commonPersonObjectClient != null) {
            ((OpdProfileActivityPresenter) presenter).startForm(OpdConstants.Form.OPD_DIAGNOSIS_AND_TREAT, commonPersonObjectClient);
        }
    }

    public OnSendActionToFragment getActionListenerForVisitFragment() {
        return sendActionListenerToVisitsFragment;
    }

    public OnSendActionToFragment getActionListenerForProfileOverview() {
        return sendActionListenerForProfileOverview;
    }


    @Override
    public void openCheckInForm() {
        if (commonPersonObjectClient != null) {
            ((OpdProfileActivityPresenter) presenter).startForm(OpdConstants.Form.OPD_CHECK_IN, commonPersonObjectClient);
        }
    }

    @Override
    public void startFormActivity(@NonNull JSONObject form, @NonNull HashMap<String, String> intentKeys) {
        Intent intent = OpdUtils.buildFormActivityIntent(form, intentKeys, this);
        startActivityForResult(intent, OpdJsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OpdJsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(OpdConstants.JSON_FORM_EXTRA.JSON);
                Timber.d("JSON-Result : %s", jsonString);

                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(OpdJsonFormUtils.ENCOUNTER_TYPE);

                switch (encounterType) {
                    case OpdConstants.EventType.CHECK_IN:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((OpdProfileActivityPresenter) presenter).saveVisitOrDiagnosisForm(encounterType, data);
                        break;
                    case OpdConstants.EventType.DIAGNOSIS_AND_TREAT:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((OpdProfileActivityPresenter) presenter).saveVisitOrDiagnosisForm(encounterType, data);
                        break;
                    case OpdConstants.EventType.UPDATE_OPD_REGISTRATION:
                        showProgressDialog(R.string.saving_dialog_title);

                        RegisterParams registerParam = new RegisterParams();
                        registerParam.setEditMode(true);
                        registerParam.setFormTag(OpdJsonFormUtils.formTag(OpdUtils.context().allSharedPreferences()));
                        showProgressDialog(R.string.saving_dialog_title);

                        ((OpdProfileActivityPresenter) presenter).saveUpdateRegistrationForm(jsonString, registerParam);
                        break;
                    case OpdConstants.EventType.OPD_CLOSE:
                        showProgressDialog(R.string.saving_dialog_title);
                        ((OpdProfileActivityPresenter) this.presenter).saveCloseForm(encounterType, data);
                        break;
                    default:
                        break;
                }

            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.opd_activity_base_profile);
    }

    @Override
    public void onClick(View view) {
        String register_type = commonPersonObjectClient.getDetails().get(OpdConstants.ColumnMapKey.REGISTER_TYPE);
        if (view.getId() == R.id.btn_profile_registration_info) {
            if (OpdConstants.RegisterType.OPD.equalsIgnoreCase(register_type)) {
                if (presenter instanceof OpdProfileActivityContract.Presenter) {
                    ((OpdProfileActivityContract.Presenter) presenter).onUpdateRegistrationBtnCLicked(baseEntityId);
                }
            } else {
                showToast(getString(R.string.edit_opd_registration_failure_message));
            }
        } else {
            super.onClick(view);
        }
    }

    @NonNull
    @Override
    public Context getContext() {
        return this;
    }

    @Nullable
    @Override
    public CommonPersonObjectClient getClient() {
        return commonPersonObjectClient;
    }

    @Override
    public void setClient(@NonNull CommonPersonObjectClient client) {
        this.commonPersonObjectClient = client;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public ViewPager setupNewOpdModuleViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        NewOpdProfileOverviewFragment profileOverviewFragment = NewOpdProfileOverviewFragment.newInstance(this.getIntent().getExtras());
        setSendActionListenerForProfileOverview(profileOverviewFragment);

        NewOpdProfileVisitsFragment profileVisitsFragment = NewOpdProfileVisitsFragment.newInstance(this.getIntent().getExtras());
        setSendActionListenerToVisitsFragment(profileVisitsFragment);

        adapter.addFragment(profileOverviewFragment, this.getString(R.string.today));
        adapter.addFragment(profileVisitsFragment, this.getString(R.string.history));

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // do nothing
            }

            @Override
            public void onPageSelected(int position) {
                Fragment visible = adapter.getItem(position);
                if(visible instanceof OnViewStateChanged)
                    ((OnViewStateChanged) visible).onViewVisible();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // do nothing
            }
        });
        return viewPager;
    }


    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        if (OpdLibrary.getInstance().getProperties().hasProperty(OpdConstants.PropertyConstants.USE_NEW_OPD_MODULE)) {
            return setupNewOpdModuleViewPager(viewPager);
        }
        return setupLegacyViewPager(viewPager);
    }


}