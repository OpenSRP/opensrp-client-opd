package org.smartregister.opd.utils;

import android.content.Context;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.jeasy.rules.api.Facts;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.Obs;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.activity.BaseOpdFormActivity;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.pojo.CompositeObs;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.repository.DetailsRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-10-17
 */

@RunWith(RobolectricTestRunner.class)
public class OpdUtilsTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private OpdLibrary opdLibrary;

    @Mock
    private OpdConfiguration opdConfiguration;

    @Mock
    private OpdMetadata opdMetadata;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void fillTemplateShouldReplaceTheBracketedVariableWithCorrectValue() {
        String template = "Gender: {gender}";
        Facts facts = new Facts();
        facts.put("gender", "Male");

        assertEquals("Gender:  Male", OpdUtils.fillTemplate(template, facts));
    }

    @Test
    public void convertStringToDate() {
        Date date = OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, "2019-10-28 18:09:49");
        assertEquals("2019-10-28 18:09:49", OpdUtils.convertDate(date, OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS));
    }

    @Test
    public void generateNIds() {
        String result = OpdUtils.generateNIds(0);
        assertEquals(result, "");

        String result1 = OpdUtils.generateNIds(1);
        assertEquals(result1.split(",").length, 1);
    }

    @Test
    public void getIntentValue() {
        Intent intent = Mockito.mock(Intent.class);
        Mockito.when(intent.hasExtra("test")).thenReturn(false);
        assertNull(OpdUtils.getIntentValue(intent, "test"));

        Mockito.when(intent.hasExtra("test")).thenReturn(true);
        Mockito.when(intent.getStringExtra("test")).thenReturn("test");
        assertEquals("test", OpdUtils.getIntentValue(intent, "test"));
    }

    @Test
    public void getIntentValueReturnNull() {
        assertNull(OpdUtils.getIntentValue(null, "test"));
    }

    @Test
    public void metadata() {
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        Mockito.doReturn(opdConfiguration).when(opdLibrary).getOpdConfiguration();
        Mockito.doReturn(opdMetadata).when(opdConfiguration).getOpdMetadata();

        assertEquals(opdMetadata, OpdUtils.metadata());

        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", null);
    }

    @Test
    public void testGetClientAge() {
        assertEquals("13", OpdUtils.getClientAge("13y 4m", "y"));
        assertEquals("4m", OpdUtils.getClientAge("4m", "y"));
        assertEquals("5", OpdUtils.getClientAge("5y 4w", "y"));
        assertEquals("3y", OpdUtils.getClientAge("3y", "y"));
        assertEquals("5w 6d", OpdUtils.getClientAge("5w 6d", "y"));
        assertEquals("6d", OpdUtils.getClientAge("6d", "y"));
    }

    @Test
    public void isTemplateShouldReturnFalseIfStringDoesNotContainMatchingBraces() {
        assertFalse(OpdUtils.isTemplate("{ This is a sytling brace"));
        assertFalse(OpdUtils.isTemplate("This is display text"));
    }

    @Test
    public void isTemplateShouldReturnTrueIfStringContainsMatchingBraces() {
        assertTrue(OpdUtils.isTemplate("Project Name: {project_name}"));
    }

    @Test
    public void buildActivityFormIntentShouldCreateIntentWithWizardEnabledWhenEncounterTypeIsDiagnosisAndTreat() throws JSONException {
        // Mock calls to OpdLibrary
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        Mockito.doReturn(opdConfiguration).when(opdLibrary).getOpdConfiguration();
        Mockito.doReturn(opdMetadata).when(opdConfiguration).getOpdMetadata();
        Mockito.doReturn(BaseOpdFormActivity.class).when(opdMetadata).getOpdFormActivity();

        JSONObject jsonForm = new JSONObject();
        jsonForm.put(OpdJsonFormUtils.ENCOUNTER_TYPE, OpdConstants.EventType.DIAGNOSIS_AND_TREAT);

        HashMap<String, String> parcelableData = new HashMap<>();
        String baseEntityId = "89283-23dsd-23sdf";
        parcelableData.put(OpdConstants.IntentKey.BASE_ENTITY_ID, baseEntityId);

        Intent actualResult = OpdUtils.buildFormActivityIntent(jsonForm, parcelableData, Mockito.mock(Context.class));
        Form form = (Form) actualResult.getSerializableExtra(JsonFormConstants.JSON_FORM_KEY.FORM);

        assertTrue(form.isWizard());
        assertEquals(OpdConstants.EventType.DIAGNOSIS_AND_TREAT, form.getName());
        assertEquals(baseEntityId, actualResult.getStringExtra(OpdConstants.IntentKey.BASE_ENTITY_ID));
    }

    @Test
    public void buildRepeatingGroupTests() throws JSONException {
        String strStep1JsonObject = "{\"fields\":[{\"key\":\"tests_repeating_group\",\"type\":\"repeating_group\",\"value\"" +
                ":[{\"key\":\"diagnostic_test\"},{\"key\":\"diagnostic_result_specify\"}]}," +
                "{\"key\":\"diagnostic_test_128040f1b4034311b34b6ea65a81d3aa\",\"values\":[\"Ultra sound\"],\"value\":\"Ultra sound\"}," +
                "{\"key\":\"diagnostic_result_specify_128040f1b4034311b34b6ea65a81d3aa\",\"value\":\"wer\"}]}";
        JSONObject step1JsonObject = new JSONObject(strStep1JsonObject);
        int repeatingGroupNum = OpdUtils.buildRepeatingGroupTests(step1JsonObject);
        assertEquals(1, repeatingGroupNum);
    }

    @Test
    public void getAllObsObject() {
        Event event = new Event();
        Obs obs = new Obs();
        obs.setFormSubmissionField(OpdConstants.JSON_FORM_KEY.DIAGNOSTIC_TEST_RESULT_SPINNER);
        obs.setValue("positive");
        obs.addToHumanReadableValuesList("");
        obs.setFieldDataType("text");
        obs.setFieldCode(OpdConstants.JSON_FORM_KEY.DIAGNOSTIC_TEST_RESULT_SPINNER);
        event.addObs(obs);

        Obs obs1 = new Obs();
        obs1.setFormSubmissionField(OpdConstants.JSON_FORM_KEY.DIAGNOSTIC_TEST);
        obs1.setValue("malaria");
        obs1.setFieldDataType("text");
        obs1.addToHumanReadableValuesList("");
        obs1.setFieldCode(OpdConstants.JSON_FORM_KEY.DIAGNOSTIC_TEST);
        event.addObs(obs1);

        List<CompositeObs> compositeObsList = OpdUtils.getAllObsObject(event);
        assertEquals(2, compositeObsList.size());
        assertEquals("positive", compositeObsList.get(0).getValue());
        assertEquals(OpdConstants.JSON_FORM_KEY.DIAGNOSTIC_TEST_RESULT_SPINNER, compositeObsList.get(0).getFormSubmissionFieldKey());

        assertEquals("malaria", compositeObsList.get(1).getValue());
        assertEquals(OpdConstants.JSON_FORM_KEY.DIAGNOSTIC_TEST, compositeObsList.get(1).getFormSubmissionFieldKey());

    }

    @Test
    public void testGetInjectableFieldsShouldPopulateMapCorrectly() {
        String baseEntityId = "234-234";
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        org.smartregister.Context context = Mockito.mock(org.smartregister.Context.class);
        Mockito.when(opdLibrary.context()).thenReturn(context);
        DetailsRepository detailsRepository = Mockito.mock(DetailsRepository.class);
        Mockito.when(context.detailsRepository()).thenReturn(detailsRepository);
        Map<String, String> detailsMap = new HashMap<>();
        detailsMap.put(OpdConstants.ClientMapKey.GENDER, "Female");
        detailsMap.put(OpdDbConstants.Column.Client.DOB, "");
        Mockito.when(detailsRepository.getAllDetailsForClient(baseEntityId)).thenReturn(detailsMap);
        String formName = OpdConstants.Form.OPD_CHECK_IN;
        HashMap<String, String> hashMap = OpdUtils.getInjectableFields(formName, baseEntityId);
        assertEquals(hashMap.get(OpdConstants.JSON_FORM_KEY.AGE), "");
        assertEquals(hashMap.get(OpdConstants.ClientMapKey.GENDER), "Female");

        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", null);

    }
}