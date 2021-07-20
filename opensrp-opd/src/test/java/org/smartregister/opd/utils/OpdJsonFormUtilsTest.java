package org.smartregister.opd.utils;

import android.graphics.Bitmap;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.BuildConfig;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.configuration.OpdRegisterQueryProviderTest;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.ImageRepository;
import org.smartregister.repository.Repository;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import id.zelory.compressor.Compressor;

@PrepareForTest({OpdUtils.class, OpdLibrary.class})
@RunWith(PowerMockRunner.class)
public class OpdJsonFormUtilsTest {

    @Mock
    private OpdLibrary opdLibrary;

    @Mock
    private DrishtiApplication drishtiApplication;

    @Mock
    private OpdConfiguration opdConfiguration;

    @Captor
    private ArgumentCaptor addClientCaptor;

    private OpdMetadata opdMetadata;

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private CoreLibrary coreLibrary;


    @Before
    public void setUp() {
        opdMetadata = new OpdMetadata(OpdConstants.JSON_FORM_KEY.NAME
                , OpdDbConstants.KEY.TABLE
                , OpdConstants.EventType.OPD_REGISTRATION
                , OpdConstants.EventType.UPDATE_OPD_REGISTRATION
                , OpdConstants.CONFIG
                , Class.class
                , Class.class
                , true);
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", null);
        ReflectionHelpers.setStaticField(LocationHelper.class, "instance", null);
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", null);
    }

    @Test
    public void mergeAndSaveClient() throws Exception {
        PowerMockito.mockStatic(OpdLibrary.class);
        PowerMockito.when(OpdLibrary.getInstance()).thenReturn(opdLibrary);
        ECSyncHelper ecSyncHelper = Mockito.mock(ECSyncHelper.class);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("first_name", "first_name");
        PowerMockito.when(ecSyncHelper.getClient("baseEntity")).thenReturn(jsonObject);
        PowerMockito.when(opdLibrary.getEcSyncHelper()).thenReturn(ecSyncHelper);

        Client client = new Client("baseEntity");
        OpdJsonFormUtils.mergeAndSaveClient(client);
        Mockito.verify(ecSyncHelper, Mockito.times(1))
                .addClient((String) addClientCaptor.capture(), (JSONObject) addClientCaptor.capture());

        JSONObject expected = new JSONObject();
        expected.put("baseEntityId", "baseEntity");
        expected.put("type", "Client");
        expected.put("first_name", "first_name");
        Assert.assertEquals("baseEntity", addClientCaptor.getAllValues().get(0));
        Assert.assertEquals(expected.toString(), addClientCaptor.getAllValues().get(1).toString());
    }

    @Test
    public void testGetFormAsJsonWithNonEmptyJsonObjectAndEntityIdBlank() throws Exception {
        OpdConfiguration opdConfiguration = new OpdConfiguration.Builder(OpdRegisterQueryProviderTest.class)
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
    public void testGetFormAsJsonWithNonEmptyJsonObjectAndEntityIdNonEmpty() throws Exception {
        OpdConfiguration opdConfiguration = new OpdConfiguration.Builder(OpdRegisterQueryProviderTest.class)
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

        JSONObject result = OpdJsonFormUtils.getFormAsJson(jsonObject, OpdConstants.JSON_FORM_KEY.NAME, "23", "currentLocation");
        Assert.assertEquals(result, jsonObject);
    }

    @Test
    public void testGetFormAsJsonWithNonEmptyJsonObjectAndInjectableFields() throws Exception {
        OpdConfiguration opdConfiguration = new OpdConfiguration.Builder(OpdRegisterQueryProviderTest.class)
                .setOpdMetadata(opdMetadata)
                .build();

        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);

        JSONObject jsonArrayFieldsJsonObject = new JSONObject();
        jsonArrayFieldsJsonObject.put(OpdJsonFormUtils.KEY, OpdJsonFormUtils.OPENSRP_ID);

        JSONObject injectableField = new JSONObject();
        injectableField.put(OpdJsonFormUtils.KEY, "Injectable");

        JSONArray jsonArrayFields = new JSONArray();
        jsonArrayFields.put(jsonArrayFieldsJsonObject);
        jsonArrayFields.put(injectableField);

        JSONObject jsonObjectForFields = new JSONObject();
        jsonObjectForFields.put(OpdJsonFormUtils.FIELDS, jsonArrayFields);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count", "1");
        jsonObject.put("metadata", new JSONObject());
        jsonObject.put(OpdJsonFormUtils.STEP1, jsonObjectForFields);

        HashMap<String, String> injectableFields = new HashMap<>();
        injectableFields.put("Injectable", "Injectable value");
        JSONObject result = OpdJsonFormUtils.getFormAsJson(jsonObject, OpdConstants.JSON_FORM_KEY.NAME, "23", "currentLocation", injectableFields);
        Assert.assertEquals(result, jsonObject);
        Assert.assertEquals("Injectable value", injectableField.getString(OpdJsonFormUtils.VALUE));
    }

