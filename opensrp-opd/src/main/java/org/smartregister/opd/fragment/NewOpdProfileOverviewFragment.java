package org.smartregister.opd.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.R;
import org.smartregister.opd.adapter.ProfileActionFragmentAdapter;
import org.smartregister.opd.contract.OpdProfileFragmentContract;
import org.smartregister.opd.dao.VisitDao;
import org.smartregister.opd.domain.ProfileAction;
import org.smartregister.opd.listener.OnSendActionToFragment;
import org.smartregister.opd.presenter.NewOpdProfileOverviewFragmentPresenter;
import org.smartregister.opd.utils.FormProcessor;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.util.AppExecutors;
import org.smartregister.view.adapter.ListableAdapter;
import org.smartregister.view.fragment.BaseListFragment;
import org.smartregister.view.viewholder.ListableViewHolder;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import timber.log.Timber;

public class NewOpdProfileOverviewFragment extends BaseListFragment<ProfileAction> implements OnSendActionToFragment, OpdProfileFragmentContract.View<ProfileAction>, FormProcessor.Requester , OnViewStateChanged {

    private String baseEntityID;
    private CommonPersonObjectClient commonPersonObjectClient;
    private final Map<String, String> formGlobalValues = new HashMap<>();
    private final Set<String> globalKeys = new HashSet<>();

