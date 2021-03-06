package org.smartregister.opd.contract;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.opd.listener.OnSendActionToFragment;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.pojo.OpdEventClient;
import org.smartregister.opd.pojo.RegisterParams;
import org.smartregister.view.contract.BaseProfileContract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-27
 */

public interface OpdProfileActivityContract {

    interface Presenter extends BaseProfileContract.Presenter {

        @Nullable
        OpdProfileActivityContract.View getProfileView();

        void refreshProfileTopSection(@NonNull Map<String, String> client);

        void startForm(@NonNull String formName, @NonNull CommonPersonObjectClient commonPersonObjectClient);

        void startFormActivity(@Nullable JSONObject form, @NonNull String caseId, @NonNull String entityTable);

        void saveVisitOrDiagnosisForm(@NonNull String eventType, @Nullable Intent data);

        void saveUpdateRegistrationForm(@NonNull String jsonString, @NonNull RegisterParams registerParams);

        HashMap<String, String> getInjectedFields(@NonNull String formName, @NonNull String entityId);

        @Nullable
        OpdEventClient processRegistration(@NonNull String jsonString, @NonNull FormTag formTag);

        void onUpdateRegistrationBtnCLicked(@NonNull String baseEntityId);
    }

    interface View extends BaseProfileContract.View {

        void setProfileName(@NonNull String fullName);

        void setProfileID(@NonNull String registerId);

        void setProfileAge(@NonNull String age);

        void setProfileGender(@NonNull String gender);

        void setProfileImage(@NonNull String baseEntityId, int defaultImage);

        void openDiagnoseAndTreatForm();

        void openCheckInForm();

        void openCloseForm();

        void startFormActivity(@NonNull JSONObject form, @NonNull HashMap<String, String> intentKeys);

        OnSendActionToFragment getActionListenerForVisitFragment();

        OnSendActionToFragment getActionListenerForProfileOverview();

        @Nullable
        String getString(@StringRes int resId);


        @NonNull
        Context getContext();

        void startActivityForResult(@NonNull Intent intent, int requestCode);

        @Nullable
        CommonPersonObjectClient getClient();

        void setClient(@NonNull CommonPersonObjectClient client);

    }

    interface Interactor {

        void fetchSavedDiagnosisAndTreatmentForm(@NonNull String baseEntityId, @NonNull String entityTable);

        void saveRegistration(@NonNull OpdEventClient opdEventClient, @NonNull String jsonString, RegisterParams registerParams, @NonNull OpdProfileActivityContract.InteractorCallBack callBack);

        @Nullable
        CommonPersonObjectClient retrieveUpdatedClient(@NonNull String baseEntityId);

        void onDestroy(boolean isChangingConfiguration);

        void saveEvents(List<Event> opdFormEvent, OpdProfileActivityContract.InteractorCallBack callback);
    }

    interface InteractorCallBack {

        void onRegistrationSaved(@Nullable CommonPersonObjectClient client, boolean isEdit);

        void onFetchedSavedDiagnosisAndTreatmentForm(@Nullable OpdDiagnosisAndTreatmentForm diagnosisAndTreatmentForm, @NonNull String caseId, @NonNull String entityTable);

        void onEventSaved(List<Event> events);

    }
}