    @Test
    public void testUpdateLocationStringShouldPopulateTreeAndDefaultAttributeUsingLocationHierarchyTree() throws Exception {
        Mockito.when(opdConfiguration.getOpdMetadata()).thenReturn(opdMetadata);
        Mockito.when(opdLibrary.getOpdConfiguration()).thenReturn(opdConfiguration);
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        opdMetadata.setFieldsWithLocationHierarchy(new HashSet<>(Arrays.asList("village")));

        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JsonFormConstants.KEY, "village");
        jsonObject.put(JsonFormConstants.TYPE, JsonFormConstants.TREE);
        jsonArray.put(jsonObject);
        String hierarchyString = "[\"Kenya\",\"Central\"]";
        String entireTreeString = "[{\"nodes\":[{\"level\":\"Province\",\"name\":\"Central\",\"key\":\"1\"}],\"level\":\"Country\",\"name\":\"Kenya\",\"key\":\"0\"}]";
        ArrayList<String> healthFacilities = new ArrayList<>();
        healthFacilities.add("Country");
        healthFacilities.add("Province");

        List<FormLocation> entireTree = new ArrayList<>();
        FormLocation formLocationCountry = new FormLocation();
        formLocationCountry.level = "Country";
        formLocationCountry.name = "Kenya";
        formLocationCountry.key = "0";
        FormLocation formLocationProvince = new FormLocation();
        formLocationProvince.level = "Province";
        formLocationProvince.name = "Central";
        formLocationProvince.key = "1";

        List<FormLocation> entireTreeCountryNode = new ArrayList<>();
        entireTreeCountryNode.add(formLocationProvince);
        formLocationCountry.nodes = entireTreeCountryNode;
        entireTree.add(formLocationCountry);

        ReflectionHelpers.setStaticField(LocationHelper.class, "instance", locationHelper);

        Mockito.doReturn(entireTree).when(locationHelper).generateLocationHierarchyTree(ArgumentMatchers.anyBoolean(), ArgumentMatchers.eq(healthFacilities));

