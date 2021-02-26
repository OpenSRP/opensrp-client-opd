package org.smartregister.opd.activity;

import android.app.Activity;
import android.content.Intent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.BaseActivityUnitTest;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.presenter.OpdProfileActivityPresenter;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.repository.ImageRepository;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class BaseOpdProfileActivityTest extends BaseActivityUnitTest {

    private ActivityController<BaseOpdProfileActivity> controller;

    private BaseOpdProfileActivity activity;

    @Mock
    private CoreLibrary coreLibrary;

    @Mock
    private Context opensrpContext;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private DrishtiApplication drishtiApplication;

    @Mock
    private OpdConfiguration opdConfiguration;

    @Mock
    private OpdLibrary opdLibrary;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        doReturn(imageRepository).when(opensrpContext).imageRepository();
        doReturn(opensrpContext).when(opensrpContext).updateApplicationContext(any(android.content.Context.class));
        doReturn(opensrpContext).when(coreLibrary).context();
        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", coreLibrary);

        doReturn(RuntimeEnvironment.application.getResources()).when(drishtiApplication).getResources();
        doReturn(RuntimeEnvironment.application).when(drishtiApplication).getApplicationContext();
        ReflectionHelpers.setStaticField(DrishtiApplication.class, "mInstance", drishtiApplication);


        doReturn(opdConfiguration).when(opdLibrary).getOpdConfiguration();
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);

        HashMap<String, String> clientMap = new HashMap<>();
        clientMap.put(OpdDbConstants.KEY.FIRST_NAME, "John");
        clientMap.put(OpdDbConstants.KEY.LAST_NAME, "Doe");
        clientMap.put(OpdDbConstants.KEY.GENDER, "Male");
        CommonPersonObjectClient commonPersonObjectClient = new CommonPersonObjectClient("user-id", clientMap, "ziggy");
        commonPersonObjectClient.setColumnmaps(clientMap);

        Intent intent = new Intent();
        intent.putExtra(OpdConstants.IntentKey.CLIENT_OBJECT, commonPersonObjectClient);

        controller = Robolectric.buildActivity(BaseOpdProfileActivity.class, intent).create().start().resume();
        activity = spy(controller.get());
    }

    @Test
    public void testOnActivityResultShouldSaveCheckInForm() {
        String strCheckinForm = "{\"count\":\"1\",\"encounter_type\":\"OPD Check-In\",\"entity_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"OPD Check-In\",\"fields\":[{\"key\":\"gender\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"label\",\"hidden\":true},{\"key\":\"age\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"label\",\"hidden\":true},{\"key\":\"pregnancy_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1541AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"What is the current pregnancy status?\",\"values\":[\"Positive\",\"Negative\",\"Unknown\"],\"keys\":[\"Positive\",\"Negative\",\"Unknown\"],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_checkin_relevance_rules.yml\"}}}},{\"key\":\"hiv_tested\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"160427AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"Has HIV Testing been done before?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"hiv_prev_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"What were the results of the previous HIV Test?\",\"values\":[\"Positive\",\"Negative\",\"Unknown\"],\"keys\":[\"Positive\",\"Negative\",\"Unknown\"],\"relevance\":{\"step1:hiv_tested\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Yes\\\")\"}}},{\"key\":\"hiv_prev_pos_art\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Is the patient taking ART?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"relevance\":{\"step1:hiv_prev_status\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Positive\\\")\"}}},{\"key\":\"current_hiv_status\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Current HIV Test Results\",\"values\":[\"Positive\",\"Negative\",\"Unknown\",\"Not Done\"],\"keys\":[\"Positive\",\"Negative\",\"Unknown\",\"Not Done\"],\"relevance\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_checkin_relevance_rules.yml\"}}},\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"visit_type\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Visit Type\",\"values\":[\"New\",\"Referral\",\"Re-Visit\"],\"keys\":[\"New\",\"Referral\",\"Re-Visit\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"appointment_due\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Was an appointment scheduled previously?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"v_required\":{\"value\":\"true\",\"err\":\"Please answer the question\"}},{\"key\":\"appointment_due_date\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"date_picker\",\"hint\":\"If appointment was scheduled, when was it due?\",\"values\":[\"Yes\",\"No\"],\"keys\":[\"Yes\",\"No\"],\"relevance\":{\"step1:appointment_due\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Yes\\\")\"}}},{\"key\":\"appointment_done_inless_time\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"edit_text\",\"hidden\":\"true\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_checkin_calculations.yml\"}}}}]}}";
        Intent intent = new Intent();
        intent.putExtra(OpdConstants.JSON_FORM_EXTRA.JSON, strCheckinForm);

        OpdProfileActivityPresenter presenter = ReflectionHelpers.getField(activity, "presenter");

        OpdProfileActivityPresenter spyPresenter = spy(presenter);
        ReflectionHelpers.setField(activity, "presenter", spyPresenter);
        activity.onActivityResult(OpdJsonFormUtils.REQUEST_CODE_GET_JSON, RESULT_OK, intent);

        verify(spyPresenter).saveVisitOrDiagnosisForm(eq(OpdConstants.EventType.CHECK_IN), eq(intent));
    }

    @After
    public void tearDown() {
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", null);
        ReflectionHelpers.setStaticField(DrishtiApplication.class, "mInstance", null);
        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", null);
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
}