package org.smartregister.opd.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vijay.jsonwizard.domain.Form;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.R;
import org.smartregister.opd.adapter.GroupedListableAdapter;
import org.smartregister.opd.adapter.ProfileHistoryAdapter;
import org.smartregister.opd.contract.OpdProfileFragmentContract;
import org.smartregister.opd.dao.VisitDao;
import org.smartregister.opd.domain.ProfileHistory;
import org.smartregister.opd.holders.GroupedListableViewHolder;
import org.smartregister.opd.listener.OnSendActionToFragment;
import org.smartregister.opd.presenter.NewOpdProfileVisitsFragmentPresenter;
import org.smartregister.opd.utils.FormProcessor;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.view.fragment.BaseListFragment;

import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;

public class NewOpdProfileVisitsFragment extends BaseListFragment<ProfileHistory> implements OnSendActionToFragment, OpdProfileFragmentContract.View<ProfileHistory>, FormProcessor.Requester {

    private String baseEntityID;

    public static NewOpdProfileVisitsFragment newInstance(Bundle bundle) {
        NewOpdProfileVisitsFragment fragment = new NewOpdProfileVisitsFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            CommonPersonObjectClient commonPersonObjectClient = (CommonPersonObjectClient) getArguments()
                    .getSerializable(OpdConstants.IntentKey.CLIENT_OBJECT);

            if (commonPersonObjectClient != null) {
                baseEntityID = commonPersonObjectClient.getCaseId();
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @NotNull
    @Override
    protected Callable<List<ProfileHistory>> onStartCallable(@Nullable @org.jetbrains.annotations.Nullable Bundle bundle) {
        return () -> VisitDao.getVisitHistory(baseEntityID);
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
    public void onListItemClicked(ProfileHistory profileHistory, int layoutID) {
        if (layoutID != R.id.tv_edit) return;

        String formName;
        switch (profileHistory.getEventType()) {
            case OpdConstants.OpdModuleEvents.OPD_CHECK_IN:
                formName = OpdConstants.JsonForm.OPD_CHECKIN;
                break;
            case OpdConstants.OpdModuleEvents.OPD_VITAL_DANGER_SIGNS_CHECK:
                formName = OpdConstants.JsonForm.VITAL_DANGER_SIGNS;
                break;
            case OpdConstants.OpdModuleEvents.OPD_DIAGNOSIS:
                formName = OpdConstants.JsonForm.DIAGNOSIS;
                break;
            case OpdConstants.OpdModuleEvents.OPD_TREATMENT:
                formName = OpdConstants.JsonForm.TREATMENT;
                break;
            case OpdConstants.OpdModuleEvents.OPD_LABORATORY:
                formName = OpdConstants.JsonForm.LAB_RESULTS;
                break;
            case OpdConstants.OpdModuleEvents.OPD_PHARMACY:
                formName = OpdConstants.JsonForm.PHARMACY;
                break;
            case OpdConstants.OpdModuleEvents.OPD_FINAL_OUTCOME:
                formName = OpdConstants.JsonForm.FINAL_OUTCOME;
                break;
            case OpdConstants.OpdModuleEvents.OPD_SERVICE_CHARGE:
                formName = OpdConstants.JsonForm.SERVICE_FEE;
                break;
            default:
                throw new IllegalArgumentException("Unknown Form");
        }

        loadPresenter().openForm(getContext(), formName, baseEntityID, profileHistory.getID());
    }

    @Override
    public void onFormProcessingResult(String jsonForm) {
        loadPresenter().saveForm(jsonForm, getContext());
    }

    public FormProcessor.Host getHostFormProcessor() {
        return (FormProcessor.Host) getActivity();
    }

    @Override
    public void onFetchError(Exception e) {
        Toast.makeText(getContext(), "An error occurred. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Timber.e(e);
    }

    @NonNull
    @Override
    public GroupedListableAdapter<ProfileHistory, GroupedListableViewHolder<ProfileHistory>> adapter() {
        return new ProfileHistoryAdapter(list, this);
    }

    @Override
    public void onActionReceive() {
        //DO NOTHING
    }

    @NonNull
    @NotNull
    @Override
    public NewOpdProfileVisitsFragmentPresenter loadPresenter() {
        if (presenter == null) {
            presenter = new NewOpdProfileVisitsFragmentPresenter()
                    .with(this);
        }
        return (NewOpdProfileVisitsFragmentPresenter) presenter;
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
        loadPresenter().fetchList(this.onStartCallable(this.getArguments()), this.fetchRequestType());
    }
}
