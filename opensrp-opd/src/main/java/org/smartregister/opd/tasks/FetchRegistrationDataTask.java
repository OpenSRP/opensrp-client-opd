package org.smartregister.opd.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.smartregister.CoreLibrary;
import org.smartregister.opd.contract.OpdProfileActivityContract;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdReverseJsonFormUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;

public class FetchRegistrationDataTask extends AsyncTask<String, Void, String> {

    private WeakReference<OpdProfileActivityContract.View> contextWeakReference;
    private OnTaskComplete onTaskComplete;

    public FetchRegistrationDataTask(@NonNull WeakReference<OpdProfileActivityContract.View> contextWeakReference, @NonNull OnTaskComplete onTaskComplete){
        this.contextWeakReference = contextWeakReference;
        this.onTaskComplete = onTaskComplete;
    }

    @Nullable
    protected String doInBackground(String... params) {
        Map<String, String> detailsMap = CoreLibrary.getInstance().context().detailsRepository().getAllDetailsForClient(params[0]);

        detailsMap.put(OpdJsonFormUtils.OPENSRP_ID, contextWeakReference.get().getClient().getDetails().get("register_id"));

        return OpdReverseJsonFormUtils.prepareJsonEditOpdRegistrationForm(detailsMap, Arrays.asList(OpdJsonFormUtils.OPENSRP_ID, OpdConstants.JSON_FORM_KEY.BHT_ID), contextWeakReference.get().getContext());
    }

    protected void onPostExecute(@Nullable String jsonForm) {
        onTaskComplete.onSuccess(jsonForm);
    }

    public interface OnTaskComplete {

        void onSuccess(@Nullable String jsonForm);
    }
}