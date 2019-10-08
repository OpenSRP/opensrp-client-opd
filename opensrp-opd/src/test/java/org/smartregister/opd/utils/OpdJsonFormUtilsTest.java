package org.smartregister.opd.utils;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.SyncConfiguration;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.BuildConfig;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.pojos.OpdMetadata;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.Repository;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;

@PrepareForTest({OpdJsonFormUtils.class, OpdUtils.class})
@RunWith(PowerMockRunner.class)
public class OpdJsonFormUtilsTest {

    @Rule
    ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGetFormAsJsonWithEmptyJsonObject() throws JSONException {
        OpdMetadata opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME
                , OpdDbConstants.KEY.TABLE
                , OpdConstants.EventType.OPD_REGISTRATION
                , OpdConstants.EventType.UPDATE_OPD_REGISTRATION
                , OpdConstants.CONFIG
                , Class.class
                , null
                , true);
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(null)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);
        expectedException.expect(JSONException.class);
        OpdJsonFormUtils.getFormAsJson(new JSONObject(), OpdConstants.EventType.OPD_REGISTRATION, "", "");
    }

    @Test
    public void testGetFormAsJsonWithNonEmptyJsonObjectAndEntityIdBlank() throws JSONException {
        OpdMetadata opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME
                , OpdDbConstants.KEY.TABLE
                , OpdConstants.EventType.OPD_REGISTRATION
                , OpdConstants.EventType.UPDATE_OPD_REGISTRATION
                , OpdConstants.CONFIG
                , Class.class
                , null
                , true);
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(null)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("metadata", new JSONObject());
        JSONObject result = OpdJsonFormUtils.getFormAsJson(jsonObject, OpdConstants.JSON_FORM_KEY.NAME, "", "");
        Assert.assertNull(result);
    }

    @Test
    public void testGetFormAsJsonWithNonEmptyJsonObjectAndEntityIdNonEmpty() throws JSONException {
        OpdMetadata opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME
                , OpdDbConstants.KEY.TABLE
                , OpdConstants.EventType.OPD_REGISTRATION
                , OpdConstants.EventType.UPDATE_OPD_REGISTRATION
                , OpdConstants.CONFIG
                , Class.class
                , null
                , true);
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(null)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);

        JSONObject jsonArrayFieldsJsonObject = new JSONObject();
        jsonArrayFieldsJsonObject.put(OpdJsonFormUtils.KEY, OpdJsonFormUtils.OPENSRP_ID);

        JSONArray jsonArrayFields = new JSONArray();
        jsonArrayFields.put(jsonArrayFieldsJsonObject);

        JSONObject jsonObjectForFields = new JSONObject();
        jsonObjectForFields.put(OpdJsonFormUtils.FIELDS, jsonArrayFields);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("metadata", new JSONObject());
        jsonObject.put(OpdJsonFormUtils.STEP1, jsonObjectForFields);

        JSONObject result = OpdJsonFormUtils.getFormAsJson(jsonObject, OpdConstants.JSON_FORM_KEY.NAME, "23", "");
        Assert.assertEquals(result, jsonObject);
    }

    @Test
    public void testAddLocationTreeWithEmptyJsonObject() throws Exception {
        JSONObject jsonObject = new JSONObject();
        Whitebox.invokeMethod(OpdJsonFormUtils.class, "addLocationTree", "", jsonObject, "");
    }

    @Test
    public void testAddLocationTreeWithNonEmptyJsonObject() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdJsonFormUtils.KEY, "");
        JSONArray jsonArray = new JSONArray();
        Whitebox.invokeMethod(OpdJsonFormUtils.class, "addLocationTree", "", jsonObject, jsonArray.toString());
    }

    @Test
    public void testAddLocationDefaultWithEmptyJsonObject() throws Exception {
        JSONObject jsonObject = new JSONObject();
        Whitebox.invokeMethod(OpdJsonFormUtils.class, "addLocationDefault", "", jsonObject, "");
    }

    @Test
    public void testAddLocationDefaultTreeWithNonEmptyJsonObject() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdJsonFormUtils.KEY, "");
        JSONArray jsonArray = new JSONArray();
        Whitebox.invokeMethod(OpdJsonFormUtils.class, "addLocationDefault", "", jsonObject, jsonArray.toString());
    }

    @Test
    public void testTagSyncMetadataWithEmptyEvent() throws Exception {
        OpdMetadata opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME
                , OpdDbConstants.KEY.TABLE
                , OpdConstants.EventType.OPD_REGISTRATION
                , OpdConstants.EventType.UPDATE_OPD_REGISTRATION
                , OpdConstants.CONFIG
                , Class.class
                , null
                , true);
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(null)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);
        CoreLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(SyncConfiguration.class));

        PowerMockito.when(OpdUtils.class, "getAllSharedPreferences").thenReturn(PowerMockito.mock(AllSharedPreferences.class));

        Event event = OpdJsonFormUtils.tagSyncMetadata(new Event());
        Assert.assertNotNull(event);
    }

    @Test
    public void testGetLocationIdWithCurrentLocalityIsNotNull() throws Exception {
        OpdMetadata opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME
                , OpdDbConstants.KEY.TABLE
                , OpdConstants.EventType.OPD_REGISTRATION
                , OpdConstants.EventType.UPDATE_OPD_REGISTRATION
                , OpdConstants.CONFIG
                , Class.class
                , null
                , true);
        opdMetadata.setHealthFacilityLevels(new ArrayList<String>());
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(null)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);
        CoreLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(SyncConfiguration.class));

        ArrayList<String> defaultLocations = new ArrayList<>();
        defaultLocations.add("Country");
        LocationHelper.init(defaultLocations,
                "Country");
        AllSharedPreferences allSharedPreferences = PowerMockito.mock(AllSharedPreferences.class);
        PowerMockito.when(allSharedPreferences, "fetchCurrentLocality").thenReturn("Place");
        Assert.assertNotNull(LocationHelper.getInstance());
        String result = OpdJsonFormUtils.getLocationId("Country", allSharedPreferences);
        Assert.assertEquals("Place", result);
    }

    @Test
    public void testValidateParameters() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormUtils.KEY, new JSONArray());
        Triple<Boolean, JSONObject, JSONArray> result = OpdJsonFormUtils.validateParameters(jsonObject.toString());
        Assert.assertNotNull(result);
    }

    @Test
    public void testProcessGenderReplaceMwithMale() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.SEX);
        jsonObject.put(OpdConstants.KEY.VALUE, "m");
        jsonArray.put(jsonObject);
        OpdJsonFormUtils.processGender(jsonArray);
        Assert.assertEquals("Male", jsonArray.getJSONObject(0).get("value"));
    }

    @Test
    public void testProcessGenderReplaceFwithFemale() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.SEX);
        jsonObject.put(OpdConstants.KEY.VALUE, "f");
        jsonArray.put(jsonObject);
        OpdJsonFormUtils.processGender(jsonArray);

        Assert.assertEquals("Female", jsonArray.getJSONObject(0).get("value"));
    }

    @Test
    public void testProcessGenderShouldReplaceNothing() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.SEX);
        jsonObject.put(OpdConstants.KEY.VALUE, "L");
        jsonArray.put(jsonObject);
        OpdJsonFormUtils.processGender(jsonArray);
        Assert.assertEquals("", jsonArray.getJSONObject(0).get("value"));
    }

    @Test
    public void testProcessGenderShouldThrowNullPointerException() {
        JSONArray jsonArray = new JSONArray();
        OpdJsonFormUtils.processGender(jsonArray);
    }

    @Test
    public void testProcessGenderShouldThrowJSONException() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.SEX);
        jsonArray.put(jsonObject);
        OpdJsonFormUtils.processGender(jsonArray);
    }

    @Test
    public void testProcessLocationFields() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.TYPE, JsonFormConstants.TREE);
        JSONArray jsonArray1 = new JSONArray();
        jsonArray1.put("test");
        jsonObject.put(JsonFormConstants.VALUE, jsonArray1.toString());
        jsonArray.put(jsonObject);
        ArrayList<String> defaultLocations = new ArrayList<>();
        defaultLocations.add("Country");
        CoreLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(SyncConfiguration.class));
        LocationHelper.init(defaultLocations,
                "Country");
        OpdJsonFormUtils.processLocationFields(jsonArray);
        Assert.assertEquals(jsonArray.getJSONObject(0).getString(JsonFormConstants.VALUE), "test");
    }

    @Test
    public void testLastInteractedWithEmpty() {
        JSONArray jsonArray = new JSONArray();
        OpdJsonFormUtils.lastInteractedWith(jsonArray);
        Assert.assertEquals(jsonArray.length(), 1);
    }

    @Test
    public void testLastInteractedWithNullJsonArrayFields() {
        JSONArray jsonArray = null;
        OpdJsonFormUtils.lastInteractedWith(jsonArray);
    }

    @Test
    public void testDobUnknownUpdateFromAge() throws JSONException {
        JSONArray jsonArrayFields = new JSONArray();

        JSONArray jsonArrayDobUnknown = new JSONArray();
        JSONObject jsonObjectOptions = new JSONObject();
        jsonObjectOptions.put(OpdConstants.KEY.VALUE, Boolean.TRUE.toString());
        jsonArrayDobUnknown.put(jsonObjectOptions);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormUtils.KEY, OpdConstants.JSON_FORM_KEY.DOB_UNKNOWN);
        jsonObject.put(OpdConstants.JSON_FORM_KEY.OPTIONS, jsonArrayDobUnknown);

        JSONObject jsonObjectDob = new JSONObject();
        jsonObjectDob.put(JsonFormUtils.KEY, OpdConstants.JSON_FORM_KEY.DOB_ENTERED);

        JSONObject jsonObjectAgeEntered = new JSONObject();
        jsonObjectAgeEntered.put(JsonFormUtils.KEY, OpdConstants.JSON_FORM_KEY.AGE_ENTERED);
        jsonObjectAgeEntered.put(JsonFormUtils.VALUE, "34");


        jsonArrayFields.put(jsonObject);
        jsonArrayFields.put(jsonObjectAgeEntered);
        jsonArrayFields.put(jsonObjectDob);

        String expected = "[{\"options\":[{\"value\":\"true\"}],\"key\":\"dob_unknown\"},{\"value\":\"34\",\"key\":\"age_entered\"}," +
                "{\"value\":\"01-01-1985\",\"key\":\"dob_entered\"},{\"openmrs_entity\":\"person\"," +
                "\"openmrs_entity_id\":\"birthdate_estimated\",\"value\":1,\"key\":\"birthdate_estimated\"}]";

        OpdJsonFormUtils.dobUnknownUpdateFromAge(jsonArrayFields);

        Assert.assertEquals(expected, jsonArrayFields.toString());
    }

    @Test
    public void testProcessReminderSetToTrue() throws Exception {
        JSONArray jsonArrayFields = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.JSON_FORM_KEY.REMINDERS);

        JSONArray jsonArrayOptions = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put(OpdConstants.KEY.VALUE, Boolean.toString(true));
        jsonArrayOptions.put(jsonObject1);

        jsonObject.put(OpdConstants.JSON_FORM_KEY.OPTIONS, jsonArrayOptions);
        jsonArrayFields.put(jsonObject);

        Whitebox.invokeMethod(OpdJsonFormUtils.class, "processReminder", jsonArrayFields);

        String expected = "[{\"options\":[{\"value\":\"true\"}],\"value\":1,\"key\":\"reminders\"}]";
        Assert.assertEquals(expected, jsonArrayFields.toString());

    }

    @Test
    public void testProcessReminderSetToFalse() throws Exception {
        JSONArray jsonArrayFields = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.JSON_FORM_KEY.REMINDERS);

        JSONArray jsonArrayOptions = new JSONArray();
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put(OpdConstants.KEY.VALUE, Boolean.toString(false));
        jsonArrayOptions.put(jsonObject1);

        jsonObject.put(OpdConstants.JSON_FORM_KEY.OPTIONS, jsonArrayOptions);
        jsonArrayFields.put(jsonObject);

        Whitebox.invokeMethod(OpdJsonFormUtils.class, "processReminder", jsonArrayFields);

        String expected = "[{\"options\":[{\"value\":\"false\"}],\"value\":0,\"key\":\"reminders\"}]";
        Assert.assertEquals(expected, jsonArrayFields.toString());

    }

    @Test
    public void testFieldsHasEmptyStep() {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = OpdJsonFormUtils.fields(jsonObject, "");
        Assert.assertNull(jsonArray);
    }

    @Test
    public void testFieldHasStep() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String step = "STEP1";
        JSONObject jsonObjectWithFields = new JSONObject();
        jsonObjectWithFields.put(OpdJsonFormUtils.FIELDS, new JSONArray());
        jsonObject.put(step, jsonObjectWithFields);
        JSONArray jsonArray = OpdJsonFormUtils.fields(jsonObject, step);
        Assert.assertNotNull(jsonArray);
    }

    @Test
    public void testProcessOpdDetailsForm() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArrayFields = new JSONArray();

        JSONObject jsonObjectWithFields = new JSONObject();
        jsonObjectWithFields.put(OpdJsonFormUtils.FIELDS, new JSONArray());
//        jsonObjectFields.put(OpdJsonFormUtils.FIELDS, new JSONArray());
        jsonObject.put(JsonFormUtils.STEP1, jsonObjectWithFields);
        OpdJsonFormUtils.processOpdDetailsForm(jsonObject.toString(), PowerMockito.mock(FormTag.class));
    }
}