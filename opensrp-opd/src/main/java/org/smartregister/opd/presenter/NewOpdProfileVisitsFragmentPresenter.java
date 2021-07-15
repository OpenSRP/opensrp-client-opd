package org.smartregister.opd.presenter;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Event;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.contract.OpdProfileFragmentContract;
import org.smartregister.opd.dao.VisitDao;
import org.smartregister.opd.domain.ProfileHistory;
import org.smartregister.opd.pojo.OpdVisitSummary;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.util.CallableInteractor;
import org.smartregister.util.CallableInteractorCallBack;
import org.smartregister.util.GenericInteractor;
import org.smartregister.util.NativeFormProcessor;
import org.smartregister.util.Utils;
import org.smartregister.view.ListContract;
import org.smartregister.view.presenter.ListPresenter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static org.smartregister.opd.utils.OpdConstants.JSON_FORM_KEY.ENCOUNTER_TYPE;

public class NewOpdProfileVisitsFragmentPresenter extends ListPresenter<ProfileHistory> implements OpdProfileFragmentContract.Presenter<ProfileHistory> {

    private CallableInteractor callableInteractor;

    @Override
    public void openForm(Context context, String formName, String baseEntityID, String formSubmissionId) {
        CallableInteractor myInteractor = getCallableInteractor();
        Callable<JSONObject> callable = () -> readFormAndAddValues(readFormAsJson(context, formName, baseEntityID), formSubmissionId);
        myInteractor.execute(callable, new CallableInteractorCallBack<JSONObject>() {
            @Override
            public void onResult(JSONObject jsonObject) {
                OpdProfileFragmentContract.View<ProfileHistory> view = getView();
                if (view != null) {
                    if (jsonObject != null) {
                        view.startJsonForm(jsonObject);
                    } else {
                        view.onFetchError(new IllegalArgumentException("Form not found"));
                    }
                    view.setLoadingState(false);
                }
            }

            @Override
            public void onError(Exception ex) {
                ListContract.View<ProfileHistory> view = getView();
                if (view != null) {
                    view.onFetchError(ex);
                    view.setLoadingState(false);
                }
            }
        });
    }

    public JSONObject readFormAndAddValues(JSONObject jsonObject, String formSubmissionId) throws JSONException {
        if (StringUtils.isEmpty(formSubmissionId)) return jsonObject;

        NativeFormProcessor processor = OpdLibrary.getInstance().getFormProcessorFactory().createInstance(jsonObject);

        // read values
        String eventJson = VisitDao.fetchEventByFormSubmissionId(formSubmissionId);
        JSONObject savedEvent = new JSONObject(eventJson);

        Event event = OpdLibrary.getInstance().getEcSyncHelper().convert(savedEvent, Event.class);
        SimpleDateFormat sdfDate = new SimpleDateFormat(OpdConstants.DateTimeFormat.dd_MMM_yyyy, Locale.US);
        boolean readonly = !sdfDate.format(event.getEventDate().toDate()).equals(sdfDate.format(new Date()));

        Map<String, Object> values = processor.getFormResults(savedEvent);
        jsonObject.put(OpdConstants.Properties.FORM_SUBMISSION_ID, formSubmissionId);

        // little hack for pesky multi_select_list
        if (values.containsKey("disease_code_primary"))
            values.put("disease_code_primary", values.get("disease_code_object"));

        // little hack for pesky multi_select_list
        if (values.containsKey("disease_code_final_diagn"))
            values.put("disease_code_final_diagn", values.get("disease_code_object_final"));

        // inject values
        processor.populateValues(values, jsonObject1 -> {
            try {
                jsonObject1.put(JsonFormConstants.READ_ONLY, readonly);
            } catch (JSONException e) {
                Timber.e(e);
            }
        });

        return jsonObject;
    }


    @Override
    public JSONObject readFormAsJson(Context context, String formName, String baseEntityID) throws JSONException {
        // read form and inject base id
        String jsonForm = readAssetContents(context, formName);
        JSONObject jsonObject = new JSONObject(jsonForm);
        jsonObject.put(OpdConstants.Properties.BASE_ENTITY_ID, baseEntityID);
        return jsonObject;
    }

    @Override
    public String readAssetContents(Context context, String path) {
        return Utils.readAssetContents(context, path);
    }

    @Override
    public void saveForm(String jsonString, Context context) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            String title = jsonObject.getString(ENCOUNTER_TYPE);
             CallableInteractor myInteractor = getCallableInteractor();

                Callable<Void> callable = () -> {
                    //JSONObject jsonObject = new JSONObject(jsonString);
                    NativeFormProcessor processor = OpdLibrary.getInstance().getFormProcessorFactory().createInstance(jsonObject);
                    String entityId = jsonObject.getString(OpdConstants.Properties.BASE_ENTITY_ID);
                    String formSubmissionId = jsonObject.has(OpdConstants.Properties.FORM_SUBMISSION_ID) ?
                            jsonObject.getString(OpdConstants.Properties.FORM_SUBMISSION_ID) : null;

                    // String eventType = jsonObject.getString(ENCOUNTER_TYPE);

                    // update metadata
                    processor.withBindType("ec_client")
                            .withEncounterType(title)
                            .withFormSubmissionId(formSubmissionId)
                            .withEntityId(entityId)
                            .tagEventMetadata()
                            // create and save event to db
                            .saveEvent()
                            // execute client processing
                            .clientProcessForm();

                    return null;
                };

                myInteractor.execute(callable, new CallableInteractorCallBack<Void>() {
                    @Override
                    public void onResult(Void aVoid) {
                        OpdProfileFragmentContract.View<ProfileHistory> view = getView();
                        if (view != null) {
                            view.reloadFromSource();
                            view.setLoadingState(false);
                        }
                    }

                    @Override
                    public void onError(Exception ex) {
                        OpdProfileFragmentContract.View<ProfileHistory> view = getView();
                        if (view == null) return;
                        view.onFetchError(ex);
                        view.setLoadingState(false);
                    }
                });

        } catch (Exception ex) {
            Timber.e(ex.getMessage());
        }

    }

    @Override
    public CallableInteractor getCallableInteractor() {
        if (callableInteractor == null)
            callableInteractor = new GenericInteractor();

        return callableInteractor;
    }

    @Override
    public OpdProfileFragmentContract.View<ProfileHistory> getView() {
        return (OpdProfileFragmentContract.View<ProfileHistory>) super.getView();
    }
}