    public static NewOpdProfileOverviewFragment newInstance(Bundle bundle) {
        NewOpdProfileOverviewFragment fragment = new NewOpdProfileOverviewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            commonPersonObjectClient = (CommonPersonObjectClient) getArguments()
                    .getSerializable(OpdConstants.IntentKey.CLIENT_OBJECT);

            if (commonPersonObjectClient != null) {
                baseEntityID = commonPersonObjectClient.getCaseId();
            }
        }
        getGlobalKeysFromConfig();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    protected Callable<List<ProfileAction>> onStartCallable(@Nullable Bundle bundle) {
        return () -> {

            Map<String, List<ProfileAction.ProfileActionVisit>> mapVisit = VisitDao.getVisitsToday(baseEntityID);

            loadGlobals(mapVisit);

            List<ProfileAction> profileActions = new ArrayList<>();
            profileActions.add(
                    new ProfileAction(getString(R.string.opd_check_in), 0)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_CHECK_IN))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.vital_danger_signs), 1)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_VITAL_DANGER_SIGNS_CHECK))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.opd_diagnosis), 2)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_DIAGNOSIS))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.lab_reports), 3)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_LABORATORY))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.opd_treatment), 4)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_TREATMENT))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.pharmacy), 5)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_PHARMACY))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.final_outcome), 6)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_FINAL_OUTCOME))
            );
            profileActions.add(
                    new ProfileAction(getString(R.string.service_fee), 7)
                            .setVisits(mapVisit.get(OpdConstants.OpdModuleEventConstants.OPD_SERVICE_CHARGE))
            );
            return profileActions;
        };
    }

    protected void loadGlobals(Map<String, List<ProfileAction.ProfileActionVisit>> mapVisit) {
        if (mapVisit.size() > 0) {
            HashMap<String, String> savedValues = new HashMap<>();

            for (String key : mapVisit.keySet()) {
                String visitId = mapVisit.get(key).get(0).getVisitID();
                Map<String, String> values = VisitDao.getSavedKeysForVisit(visitId);
                savedValues.putAll(values);
            }

            formGlobalValues.clear();
            for (String globalKey : globalKeys) {
                if (savedValues.containsKey(globalKey)) {
                    if (globalKey.equalsIgnoreCase(OpdConstants.JSON_FORM_KEY.MEDICINE) && !savedValues.get(globalKey).isEmpty()) {
                        String medidcineString = OpdJsonFormUtils.getMedicineNoteString(savedValues.get(globalKey));
                        formGlobalValues.put(globalKey, medidcineString);
                    } else {
                        formGlobalValues.put(globalKey, savedValues.get(globalKey));
                    }
                } else {
                    formGlobalValues.put(globalKey, "");
                }
            }
            if (savedValues.containsKey(OpdConstants.REPEATING_GROUP_MAP)) {
                String testResults = OpdJsonFormUtils.getLabResultsStringFromMap(savedValues);
                formGlobalValues.put("diagnostic_test_lab_results", testResults);
            }
        }
    }

    @Override
    protected AppExecutors.Request fetchRequestType() {
        return AppExecutors.Request.DISK_THREAD;
    }

    @Override
    protected int getRootLayout() {
        return R.layout.new_opd_fragment_profile_overview;
    }

    @Override
    protected int getRecyclerViewID() {
        return R.id.opd_profile_overview_recycler;
    }

    @Override
    protected int getProgressBarID() {
        return R.id.progress_bar;
    }

    @Override
    public void onListItemClicked(ProfileAction report, int layoutID) {

        String eventId = (report.getSelectedAction() != null) ? report.getSelectedAction().getVisitID() : null;

        String formName;
        switch (report.getKey()) {
            case 0:
                formName = OpdConstants.JsonForm.OPD_CHECKIN;
                break;
            case 1:
                formName = OpdConstants.JsonForm.VITAL_DANGER_SIGNS;
                break;
            case 2:
                formName = OpdConstants.JsonForm.DIAGNOSIS;
                break;
            case 3:
                formName = OpdConstants.JsonForm.LAB_RESULTS;
                break;
            case 4:
                formName = OpdConstants.JsonForm.TREATMENT;
                break;
            case 5:
                formName = OpdConstants.JsonForm.PHARMACY;
                break;
            case 6:
                formName = OpdConstants.JsonForm.FINAL_OUTCOME;
                break;
            case 7:
                formName = OpdConstants.JsonForm.SERVICE_FEE;
                break;
            default:
                throw new IllegalArgumentException("Unknown Form");
        }
        loadPresenter().openForm(getContext(), formName, baseEntityID, eventId);
    }

    @Override
    public void onFormProcessingResult(String jsonForm) {
        loadPresenter().saveForm(jsonForm, getContext());
    }

    public FormProcessor.Host getHostFormProcessor() {
        return (FormProcessor.Host) getActivity();
    }

    @Override
    public void onFetchError(Exception ex) {
        Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
        Timber.e(ex);
    }

    @NonNull
    @Override
    public ListableAdapter<ProfileAction, ListableViewHolder<ProfileAction>> adapter() {
        return new ProfileActionFragmentAdapter(list, this);
    }

    @Override
    public void onActionReceive() {
        //DO Nothing
    }

    @NonNull
    @NotNull
    @Override
    public NewOpdProfileOverviewFragmentPresenter loadPresenter() {
        if (presenter == null) {
            presenter = new NewOpdProfileOverviewFragmentPresenter()
                    .with(this);
        }
        return (NewOpdProfileOverviewFragmentPresenter) presenter;
    }

    @Override
    public void startJsonForm(JSONObject jsonObject) {
        Form form = new Form();
        form.setWizard(false);
        form.setHideSaveLabel(true);
        form.setNextLabel("");

        getHostFormProcessor().startForm(jsonObject, form, this);
    }

    @Override
    public void reloadFromSource() {
        // reload this list from the database
        loadPresenter().fetchList(this.onStartCallable(this.getArguments()), this.fetchRequestType());
    }

    @Override
    public CommonPersonObjectClient getCommonPersonObject() {
        return this.commonPersonObjectClient;
    }

    @Override
    public void attachGlobals(JSONObject jsonObject, String formSubmissionId) {
        try {
            jsonObject.put(JsonFormConstants.JSON_FORM_KEY.GLOBAL, new JSONObject(formGlobalValues));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void getGlobalKeysFromConfig() {
        try {
            Iterable<Object> opdGlobals = OpdLibrary.getInstance().readYaml(OpdConstants.FileUtils.OPD_GLOBALS, new Yaml());
            for (Object ruleObject : opdGlobals) {
                Map<String, Object> map = ((Map<String, Object>) ruleObject);
                globalKeys.addAll((List<String>) map.get(JsonFormConstants.FIELDS));
            }
        } catch (Exception e) {
            Timber.e(e);

        }
    }

    @Override
    public void onViewVisible() {
        reloadFromSource();
    }
}
