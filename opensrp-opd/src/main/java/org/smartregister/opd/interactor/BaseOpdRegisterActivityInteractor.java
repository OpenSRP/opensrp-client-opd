package org.smartregister.opd.interactor;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.opd.utils.AppExecutors;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.contract.ConfigurableRegisterActivityContract;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-13
 */

public class BaseOpdRegisterActivityInteractor implements OpdRegisterActivityContract.Interactor {

    protected AppExecutors appExecutors;

    public BaseOpdRegisterActivityInteractor() {
        this(new AppExecutors());
    }

    @VisibleForTesting
    BaseOpdRegisterActivityInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    @Override
    public void fetchSavedDiagnosisAndTreatmentForm(final @NonNull String baseEntityId, final @Nullable String entityTable, @NonNull final OpdRegisterActivityContract.InteractorCallBack interactorCallBack) {
        appExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final OpdDiagnosisAndTreatmentForm diagnosisAndTreatmentForm = OpdLibrary
                        .getInstance()
                        .getOpdDiagnosisAndTreatmentFormRepository()
                        .findOne(new OpdDiagnosisAndTreatmentForm(baseEntityId));

                appExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        interactorCallBack.onFetchedSavedDiagnosisAndTreatmentForm(diagnosisAndTreatmentForm, baseEntityId, entityTable);
                    }
                });
            }
        });
    }

    @Override
    public void getNextUniqueId(final Triple<String, String, String> triple, final OpdRegisterActivityContract.InteractorCallBack callBack) {
        // Do nothing for now, this will be handled by the class that extends this
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        // Do nothing for now, this will be handled by the class that extends this to nullify the presenter
    }

    @Override
    public void getNextUniqueId(Triple<String, String, String> triple, ConfigurableRegisterActivityContract.InteractorCallBack interactorCallBack) {
        // Abstract method implementation
    }

    @Override
    public void saveRegistration(@Nullable HashMap<Client, List<Event>> hashMap, String s, @NonNull org.smartregister.view.contract.RegisterParams registerParams, ConfigurableRegisterActivityContract.InteractorCallBack interactorCallBack) {
        // Abstract method implementation
    }

    @Override
    public void saveEvents(@NonNull List<Event> list, @NonNull ConfigurableRegisterActivityContract.InteractorCallBack interactorCallBack) {
        // Abstract method implementation
    }

    @Override
    public void saveRegistration(List<OpdEventClient> opdEventClientList, String jsonString, RegisterParams registerParams, OpdRegisterActivityContract.InteractorCallBack callBack) {
        // Do nothing for now, this will be handled by the class that extends this
    }

    @Override
    public void saveEvents(@NonNull final List<Event> events, @NonNull final OpdRegisterActivityContract.InteractorCallBack callBack) {
        Runnable runnable = () -> {
            List<String> formSubmissionsIds = new ArrayList<>();
            for (Event event : events) {
                formSubmissionsIds.add(event.getFormSubmissionId());
                saveEventInDb(event);
            }
            processLatestUnprocessedEvents(formSubmissionsIds);

            appExecutors.mainThread().execute(() -> callBack.onEventSaved());
        };

        appExecutors.diskIO().execute(runnable);
    }


    private void saveEventInDb(@NonNull Event event) {
        try {
            CoreLibrary.getInstance()
                    .context()
                    .getEventClientRepository()
                    .addEvent(event.getBaseEntityId()
                            , new JSONObject(JsonFormUtils.gson.toJson(event)));
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private void processLatestUnprocessedEvents(@NonNull List<String> formSubmissionsIds) {
        // Process this event
        try {
            getClientProcessorForJava().processClient(getSyncHelper().getEvents(formSubmissionsIds));
            getAllSharedPreferences().saveLastUpdatedAtDate(new Date().getTime());
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @NonNull
    public ECSyncHelper getSyncHelper() {
        return OpdLibrary.getInstance().getEcSyncHelper();
    }

    @NonNull
    public AllSharedPreferences getAllSharedPreferences() {
        return OpdLibrary.getInstance().context().allSharedPreferences();
    }

    @NonNull
    public ClientProcessorForJava getClientProcessorForJava() {
        return DrishtiApplication.getInstance().getClientProcessor();
    }

    @NonNull
    public UniqueIdRepository getUniqueIdRepository() {
        return OpdLibrary.getInstance().getUniqueIdRepository();
    }

}