        WhiteboxImpl.invokeMethod(OpdJsonFormUtils.class, "updateLocationTree", jsonArray, hierarchyString, entireTreeString, entireTreeString);
        Assert.assertTrue(jsonObject.has(JsonFormConstants.TREE));
        Assert.assertTrue(jsonObject.has(JsonFormConstants.DEFAULT));
        Assert.assertEquals(hierarchyString, jsonObject.optString(JsonFormConstants.DEFAULT));
        JSONArray resultTreeObject = new JSONArray(jsonObject.optString(JsonFormConstants.TREE));
        Assert.assertTrue(resultTreeObject.optJSONObject(0).has("nodes"));
        Assert.assertEquals("Kenya", resultTreeObject.optJSONObject(0).optString("name"));
        Assert.assertEquals("Country", resultTreeObject.optJSONObject(0).optString("level"));
        Assert.assertEquals("0", resultTreeObject.optJSONObject(0).optString("key"));
        Assert.assertEquals("Central", resultTreeObject.optJSONObject(0).optJSONArray("nodes").optJSONObject(0).optString("name"));
        Assert.assertEquals("1", resultTreeObject.optJSONObject(0).optJSONArray("nodes").optJSONObject(0).optString("key"));
        Assert.assertEquals("Province", resultTreeObject.optJSONObject(0).optJSONArray("nodes").optJSONObject(0).optString("level"));
    }

    @Test
    public void testTagSyncMetadataWithEmptyEvent() throws Exception {
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(null)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);


        Context opensrpContext = PowerMockito.mock(Context.class);

        Mockito.doReturn(PowerMockito.mock(AllSharedPreferences.class)).when(opensrpContext).allSharedPreferences();

        Mockito.doReturn(opensrpContext).when(coreLibrary).context();

        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", coreLibrary);

        Event event = OpdJsonFormUtils.tagSyncMetadata(new Event());
        Assert.assertNotNull(event);
    }

    @Test
    public void testGetLocationIdWithCurrentLocalityIsNotNull() throws Exception {
        opdMetadata.setHealthFacilityLevels(new ArrayList<String>());
        OpdConfiguration opdConfiguration = new OpdConfiguration
                .Builder(OpdRegisterQueryProviderTest.class)
                .setOpdMetadata(opdMetadata)
                .build();
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), opdConfiguration,
                BuildConfig.VERSION_CODE, 1);

        Context opensrpContext = PowerMockito.mock(Context.class);

        Mockito.doReturn(PowerMockito.mock(AllSharedPreferences.class)).when(opensrpContext).allSharedPreferences();

        Mockito.doReturn(opensrpContext).when(coreLibrary).context();

        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", coreLibrary);

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
    public void testProcessGenderCheckNullOnGenderJsonObject() {
        JSONArray jsonArray = new JSONArray();
        OpdJsonFormUtils.processGender(jsonArray);
        Assert.assertEquals(0, jsonArray.length());
    }

    @Test
    public void testProcessGenderShouldThrowJSONException() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdConstants.KEY.KEY, OpdConstants.SEX);
        jsonArray.put(jsonObject);
        OpdJsonFormUtils.processGender(jsonArray);
        Assert.assertEquals(1, jsonArray.getJSONObject(0).length());
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

        Context opensrpContext = PowerMockito.mock(Context.class);

        Mockito.doReturn(PowerMockito.mock(AllSharedPreferences.class)).when(opensrpContext).allSharedPreferences();

        Mockito.doReturn(opensrpContext).when(coreLibrary).context();

        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", coreLibrary);

        LocationHelper.init(defaultLocations,
                "Country");
        OpdJsonFormUtils.processLocationFields(jsonArray);
        Assert.assertEquals("test", jsonArray.getJSONObject(0).getString(JsonFormConstants.VALUE));
    }

    @Test
    public void testLastInteractedWithEmpty() {
        JSONArray jsonArray = new JSONArray();
        OpdJsonFormUtils.lastInteractedWith(jsonArray);
        Assert.assertEquals(1, jsonArray.length());
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

        OpdJsonFormUtils.dobUnknownUpdateFromAge(jsonArrayFields);
        JSONObject field = JsonFormUtils.getFieldJSONObject(jsonArrayFields, OpdConstants.JSON_FORM_KEY.DOB_ENTERED);

        Assert.assertEquals(Utils.getDob(34), field.getString(JsonFormUtils.VALUE));
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
    public void testFormTagShouldReturnValidFormTagObject() {
        OpdLibrary.init(PowerMockito.mock(Context.class), PowerMockito.mock(Repository.class), Mockito.mock(OpdConfiguration.class),
                BuildConfig.VERSION_CODE, 1);
        AllSharedPreferences allSharedPreferences = Mockito.mock(AllSharedPreferences.class);
        Mockito.when(allSharedPreferences.fetchRegisteredANM()).thenReturn("1");
        FormTag formTag = OpdJsonFormUtils.formTag(allSharedPreferences);
        Assert.assertTrue((BuildConfig.VERSION_CODE == formTag.appVersion));
        Assert.assertTrue((formTag.databaseVersion == 1));
        Assert.assertEquals("1", formTag.providerId);
    }

    @Test
    public void testGetFieldValueShouldReturnNullWithInvalidJsonString() {
        Assert.assertNull(OpdJsonFormUtils.getFieldValue("", "", ""));
    }

    @Test
    public void testGetFieldValueShouldReturnNullWithValidJsonStringWithoutStepKey() throws JSONException {
        JSONObject jsonForm = new JSONObject();
        JSONObject jsonStep = new JSONObject();
        jsonStep.put(OpdJsonFormUtils.FIELDS, new JSONArray());
        Assert.assertNull(OpdJsonFormUtils.getFieldValue(jsonForm.toString(), OpdJsonFormUtils.STEP1, ""));

    }

    @Test
    public void testGetFieldValueShouldReturnPassedValue() throws JSONException {
        JSONObject jsonForm = new JSONObject();
        JSONObject jsonStep = new JSONObject();
        JSONArray jsonArrayFields = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(OpdJsonFormUtils.KEY, OpdConstants.JSON_FORM_KEY.REMINDERS);
        jsonObject.put(OpdJsonFormUtils.VALUE, "some reminder");
        jsonArrayFields.put(jsonObject);
        jsonStep.put(OpdJsonFormUtils.FIELDS, jsonArrayFields);
        jsonForm.put(OpdJsonFormUtils.STEP1, jsonStep);

        Assert.assertEquals("some reminder", OpdJsonFormUtils.getFieldValue(jsonForm.toString(), OpdJsonFormUtils.STEP1, OpdConstants.JSON_FORM_KEY.REMINDERS));
    }

    @Test
    public void testProcessOpdDetailsFormShouldReturnNullJsonFormNull() {
        Assert.assertNull(OpdJsonFormUtils.processOpdDetailsForm("", Mockito.mock(FormTag.class)));
    }


    @Test
    public void testSaveImage() throws Exception {
        String providerId = "demo";
        String baseEntityId = "2323-wxdfd9-34";
        String imageLocation = "/";
        Compressor compressor = Mockito.mock(Compressor.class);
        PowerMockito.mockStatic(OpdUtils.class);
        PowerMockito.doNothing().when(OpdUtils.class, "saveImageAndCloseOutputStream", Mockito.any(Bitmap.class), Mockito.any(File.class));
        Bitmap bitmap = Mockito.mock(Bitmap.class);
        Mockito.when(compressor.compressToBitmap(Mockito.any(File.class))).thenReturn(bitmap);
        Mockito.when(opdLibrary.getCompressor()).thenReturn(compressor);
        android.content.Context context = Mockito.mock(android.content.Context.class);
        File file = Mockito.mock(File.class);
        Mockito.when(file.getAbsolutePath()).thenReturn("/home/opensrp");
        Mockito.when(context.getDir("opensrp", android.content.Context.MODE_PRIVATE)).thenReturn(file);
        Mockito.when(drishtiApplication.getApplicationContext()).thenReturn(context);
        Context opensrpContext = Mockito.mock(Context.class);
        ImageRepository imageRepository = Mockito.mock(ImageRepository.class);
        Mockito.when(opensrpContext.imageRepository()).thenReturn(imageRepository);
        PowerMockito.when(OpdUtils.class, "context").thenReturn(opensrpContext);
        Mockito.when(opdLibrary.context()).thenReturn(opensrpContext);
        ReflectionHelpers.setStaticField(DrishtiApplication.class, "mInstance", drishtiApplication);
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);
        OpdJsonFormUtils.saveImage(providerId, baseEntityId, imageLocation);
        ArgumentCaptor<ProfileImage> profileImageArgumentCaptor = ArgumentCaptor.forClass(ProfileImage.class);
        Mockito.verify(imageRepository, Mockito.times(1)).add(profileImageArgumentCaptor.capture());
        ProfileImage profileImage = profileImageArgumentCaptor.getValue();
        Assert.assertNotNull(profileImage);
        Assert.assertEquals("demo", profileImage.getAnmId());
        Assert.assertEquals(baseEntityId, profileImage.getEntityID());
        Assert.assertEquals("/home/opensrp/2323-wxdfd9-34.JPEG", profileImage.getFilepath());
        Assert.assertEquals(ImageRepository.TYPE_Unsynced, profileImage.getSyncStatus());
    }

    @Test
    public void testMedicineNoteString() throws Exception {
        String medicineValues = "[{\"key\":\"AA007840\",\"text\":\"Atenolol 50mg\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_entity_parent\":\"\",\"property\":{\"pack_size\":null,\"product_code\":\"AA007840\",\"dispensing_unit\":\"Tablet\",\"meta\":{\"duration\":\"78\",\"dosage\":\"12\",\"frequency\":\"3456\",\"info\":\"Dose: 12, Duration: 78, Frequency: 3456\"}}},{\"key\":\"FF006300\",\"text\":\"Bandage, WOW 10cm x 4m long, when stretched\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_entity_parent\":\"\",\"property\":{\"pack_size\":null,\"product_code\":\"FF006300\",\"dispensing_unit\":\"each\",\"meta\":{\"duration\":\"33\",\"dosage\":\"11\",\"frequency\":\"2244\",\"info\":\"Dose: 11, Duration: 33, Frequency: 2244\"}}}]\n";
        String result = OpdJsonFormUtils.getMedicineNoteString(medicineValues);
        String output = "Atenolol 50mg, Bandage, WOW 10cm x 4m long, when stretched";
        Assert.assertEquals(result, output);
    }

    @Test
    public void testGetLabResultsStringFromMap() throws Exception {
        HashMap<String, String> savedValues = new HashMap<>();
        savedValues.put("tests_repeating_group_count","3");
        savedValues.put("diagnostic_test_ba1ed23029a44fd980784093a5c6f746","ultra_sound");
        savedValues.put("diagnostic_test_24f8d3b0a73a49e9894c83d6d545b39f","pregnancy_test");
        savedValues.put("repeatingGroupMap","{\"24f8d3b0a73a49e9894c83d6d545b39f\":{\"diagnostic_test_result\":\"Negative\",\"diagnostic_test\":\"pregnancy_test\"},\"ba1ed23029a44fd980784093a5c6f746\":{\"diagnostic_test\":\"ultra_sound\",\"diagnostic_test_result_specify\":\"Ultra\"}}");
        savedValues.put("treatment_type","Medicine, Suturing, Wound dressing, Foreign body removal");
        savedValues.put("diagnostic_test_result_specify_ba1ed23029a44fd980784093a5c6f746","Ultra");
        savedValues.put("diagnostic_test_result_24f8d3b0a73a49e9894c83d6d545b39f","Negative");
        String result = OpdJsonFormUtils.getLabResultsStringFromMap(savedValues);
        String output = "Pregnancy test: { Status result: Negative, }, Ultra sound: , Specify result: Ultra}";
        Assert.assertEquals(result, output);
    }
}
