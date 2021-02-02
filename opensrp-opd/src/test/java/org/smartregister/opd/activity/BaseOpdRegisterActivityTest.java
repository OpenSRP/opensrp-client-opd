package org.smartregister.opd.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.opd.BaseActivityUnitTest;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.contract.OpdRegisterActivityContract;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.opd.presenter.BaseOpdRegisterActivityPresenter;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class BaseOpdRegisterActivityTest extends BaseActivityUnitTest {

    private TestBaseOpdRegisterActivity activity;

    private ActivityController<TestBaseOpdRegisterActivity> controller;

    @Mock
    private CoreLibrary coreLibrary;

    @Mock
    private Context opensrpContext;

    @Mock
    private OpdLibrary opdLibrary;

    @Mock
    private OpdConfiguration opdConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(opensrpContext).when(opensrpContext).updateApplicationContext(any(android.content.Context.class));
        doReturn(opensrpContext).when(coreLibrary).context();
        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", coreLibrary);

        doReturn(opdConfiguration).when(opdLibrary).getOpdConfiguration();
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);

        Intent intent = new Intent();
        controller = Robolectric.buildActivity(TestBaseOpdRegisterActivity.class, intent).create().start().resume();
        activity = spy(controller.get());
    }

    @Test
    public void testStartFormActivity() throws JSONException {
        String strForm = "{\"count\":\"1\",\"encounter_type\":\"OPD Check-In\",\"entity_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"OPD Check-In\",\"fields\":[{\"key\":\"gender\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"label\",\"hidden\":true},{\"key\":\"age\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"label\",\"hidden\":true},{\"key\":\"pregnancy_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1541AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"What is the current pregnancy status?\",\"values\":[\"Positive\",\"Negative\",\"Unknown\"],\"keys\":[\"Positive\",\"Negative\",\"Unknown\"],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_checkin_relevance_rules.yml\"}}}},{\"key\":\"hiv_tested\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160427AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"Has HIV Testing been done before?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"hiv_prev_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"What were the results of the previous HIV Test?\",\"values\":[\"Positive\",\"Negative\",\"Unknown\"],\"keys\":[\"Positive\",\"Negative\",\"Unknown\"],\"relevance\":{\"step1:hiv_tested\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Yes\\\")\"}}},{\"key\":\"hiv_prev_pos_art\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Is the patient taking ART?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"relevance\":{\"step1:hiv_prev_status\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Positive\\\")\"}}},{\"key\":\"current_hiv_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Current HIV Test Results\",\"values\":[\"Positive\",\"Negative\",\"Unknown\",\"Not Done\"],\"keys\":[\"Positive\",\"Negative\",\"Unknown\",\"Not Done\"],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_checkin_relevance_rules.yml\"}}},\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"visit_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Visit Type\",\"values\":[\"New\",\"Referral\",\"Re-Visit\"],\"keys\":[\"New\",\"Referral\",\"Re-Visit\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"appointment_due\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Was an appointment scheduled previously?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"appointment_due_date\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"date_picker\",\"hint\":\"If appointment was scheduled, when was it due?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"relevance\":{\"step1:appointment_due\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Yes\\\")\"}}},{\"key\":\"appointment_done_inless_time\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"edit_text\",\"hidden\":\"true\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_checkin_calculations.yml\"}}}}]}}";
        JSONObject form = new JSONObject(strForm);
        OpdMetadata opdMetadata = new OpdMetadata("form-name"
                , "table-name"
                , "register-event-type"
                , "update-event-type"
                , "config"
                , BaseOpdFormActivity.class
                , BaseOpdProfileActivity.class
                , false);
        doReturn(opdMetadata).when(opdConfiguration).getOpdMetadata();
        doNothing().when(activity).startActivityForResult(any(Intent.class), eq(OpdJsonFormUtils.REQUEST_CODE_GET_JSON));

        activity.startFormActivity(form, new HashMap<>());

        verify(activity).startActivityForResult(any(Intent.class), eq(OpdJsonFormUtils.REQUEST_CODE_GET_JSON));
    }

    @After
    public void tearDown() {
        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", null);
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", null);
        destroyController();
    }

    @Override
    protected Activity getActivity() {
        return activity;
    }

    @Override
    protected ActivityController getActivityController() {
        return controller;
    }

    public static class TestBaseOpdRegisterActivity extends BaseOpdRegisterActivity {

        @Override
        protected BaseOpdRegisterActivityPresenter createPresenter(@NonNull OpdRegisterActivityContract.View view, @NonNull OpdRegisterActivityContract.Model model) {
            return mock(BaseOpdRegisterActivityPresenter.class);
        }

        @Override
        protected BaseRegisterFragment getRegisterFragment() {
            return new BaseRegisterFragment() {
                @Override
                protected void initializePresenter() {
                }

                @Override
                public void setUniqueID(String qrCode) {

                }

                @Override
                public void setAdvancedSearchFormData(HashMap<String, String> advancedSearchFormData) {

                }

                @Override
                protected String getMainCondition() {
                    return null;
                }

                @Override
                protected String getDefaultSortQuery() {
                    return null;
                }

                @Override
                protected void startRegistration() {

                }

                @Override
                protected void onViewClicked(View view) {

                }

                @Override
                public void showNotFoundPopup(String opensrpId) {

                }
            };
        }

        @Override
        public void startFormActivity(String formName, String entityId, Map<String, String> metaData) {

        }

        @Override
        public void startFormActivity(JSONObject form) {

        }

        @Override
        protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {

        }
    }
}