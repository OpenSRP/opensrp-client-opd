package org.smartregister.opd.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.opd.BaseActivityUnitTest;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.repository.OpdDiagnosisAndTreatmentFormRepository;
import org.smartregister.opd.utils.OpdConstants;

import static android.os.Looper.getMainLooper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@LooperMode(PAUSED)
public class BaseOpdFormActivityTest extends BaseActivityUnitTest {

    private ActivityController<BaseOpdFormActivity> controller;

    private BaseOpdFormActivity activity;

    @Mock
    private OpdLibrary opdLibrary;

    @Mock
    private OpdDiagnosisAndTreatmentFormRepository treatmentFormRepository;

    private String formJson = "{\"count\":\"1\",\"encounter_type\":\"OPD Close\",\"entity_id\":\"\",\"metadata\":{\"start\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"start\",\"openmrs_entity_id\":\"163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"end\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"end\",\"openmrs_entity_id\":\"163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"today\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"encounter\",\"openmrs_entity_id\":\"encounter_date\"},\"deviceid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"deviceid\",\"openmrs_entity_id\":\"163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"subscriberid\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"subscriberid\",\"openmrs_entity_id\":\"163150AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"simserial\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"simserial\",\"openmrs_entity_id\":\"163151AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"phonenumber\":{\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_data_type\":\"phonenumber\",\"openmrs_entity_id\":\"163152AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"encounter_location\":\"\",\"look_up\":{\"entity_id\":\"\",\"value\":\"\"}},\"step1\":{\"title\":\"OPD Close\",\"fields\":[{\"key\":\"opd_close_reason\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"165245AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"Reason for closing the OPD record\",\"values\":[\"Died\",\"Lost to follow-up\",\"Moved away\",\"Wrong entry\",\"Other\"],\"keys\":[\"died\",\"lost_to_follow_up\",\"moved_away\",\"wrong_entry\",\"other\"],\"openmrs_choice_ids\":{\"Woman died\":\"160034AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Lost to follow-up\":\"5240AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Moved away\":\"160415AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Wrong entry\":\"165246AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Other\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the place of delivery\"}},{\"key\":\"client_status\",\"type\":\"hidden\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"calculation\":{\"rules-engine\":{\"ex-rules\":{\"rules-file\":\"opd/opd_close_calculation_rules.yml\"}}}},{\"key\":\"date_of_death\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1543AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"date_picker\",\"hint\":\"When did the death occur?\",\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the date of death\"},\"relevance\":{\"step1:client_status\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"dead\\\")\"}}},{\"key\":\"place_of death\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1541AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"Place of death?\",\"values\":[\"Community\",\"This Facility\",\"In Transit (TR)\",\"Other Facility\",\"Home/Traditional Birth Authority (TBA)\",\"Other\"],\"openmrs_choice_ids\":{\"This Facility\":\"159372AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"In Transit (TR)\":\"1601AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Other Facility\":\"1588AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Home/Traditional Birth Authority (TBA)\":\"1536AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Other\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the place of death\"},\"relevance\":{\"step1:client_status\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"dead\\\")\"}}},{\"key\":\"death_cause\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"concept\",\"openmrs_entity_id\":\"1599AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"type\":\"spinner\",\"hint\":\"Cause of death?\",\"values\":[\"Unknown\",\"Abortion-related complications\",\"Obstructed labour\",\"Pre-eclampsia\",\"Eclampsia\",\"Antepartum haemorrhage\",\"Postpartum haemorrhage\",\"Placental abruption\",\"Infection\",\"Other (specify)\"],\"openmrs_choice_ids\":{\"Unknown\":\"1067AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Abortion-related complications\":\"122299AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Obstructed labour\":\"141596AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Pre-eclampsia\":\"129251AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Eclampsia\":\"118744AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Antepartum haemorrhage\":\"228AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Postpartum haemorrhage\":\"230AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Placental abruption\":\"130108AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Infection\":\"130AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"Other (specify)\":\"5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"v_required\":{\"value\":\"true\",\"err\":\"Please enter the cause of death\"},\"relevance\":{\"step1:client_status\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"dead\\\")\"}}},{\"key\":\"death_cause_specify\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"edit_text\",\"hint\":\"Specify the other cause of death\",\"relevance\":{\"step1:death_cause\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"Other (specify)\\\")\"}}},{\"key\":\"death_follow_up\",\"openmrs_entity_parent\":\"\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"type\":\"spinner\",\"hint\":\"Was the follow-up of death conducted by health workers?\",\"values\":[\"Yes\",\"No\"],\"openmrs_choice_ids\":{\"Yes\":\"1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"No\":\"1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"},\"relevance\":{\"step1:client_status\":{\"type\":\"string\",\"ex\":\"equalTo(., \\\"dead\\\")\"}}}]}}";

    @Before
    public void setUp() {
        Intent intent = new Intent();
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.JSON, formJson);
        intent.putExtra(OpdConstants.IntentKey.BASE_ENTITY_ID, "user-id");

        controller = Robolectric.buildActivity(BaseOpdFormActivity.class, intent).create().start().resume();
        activity = spy(controller.get());
    }

    @Test
    public void testClearSavedSessionShouldInvokeFormDelete() throws InterruptedException {
        doReturn(treatmentFormRepository).when(opdLibrary).getOpdDiagnosisAndTreatmentFormRepository();
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        activity.clearSavedSession();
        shadowOf(getMainLooper()).idle();
        Thread.sleep(ASYNC_TIMEOUT);
        verify(treatmentFormRepository).delete(any(OpdDiagnosisAndTreatmentForm.class));
    }

    @Test
    public void testSaveFormFillSessionShouldInvokeFormDelete() throws InterruptedException {
        doReturn(treatmentFormRepository).when(opdLibrary).getOpdDiagnosisAndTreatmentFormRepository();
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        activity.saveFormFillSession();
        shadowOf(getMainLooper()).idle();
        Thread.sleep(ASYNC_TIMEOUT);
        verify(treatmentFormRepository).saveOrUpdate(any(OpdDiagnosisAndTreatmentForm.class));
    }

    @After
    public void tearDown() {
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
}