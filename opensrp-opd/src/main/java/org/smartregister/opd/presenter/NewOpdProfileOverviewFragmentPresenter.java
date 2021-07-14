package org.smartregister.opd.presenter;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.contract.OpdProfileFragmentContract;
import org.smartregister.opd.dao.VisitDao;
import org.smartregister.opd.domain.ProfileAction;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.util.CallableInteractor;
import org.smartregister.util.CallableInteractorCallBack;
import org.smartregister.util.GenericInteractor;
import org.smartregister.util.NativeFormProcessor;
import org.smartregister.util.Utils;
import org.smartregister.view.ListContract;
import org.smartregister.view.presenter.ListPresenter;
import java.util.Map;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static org.smartregister.opd.utils.OpdConstants.JSON_FORM_EXTRA.STEP1;
import static org.smartregister.opd.utils.OpdConstants.JSON_FORM_KEY.ENCOUNTER_TYPE;
import static org.smartregister.opd.utils.OpdConstants.JSON_FORM_KEY.FIELDS;


public class NewOpdProfileOverviewFragmentPresenter extends ListPresenter<ProfileAction> implements OpdProfileFragmentContract.Presenter<ProfileAction> {

    private CallableInteractor callableInteractor;

    @Override
    public void openForm(Context context, String formName, String baseEntityID, String formSubmissionId) {
        CallableInteractor myInteractor = getCallableInteractor();
        Callable<JSONObject> callable = () -> readFormAndAddValues(readFormAsJson(context, formName, baseEntityID), formSubmissionId);
        myInteractor.execute(callable, new CallableInteractorCallBack<JSONObject>() {
            @Override
            public void onResult(JSONObject jsonObject) {
                OpdProfileFragmentContract.View<ProfileAction> view = getView();
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
                ListContract.View<ProfileAction> view = getView();
                if (view != null) {
                    view.onFetchError(ex);
                    view.setLoadingState(false);
                }
            }
        });
    }

    public JSONObject readFormAndAddValues(JSONObject jsonObject, String formSubmissionId) throws JSONException {
        attachAgeAndGender(jsonObject);
        if (getView() != null) {
            getView().attachGlobals(jsonObject);
        }
        attachLocationHierarchy(jsonObject);

        if (StringUtils.isEmpty(formSubmissionId)) return jsonObject;

        NativeFormProcessor processor = OpdLibrary.getInstance().getFormProcessorFactory().createInstance(jsonObject);

        // read values
        JSONObject savedEvent = VisitDao.fetchEventAsJson(formSubmissionId);
        Map<String, Object> values = processor.getFormResults(savedEvent);

        // inject values
        processor.populateValues(values);

        jsonObject.put(OpdConstants.Properties.FORM_SUBMISSION_ID, formSubmissionId);

        return jsonObject;
    }

    private void attachLocationHierarchy(JSONObject jsonObject) {
        try {
            if (jsonObject.getString(ENCOUNTER_TYPE).equals(OpdConstants.OpdModuleEvents.OPD_PHARMACY)
                    || jsonObject.getString(ENCOUNTER_TYPE).equals(OpdConstants.OpdModuleEvents.OPD_LABORATORY)
                    || jsonObject.getString(ENCOUNTER_TYPE).equals(OpdConstants.OpdModuleEvents.OPD_FINAL_OUTCOME)) {
                OpdJsonFormUtils.addRegLocHierarchyQuestions(jsonObject);
            }
        } catch (Exception e) {
            Timber.e(e, "NewOpdProfileOverviewFragmentPresenter -> attachLocationHierarchy()");
        }

    }

    protected void attachAgeAndGender(JSONObject jsonObject) {
        try {
            String encounterType = jsonObject.getString(ENCOUNTER_TYPE);
            if (getView() == null)
                return;
            CommonPersonObjectClient commonPersonObject = getView().getCommonPersonObject();
            if (commonPersonObject != null && encounterType.equals(OpdConstants.OpdModuleEvents.OPD_DIAGNOSIS)) {
                String gender = commonPersonObject.getColumnmaps().get(OpdDbConstants.Column.Client.GENDER);
                String age = String.valueOf(Utils.getAgeFromDate(commonPersonObject.getColumnmaps().get(OpdDbConstants.Column.Client.DOB)));
                JSONArray fields = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    if (field.getString(OpdConstants.KEY.KEY).equals(OpdConstants.JSON_FORM_KEY.AGE)) {
                        field.put(OpdConstants.JSON_FORM_KEY.VALUE, age);
                    } else if (field.getString(OpdConstants.KEY.KEY).equals(OpdConstants.JSON_FORM_KEY.GENDER)) {
                        field.put(OpdConstants.JSON_FORM_KEY.VALUE, gender);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
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
        CallableInteractor myInteractor = getCallableInteractor();

        Callable<Void> callable = () -> {
            JSONObject jsonObject = new JSONObject(jsonString);
            NativeFormProcessor processor = OpdLibrary.getInstance().getFormProcessorFactory().createInstance(jsonObject);
            String entityId = jsonObject.getString(OpdConstants.Properties.BASE_ENTITY_ID);
            String formSubmissionId = jsonObject.has(OpdConstants.Properties.FORM_SUBMISSION_ID) ?
                    jsonObject.getString(OpdConstants.Properties.FORM_SUBMISSION_ID) : null;

            String eventType = jsonObject.getString(ENCOUNTER_TYPE);

            // update metadata
            processor.withBindType("ec_client")
                    .withEncounterType(eventType)
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
                OpdProfileFragmentContract.View<ProfileAction> view = getView();
                if (view != null) {
                    view.reloadFromSource();
                    view.setLoadingState(false);
                }
            }

            @Override
            public void onError(Exception ex) {
                OpdProfileFragmentContract.View<ProfileAction> view = getView();
                if (view == null) return;
                view.onFetchError(ex);
                view.setLoadingState(false);
            }
        });
    }

    @Override
    public CallableInteractor getCallableInteractor() {
        if (callableInteractor == null)
            callableInteractor = new GenericInteractor();

        return callableInteractor;
    }

    @Override
    public OpdProfileFragmentContract.View<ProfileAction> getView() {
        return (OpdProfileFragmentContract.View<ProfileAction>) super.getView();
    }
}
