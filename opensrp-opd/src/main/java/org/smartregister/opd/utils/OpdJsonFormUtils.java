package org.smartregister.opd.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.reflect.TypeToken;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.FormEntityConstants;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.enums.LocationHierarchy;
import org.smartregister.opd.pojos.OpdEventClient;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.ImageRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.util.AssetHandler;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

public class OpdJsonFormUtils extends org.smartregister.util.JsonFormUtils {

    public static final String METADATA = "metadata";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final int REQUEST_CODE_GET_JSON = 2244;
    public static final String STEP2 = "step2";
    public static final String CURRENT_OPENSRP_ID = "current_opensrp_id";
    public static final String OPENSRP_ID = "OPENSRP_ID";
    public static final String ZEIR_ID = "ZEIR_ID";

    public static JSONObject getFormAsJson(@NonNull JSONObject form, @NonNull  String formName, @NonNull  String id, @NonNull  String currentLocationId) throws JSONException {
        String entityId = id;
        form.getJSONObject(METADATA).put(ENCOUNTER_LOCATION, currentLocationId);

        if (OpdUtils.metadata().getOpdRegistrationFormName().equals(formName)) {
            if (StringUtils.isBlank(entityId)) {
                UniqueIdRepository uniqueIdRepo = OpdLibrary.getInstance().getUniqueIdRepository();
                entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                if (entityId.isEmpty()) {
                    Timber.e("OpdJsonFormUtils --> UniqueIds are empty");
                    return null;
                }
            }

            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            OpdJsonFormUtils.addRegLocHierarchyQuestions(form, OpdConstants.JSON_FORM_KEY.ADDRESS_WIDGET_KEY , LocationHierarchy.ENTIRE_TREE);

            // Inject OPenSrp id into the form
            JSONObject stepOne = form.getJSONObject(OpdJsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(OpdJsonFormUtils.FIELDS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(OpdJsonFormUtils.KEY).equalsIgnoreCase(OpdJsonFormUtils.OPENSRP_ID)) {
                    jsonObject.remove(OpdJsonFormUtils.VALUE);
                    jsonObject.put(OpdJsonFormUtils.VALUE, entityId);
                }
            }

        } else {
            Timber.w("OpdJsonFormUtils --> Unsupported form requested for launch %s", formName);
        }
        Timber.d("OpdJsonFormUtils --> form is %s", form.toString());
        return form;
    }

