package org.smartregister.opd.contract;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.util.CallableInteractor;
import org.smartregister.view.ListContract;

public interface OpdProfileFragmentContract {

    interface View<T extends ListContract.Identifiable> extends ListContract.View<T> {
        void startJsonForm(JSONObject jsonObject);

        void reloadFromSource();

        CommonPersonObjectClient getCommonPersonObject();

        void attachGlobals(JSONObject jsonObject, String formSubmissionId);
    }


    interface Presenter<T extends ListContract.Identifiable> extends ListContract.Presenter<T> {
        void openForm(Context context, String formName, String baseEntityID, String formSubmissionId);

        CallableInteractor getCallableInteractor();

        String readAssetContents(Context context, String path);

        JSONObject readFormAsJson(Context context, String formName, String baseEntityID) throws JSONException;

        void saveForm(String jsonString, Context context);
    }
}