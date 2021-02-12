package org.smartregister.opd.contract;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.view.contract.BaseRegisterContract;
import org.smartregister.view.contract.ConfigurableRegisterActivityContract;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-13
 */

public interface OpdRegisterActivityContract {

    interface View extends BaseRegisterContract.View {

        OpdRegisterActivityContract.Presenter presenter();

        void startFormActivity(String formName, String entityId, String metaData, @Nullable HashMap<String, String> injectedFieldValues, @Nullable String clientTable);

        void startFormActivity(@NonNull JSONObject jsonForm, @Nullable HashMap<String, String> parcelableData);
    }

    interface Presenter extends BaseRegisterContract.Presenter {

        void saveLanguage(String language);

        void saveForm(String jsonString, @NonNull RegisterParams registerParams);

        void saveVisitOrDiagnosisForm(@NonNull String eventType, @Nullable Intent data);

        HashMap<String, String> getInjectedFields(@NonNull String formName, @NonNull String entityId);

        void startForm(String formName, String entityId, String metaData, String locationId, @Nullable HashMap<String, String> injectedFieldValues, @Nullable String entityTable);

        @NonNull
        OpdRegisterActivityContract.Interactor createInteractor();
    }

    interface Model {

        void registerViewConfigurations(List<String> viewIdentifiers);

        void unregisterViewConfiguration(List<String> viewIdentifiers);

        void saveLanguage(String language);

        String getLocationId(String locationName);

        List<OpdEventClient> processRegistration(String jsonString, FormTag formTag);

        @Nullable
        JSONObject getFormAsJson(String formName, String entityId,
                                 String currentLocationId) throws JSONException;

        @Nullable
        JSONObject getFormAsJson(String formName, String entityId,
                                 String currentLocationId, @Nullable HashMap<String, String> injectedValues) throws JSONException;

        String getInitials();

    }

    interface Interactor extends ConfigurableRegisterActivityContract.Interactor {

        void fetchSavedDiagnosisAndTreatmentForm(@NonNull String baseEntityId, @Nullable String entityTable, @NonNull InteractorCallBack interactorCallBack);

        void getNextUniqueId(Triple<String, String, String> triple, OpdRegisterActivityContract.InteractorCallBack callBack);

        void saveRegistration(List<OpdEventClient> opdEventClientList, String jsonString, RegisterParams registerParams, OpdRegisterActivityContract.InteractorCallBack callBack);

        void saveEvents(@NonNull List<Event> events, @NonNull InteractorCallBack callBack);
    }

    interface InteractorCallBack {

        void onNoUniqueId();

        void onUniqueIdFetched(Triple<String, String, String> triple, String entityId);

        void onRegistrationSaved(boolean isEdit);

        void onEventSaved();

        void onFetchedSavedDiagnosisAndTreatmentForm(@Nullable OpdDiagnosisAndTreatmentForm diagnosisAndTreatmentForm, @NonNull String caseId, @Nullable String entityTable);

    }
}