    private static void addRegLocHierarchyQuestions(@NonNull JSONObject form,@NonNull String widgetKey,@NonNull LocationHierarchy locationHierarchy) {
        try {
            JSONArray questions = form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(JsonFormConstants.FIELDS);
            ArrayList<String> allLevels = OpdLibrary.getInstance().getOpdConfiguration().getOpdMetadata().getLocationLevels();
            ArrayList<String> healthFacilities = OpdLibrary.getInstance().getOpdConfiguration().getOpdMetadata().getHealthFacilityLevels();

            List<String> defaultLocation = LocationHelper.getInstance().generateDefaultLocationHierarchy(allLevels);
            List<String> defaultFacility = LocationHelper.getInstance().generateDefaultLocationHierarchy(healthFacilities);
            List<FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);
            List<FormLocation> upToFacilitiesWithOther = LocationHelper.getInstance().generateLocationHierarchyTree(true, healthFacilities);
            List<FormLocation> entireTree = LocationHelper.getInstance().generateLocationHierarchyTree(true, allLevels);

            String defaultLocationString = AssetHandler.javaToJsonString(defaultLocation, new TypeToken<List<String>>() {
            }.getType());

            String defaultFacilityString = AssetHandler.javaToJsonString(defaultFacility, new TypeToken<List<String>>() {
            }.getType());

            String upToFacilitiesString = AssetHandler.javaToJsonString(upToFacilities, new TypeToken<List<FormLocation>>() {
            }.getType());

            String upToFacilitiesWithOtherString = AssetHandler.javaToJsonString(upToFacilitiesWithOther, new TypeToken<List<FormLocation>>() {
            }.getType());

            String entireTreeString = AssetHandler.javaToJsonString(entireTree, new TypeToken<List<FormLocation>>() {
            }.getType());

            updateLocationTree(widgetKey, locationHierarchy, questions, defaultLocationString, defaultFacilityString, upToFacilitiesString, upToFacilitiesWithOtherString, entireTreeString);

            //To Do Refactor to remove dependency on hardocded keys
            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Home_Facility")) {
                    if (StringUtils.isNotBlank(upToFacilitiesString)) {
                        questions.getJSONObject(i).put("tree", new JSONArray(upToFacilitiesString));
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        questions.getJSONObject(i).put("default", defaultFacilityString);
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("Birth_Facility_Name")) {
                    if (StringUtils.isNotBlank(upToFacilitiesWithOtherString)) {
                        questions.getJSONObject(i).put("tree", new JSONArray(upToFacilitiesWithOtherString));
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        questions.getJSONObject(i).put("default", defaultFacilityString);
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("Residential_Area")) {
                    if (StringUtils.isNotBlank(entireTreeString)) {
                        questions.getJSONObject(i).put("tree", new JSONArray(entireTreeString));
                    }
                    if (StringUtils.isNotBlank(defaultLocationString)) {
                        questions.getJSONObject(i).put("default", defaultLocationString);
                    }
                }
            }

        } catch (Exception e) {
            Timber.e(e, "OpdJsonFormUtils --> addRegLocHierarchyQuestions");
        }
    }

    private static void updateLocationTree(@NonNull String widgetKey,@NonNull LocationHierarchy locationHierarchy,@NonNull JSONArray questions,
                                           @NonNull String defaultLocationString,@NonNull String defaultFacilityString,
                                           @NonNull String upToFacilitiesString,@NonNull String upToFacilitiesWithOtherString,
                                           @NonNull String entireTreeString) throws JSONException {
        for (int i = 0; i < questions.length(); i++) {
            JSONObject widgets = questions.getJSONObject(i);
            switch (locationHierarchy) {
                case FACILITY_ONLY:
                    if (StringUtils.isNotBlank(upToFacilitiesString)) {
                        addLocationTree(widgetKey, widgets, upToFacilitiesString);
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        addLocationDefault(widgetKey, widgets, defaultFacilityString);
                    }
                    break;
                case FACILITY_WITH_OTHER_STRING:
                    if (StringUtils.isNotBlank(upToFacilitiesWithOtherString)) {
                        addLocationTree(widgetKey, widgets, upToFacilitiesWithOtherString);
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        addLocationDefault(widgetKey, widgets, defaultFacilityString);
                    }
                    break;
                case ENTIRE_TREE:
                    if (StringUtils.isNotBlank(entireTreeString)) {
                        addLocationTree(widgetKey, widgets, entireTreeString);
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        addLocationDefault(widgetKey, widgets, defaultLocationString);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static void addLocationTree(@NonNull String widgetKey,@NonNull JSONObject widget,@NonNull String updateString) {
        try {
            if (widget.getString("key").equals(widgetKey)) {
                widget.put("tree", new JSONArray(updateString));
            }
        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> addLocationTree");
        }
    }

    private static void addLocationDefault(@NonNull String widgetKey,@NonNull JSONObject widget,@NonNull String updateString) {
        try {
            if (widget.getString("key").equals(widgetKey)) {
                widget.put("default", new JSONArray(updateString));
            }
        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> addLocationDefault");
        }
    }

    protected static Event tagSyncMetadata(@NonNull Event event) {
        AllSharedPreferences allSharedPreferences = OpdUtils.getAllSharedPreferences();
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(locationId(allSharedPreferences));

        String childLocationId = getLocationId(event.getLocationId(), allSharedPreferences);
        event.setChildLocationId(childLocationId);

        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));

        event.setClientDatabaseVersion(OpdLibrary.getInstance().getDatabaseVersion());
        event.setClientApplicationVersion(OpdLibrary.getInstance().getApplicationVersion());
        return event;
    }

    @Nullable
    public static String getLocationId(@NonNull String defaultLocationId, @NonNull AllSharedPreferences allSharedPreferences) {
        String currentLocality = allSharedPreferences.fetchCurrentLocality();

        if (currentLocality != null) {
            String currentLocalityId = LocationHelper.getInstance().getOpenMrsLocationId(currentLocality);
            if (currentLocalityId != null && !defaultLocationId.equals(currentLocalityId)) {
                return currentLocalityId;
            }
        }

        return null;
    }

    public static String locationId(@NonNull AllSharedPreferences allSharedPreferences) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        String userLocationId = allSharedPreferences.fetchUserLocalityId(providerId);
        if (StringUtils.isBlank(userLocationId)) {
            userLocationId = allSharedPreferences.fetchDefaultLocalityId(providerId);
        }
        return userLocationId;
    }

    protected static Triple<Boolean, JSONObject, JSONArray> validateParameters(@NonNull String jsonString) {

        JSONObject jsonForm = toJSONObject(jsonString);
        JSONArray fields = fields(jsonForm);

        return Triple.of(jsonForm != null && fields != null, jsonForm, fields);
    }

    protected static void processGender(@NonNull JSONArray fields) {
        try {
            //TO DO Will need re-architecting later to support more languages, perhaps update the selector widget

            JSONObject genderObject = getFieldJSONObject(fields, OpdConstants.SEX);
            String genderValue = "";

            String rawGender = genderObject.getString(JsonFormConstants.VALUE);
            char rawGenderChar = !TextUtils.isEmpty(rawGender) ? rawGender.charAt(0) : ' ';
            switch (rawGenderChar) {
                case 'm':
                case 'M':
                    genderValue = "Male";
                    break;

                case 'f':
                case 'F':
                    genderValue = "Female";
                    break;

                default:
                    break;

            }

            genderObject.put(OpdConstants.KEY.VALUE, genderValue);
        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> processGender");
        }
    }

    protected static void processLocationFields(@NonNull JSONArray fields) throws JSONException {
        for (int i = 0; i < fields.length(); i++) {
            if (fields.getJSONObject(i).has(JsonFormConstants.TYPE) &&
                    fields.getJSONObject(i).getString(JsonFormConstants.TYPE).equals(JsonFormConstants.TREE))
                try {
                    String rawValue = fields.getJSONObject(i).getString(JsonFormConstants.VALUE);
                    JSONArray valueArray = new JSONArray(rawValue);
                    if (valueArray.length() > 0) {
                        String lastLocationName = valueArray.getString(valueArray.length() - 1);
                        String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lastLocationName);
                        fields.getJSONObject(i).put(JsonFormConstants.VALUE, lastLocationId);
                    }
                } catch (NullPointerException e) {
                    Timber.e(e);
                } catch (IllegalArgumentException e) {
                    Timber.e(e);
                }
        }
    }

    protected static void lastInteractedWith(@NonNull JSONArray fields) {
        try {
            JSONObject lastInteractedWith = new JSONObject();
            lastInteractedWith.put(OpdConstants.KEY.KEY, OpdConstants.JSON_FORM_KEY.LAST_INTERACTED_WITH);
            lastInteractedWith.put(OpdConstants.KEY.VALUE, Calendar.getInstance().getTimeInMillis());
            fields.put(lastInteractedWith);
        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> lastInteractedWith");
        }
    }

    protected static void dobUnknownUpdateFromAge(@NonNull JSONArray fields) {
        try {
            JSONObject dobUnknownObject = getFieldJSONObject(fields, OpdConstants.JSON_FORM_KEY.DOB_UNKNOWN);
            JSONArray options = getJSONArray(dobUnknownObject, OpdConstants.JSON_FORM_KEY.OPTIONS);
            JSONObject option = getJSONObject(options, 0);
            String dobUnKnownString = option != null ? option.getString(VALUE) : null;
            if (StringUtils.isNotBlank(dobUnKnownString) && Boolean.valueOf(dobUnKnownString)) {

                String ageString = getFieldValue(fields, OpdConstants.JSON_FORM_KEY.AGE_ENTERED);
                if (StringUtils.isNotBlank(ageString) && NumberUtils.isNumber(ageString)) {
                    int age = Integer.valueOf(ageString);
                    JSONObject dobJSONObject = getFieldJSONObject(fields, OpdConstants.JSON_FORM_KEY.DOB_ENTERED);
                    dobJSONObject.put(VALUE, OpdUtils.getDob(age));

                    //Mark the birth date as an approximation
                    JSONObject isBirthdateApproximate = new JSONObject();
                    isBirthdateApproximate.put(OpdConstants.KEY.KEY, FormEntityConstants.Person.birthdate_estimated);
                    isBirthdateApproximate.put(OpdConstants.KEY.VALUE, OpdConstants.BOOLEAN_INT.TRUE);
                    isBirthdateApproximate
                            .put(OpdConstants.OPENMRS.ENTITY, OpdConstants.ENTITY.PERSON);//Required for value to be processed
                    isBirthdateApproximate.put(OpdConstants.OPENMRS.ENTITY_ID, FormEntityConstants.Person.birthdate_estimated);
                    fields.put(isBirthdateApproximate);

                }
            }
        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> dobUnknownUpdateFromAge");
        }
    }

    public static void mergeAndSaveClient(@NonNull Client baseClient) throws Exception {
        JSONObject updatedClientJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(baseClient));
        JSONObject originalClientJsonObject =
                OpdLibrary.getInstance().getEcSyncHelper().getClient(baseClient.getBaseEntityId());
        JSONObject mergedJson = org.smartregister.util.JsonFormUtils.merge(originalClientJsonObject, updatedClientJson);
        //TODO Save edit log ?
        OpdLibrary.getInstance().getEcSyncHelper().addClient(baseClient.getBaseEntityId(), mergedJson);
    }

    public static void saveImage(@NonNull String providerId,@NonNull String entityId,@NonNull String imageLocation) {
        if (StringUtils.isBlank(imageLocation)) {
            return;
        }

        File file = new File(imageLocation);
        if (!file.exists()) {
            return;
        }

        Bitmap compressedImageFile = OpdLibrary.getInstance().getCompressor().compressToBitmap(file);
        saveStaticImageToDisk(compressedImageFile, providerId, entityId);

    }

    private static void saveStaticImageToDisk(Bitmap image, String providerId, String entityId) {
        if (image == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(entityId)) {
            return;
        }
        OutputStream os = null;
        try {

            if (entityId != null && !entityId.isEmpty()) {
                final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                File outputFile = new File(absoluteFileName);
                os = new FileOutputStream(outputFile);
                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                if (compressFormat != null) {
                    image.compress(compressFormat, 100, os);
                } else {
                    throw new IllegalArgumentException(
                            "Failed to save static image, could not retrieve image compression format from name " +
                                    absoluteFileName);
                }
                // insert into the db
                ProfileImage profileImage = new ProfileImage();
                profileImage.setImageid(UUID.randomUUID().toString());
                profileImage.setAnmId(providerId);
                profileImage.setEntityID(entityId);
                profileImage.setFilepath(absoluteFileName);
                profileImage.setFilecategory("profilepic");
                profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                ImageRepository imageRepo = OpdUtils.context().imageRepository();
                imageRepo.add(profileImage);
            }

        } catch (FileNotFoundException e) {
            Timber.e(e, "OpdJsonFormUtils --> Failed to save static image to disk");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Timber.e(e, "OpdJsonFormUtils --> Failed to close static images output stream after attempting to write image");
                }
            }
        }

    }

    public static JSONArray fields(@NonNull JSONObject jsonForm,@NonNull String step) {
        try {

            JSONObject step1 = jsonForm.has(step) ? jsonForm.getJSONObject(step) : null;
            if (step1 == null) {
                return null;
            }

            return step1.has(FIELDS) ? step1.getJSONArray(FIELDS) : null;

        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> fields");
        }
        return null;
    }

    public static FormTag formTag(@NonNull AllSharedPreferences allSharedPreferences) {
        FormTag formTag = new FormTag();
        formTag.providerId = allSharedPreferences.fetchRegisteredANM();
        formTag.appVersion = OpdLibrary.getInstance().getApplicationVersion();
        formTag.databaseVersion = OpdLibrary.getInstance().getDatabaseVersion();
        return formTag;
    }

    public static String getFieldValue(@NonNull String jsonString,@NonNull String step,@NonNull String key) {
        JSONObject jsonForm = toJSONObject(jsonString);
        if (jsonForm == null) {
            return null;
        }

        JSONArray fields = fields(jsonForm, step);
        if (fields == null) {
            return null;
        }

        return getFieldValue(fields, key);

    }

    public static OpdEventClient processOpdDetailsForm(@NonNull String jsonString,@NonNull FormTag formTag) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();

            String entityId = getString(jsonForm, ENTITY_ID);
            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }

            processGender(fields);//multi language to re visit

            processLocationFields(fields);

            lastInteractedWith(fields);

            dobUnknownUpdateFromAge(fields);

            processReminder(fields);

            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);

            Event baseEvent = org.smartregister.util.JsonFormUtils
                    .createEvent(fields, getJSONObject(jsonForm, METADATA), formTag, entityId,
                            OpdUtils.metadata().getRegisterEventType(), OpdUtils.metadata().getTableName());

            OpdJsonFormUtils.tagSyncMetadata(baseEvent);// tag docs

            return new OpdEventClient(baseClient, baseEvent);
        } catch (JSONException e) {
            Timber.e(e);
            return null;
        } catch (NullPointerException e) {
            Timber.e(e);
            return null;
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return null;
        }
    }

    private static void processReminder(@NonNull JSONArray fields) {
        try {
            JSONObject reminderObject = getFieldJSONObject(fields, "reminders");
            JSONArray options = getJSONArray(reminderObject, OpdConstants.JSON_FORM_KEY.OPTIONS);
            JSONObject option = getJSONObject(options, 0);
            String value = option.optString(JsonFormConstants.VALUE);
            int result = value.equals(Boolean.toString(false)) ? 0 : 1;
            reminderObject.put(OpdConstants.KEY.VALUE, result);
        } catch (JSONException e) {
            Timber.e(e, "OpdJsonFormUtils --> processReminder");
        }
    }
}