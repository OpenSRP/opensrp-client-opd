package org.smartregister.opd.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.R;
import org.smartregister.opd.dao.OpdDiagnosisAndTreatmentFormDao;
import org.smartregister.opd.fragment.BaseOpdFormFragment;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.utils.AppExecutors;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.util.Utils;

import java.util.HashMap;
import java.util.Set;

import timber.log.Timber;

public class BaseOpdFormActivity extends JsonWizardFormActivity {

    private BaseOpdFormFragment opdFormFragment;
    private boolean enableOnCloseDialog = true;
    private JSONObject form;

    private HashMap<String, String> parcelableData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            form = new JSONObject(currentJsonState());
        } catch (JSONException e) {
            Timber.e(e);
        }

        enableOnCloseDialog = getIntent().getBooleanExtra(OpdConstants.FormActivity.EnableOnCloseDialog, true);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            Set<String> keySet = extras.keySet();

            for (String key : keySet) {
                if (!key.equals(OpdConstants.JSON_FORM_EXTRA.JSON)) {
                    Object objectValue = extras.get(key);

                    if (objectValue instanceof String) {
                        String value = (String) objectValue;
                        parcelableData.put(key, value);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            String encounterType = form.getString(OpdJsonFormUtils.ENCOUNTER_TYPE);
            confirmCloseTitle = getString(R.string.confirm_form_close);
            confirmCloseMessage = encounterType.trim().toLowerCase().contains("update") ? this.getString(R.string.any_changes_you_make) : this.getString(R.string.confirm_form_close_explanation);
            setConfirmCloseTitle(confirmCloseTitle);
            setConfirmCloseMessage(confirmCloseMessage);

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    protected void initializeFormFragmentCore() {
        opdFormFragment = (BaseOpdFormFragment) BaseOpdFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction().add(R.id.container, opdFormFragment).commit();
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (toolbar != null) {
            toolbar.setContentInsetStartWithNavigation(0);
        }
        super.setSupportActionBar(toolbar);
    }

    /**
     * Conditionally display the confirmation dialog
     */
    @Override
    public void onBackPressed() {
        if (enableOnCloseDialog) {
            if (form.optString(OpdJsonFormUtils.ENCOUNTER_TYPE).equals(OpdConstants.EventType.DIAGNOSIS_AND_TREAT)) {
                AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppThemeAlertDialog)
                        .setTitle(getString(R.string.opd_exit_form))
                        .setCancelable(true)
                        .setMessage(getString(R.string.save_form_fill_session))
                        .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveFormFillSession();
                                BaseOpdFormActivity.this.finish();
                            }
                        }).setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //clear
                                clearSavedSession();
                                BaseOpdFormActivity.this.finish();
                            }
                        }).setNeutralButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();

                dialog.show();
            } else {
                super.onBackPressed();
            }

        } else {
            BaseOpdFormActivity.this.finish();
        }
    }

    protected void clearSavedSession() {
        final OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm = new OpdDiagnosisAndTreatmentForm(OpdUtils.getIntentValue(getIntent(), OpdConstants.IntentKey.BASE_ENTITY_ID));
        final OpdDiagnosisAndTreatmentFormDao opdDiagnosisAndTreatmentFormDao = OpdLibrary.getInstance().getOpdDiagnosisAndTreatmentFormRepository();
        new AppExecutors().diskIO().execute(() -> opdDiagnosisAndTreatmentFormDao.delete(opdDiagnosisAndTreatmentForm));
    }

    protected void saveFormFillSession() {
        JSONObject jsonObject = getmJSONObject();
        final OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm = new OpdDiagnosisAndTreatmentForm(0, OpdUtils.getIntentValue(getIntent(), OpdConstants.IntentKey.BASE_ENTITY_ID),
                jsonObject.toString(), Utils.convertDateFormat(new DateTime()));
        final OpdDiagnosisAndTreatmentFormDao opdDiagnosisAndTreatmentFormDao = OpdLibrary.getInstance().getOpdDiagnosisAndTreatmentFormRepository();
        new AppExecutors().diskIO().execute(() -> opdDiagnosisAndTreatmentFormDao.saveOrUpdate(opdDiagnosisAndTreatmentForm));
    }

    @NonNull
    public HashMap<String, String> getParcelableData() {
        return parcelableData;
    }
}
