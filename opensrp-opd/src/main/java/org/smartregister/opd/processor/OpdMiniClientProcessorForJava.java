package org.smartregister.opd.processor;

import android.content.ContentValues;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import net.sqlcipher.database.SQLiteException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.domain.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.Obs;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.exception.CheckInEventProcessException;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdDiagnosisDetail;
import org.smartregister.opd.pojo.OpdTestConducted;
import org.smartregister.opd.pojo.OpdTreatmentDetail;
import org.smartregister.opd.pojo.OpdVisit;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.opd.utils.VisitUtils;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.MiniClientProcessorForJava;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-10-01
 */

public class OpdMiniClientProcessorForJava extends ClientProcessorForJava implements MiniClientProcessorForJava {

    private static OpdMiniClientProcessorForJava instance;

    private HashSet<String> eventTypes = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(OpdDbConstants.DATE_FORMAT, Locale.US);

    public OpdMiniClientProcessorForJava(Context context) {
        super(context);
    }

    public static OpdMiniClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new OpdMiniClientProcessorForJava(context);
        }

        return instance;
    }

    @NonNull
    @Override
    public HashSet<String> getEventTypes() {
        if (eventTypes == null) {
            eventTypes = new HashSet<>();
            eventTypes.add(OpdConstants.EventType.CHECK_IN);
            eventTypes.add(OpdConstants.EventType.TEST_CONDUCTED);
            eventTypes.add(OpdConstants.EventType.DIAGNOSIS);
            eventTypes.add(OpdConstants.EventType.SERVICE_DETAIL);
            eventTypes.add(OpdConstants.EventType.TREATMENT);
            eventTypes.add(OpdConstants.EventType.CLOSE_OPD_VISIT);
            eventTypes.add(OpdConstants.EventType.OUTCOME);
            eventTypes.add(OpdConstants.EventType.MEDICAL_CONDITIONS_AND_HIV_DETAILS);
            eventTypes.add(OpdConstants.EventType.PREGNANCY_STATUS);
            eventTypes.add(OpdConstants.EventType.OPD_CLOSE);
            eventTypes.add(OpdConstants.EventType.DEATH);
        }

        return eventTypes;
    }

    @Override
    public boolean canProcess(@NonNull String eventType) {
        return getEventTypes().contains(eventType);
    }

    @Override
    public void processEventClient(@NonNull EventClient eventClient, @NonNull List<Event> unsyncEvents, @Nullable ClientClassification clientClassification) throws Exception {
        Event event = eventClient.getEvent();

        if (event.getEventType().equals(OpdConstants.EventType.CHECK_IN)) {
            if (eventClient.getClient() == null) {
                throw new CheckInEventProcessException(String.format("Client %s referenced by %s event does not exist", event.getBaseEntityId(), OpdConstants.EventType.CHECK_IN));
            }
            processCheckIn(eventClient, clientClassification);
            CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
        } else if (event.getEventType().equals(OpdConstants.EventType.TEST_CONDUCTED)) {
            try {
                processTestConducted(eventClient);
                CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
            } catch (Exception ex) {
                Timber.e(ex);
            }
        } else if (event.getEventType().equals(OpdConstants.EventType.DIAGNOSIS)) {
            try {
                processDiagnosis(eventClient);
                CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
            } catch (Exception ex) {
                Timber.e(ex);
            }
        } else if (event.getEventType().equals(OpdConstants.EventType.TREATMENT)) {
            try {
                processTreatment(eventClient);
                CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
            } catch (Exception ex) {
                Timber.e(ex);
            }
        } else if (event.getEventType().equals(OpdConstants.EventType.CLOSE_OPD_VISIT)) {
            try {
                processOpdCloseVisitEvent(event);
                CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (event.getEventType().equals(OpdConstants.EventType.OUTCOME)
                || event.getEventType().equals(OpdConstants.EventType.PREGNANCY_STATUS)
                || event.getEventType().equals(OpdConstants.EventType.MEDICAL_CONDITIONS_AND_HIV_DETAILS)
                || event.getEventType().equals(OpdConstants.EventType.SERVICE_DETAIL)) {
            try {
                if (eventClient.getClient() != null) {
                    processEvent(event, eventClient.getClient(), clientClassification);
                    CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        } else if (event.getEventType().equals(OpdConstants.EventType.OPD_CLOSE)) {
            processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
            CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
            unsyncEvents.add(event);
        } else if (event.getEventType().equals(OpdConstants.EventType.DEATH)) {
            processDeathEvent(eventClient);
            processEvent(eventClient.getEvent(), eventClient.getClient(), clientClassification);
            CoreLibrary.getInstance().context().getEventClientRepository().markEventAsProcessed(eventClient.getEvent().getFormSubmissionId());
            unsyncEvents.add(event);
        }
    }

    private void processDeathEvent(@NonNull EventClient eventClient) {
        Event event = eventClient.getEvent();
        String entityId = event.getBaseEntityId();

        HashMap<String, String> keyValues = new HashMap<>();
        generateKeyValuesFromEvent(event, keyValues);

        String encounterDateField = keyValues.get(OpdConstants.JSON_FORM_KEY.DATE_OF_DEATH);

        ContentValues values = new ContentValues();
        values.put(OpdConstants.KEY.DOD, encounterDateField);
        values.put(OpdConstants.KEY.DATE_REMOVED, OpdUtils.getTodaysDate());

        //Update REGISTER and FTS Tables
        AllCommonsRepository allCommonsRepository = OpdLibrary.getInstance().context().allCommonsRepositoryobjects(OpdDbConstants.Table.EC_CLIENT);
        if (allCommonsRepository != null) {
            allCommonsRepository.update(OpdDbConstants.Table.EC_CLIENT, values, entityId);
            allCommonsRepository.updateSearch(entityId);
        }
    }

    private void processOpdCloseVisitEvent(@NonNull Event event) {
        Map<String, String> mapDetails = event.getDetails();
        //update visit end date
        if (mapDetails != null) {
            OpdDetails opdDetails = new OpdDetails(event.getBaseEntityId(), mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_ID));
            opdDetails = OpdLibrary.getInstance().getOpdDetailsRepository().findOne(opdDetails);
            if (opdDetails != null) {
                opdDetails.setCurrentVisitEndDate(OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_END_DATE)));
                boolean result = OpdLibrary.getInstance().getOpdDetailsRepository().saveOrUpdate(opdDetails);
                if (result) {
                    Timber.d("Opd processOpdCloseVisitEvent for %s saved", event.getBaseEntityId());
                    return;
                }
                Timber.e("Opd processOpdCloseVisitEvent for %s not saved", event.getBaseEntityId());
            } else {
                Timber.e("Opd Details for %s not found", mapDetails.toString());
            }
        } else {
            Timber.e("Opd Details for %s not found, event details is null", event.getBaseEntityId());
        }
    }

    private void processTreatment(@NonNull EventClient eventClient) throws JSONException {
        Map<String, String> mapDetails = eventClient.getEvent().getDetails();
        HashMap<String, String> keyValues = new HashMap<>();
        generateKeyValuesFromEvent(eventClient.getEvent(), keyValues);
        JSONArray valueJsonArray = null;
        if (mapDetails.containsKey(OpdConstants.KEY.VALUE)) {
            String strValue = mapDetails.get(OpdConstants.KEY.VALUE);
            if (!StringUtils.isBlank(strValue)) {
                valueJsonArray = new JSONArray(strValue);
            }
        }
        if (valueJsonArray != null && valueJsonArray.length() > 0) {
            String medicine = keyValues.get(OpdConstants.JSON_FORM_KEY.MEDICINE);
            if (!TextUtils.isEmpty(medicine)) {
                for (int i = 0; i < valueJsonArray.length(); i++) {
                    OpdTreatmentDetail opdTreatmentDetail = new OpdTreatmentDetail();
                    opdTreatmentDetail.setBaseEntityId(eventClient.getEvent().getBaseEntityId());
                    opdTreatmentDetail.setVisitId(mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_ID));

                    JSONObject valueJsonObject = valueJsonArray.optJSONObject(i);

                    JSONObject propertyJsonObject = valueJsonObject.optJSONObject(JsonFormConstants.MultiSelectUtils.PROPERTY);
                    JSONObject meta = propertyJsonObject.optJSONObject(OpdConstants.JSON_FORM_KEY.META);
                    if (meta != null) {
                        opdTreatmentDetail.setDosage(meta.optString(OpdConstants.JSON_FORM_KEY.DOSAGE));
                        opdTreatmentDetail.setFrequency(meta.optString(OpdConstants.JSON_FORM_KEY.FREQUENCY));
                        opdTreatmentDetail.setDuration(meta.optString(OpdConstants.JSON_FORM_KEY.DURATION));
                        opdTreatmentDetail.setNote(meta.optString(OpdConstants.JSON_FORM_KEY.INFO));
                    }
                    opdTreatmentDetail.setMedicine(valueJsonObject.optString(JsonFormConstants.MultiSelectUtils.TEXT));
                    opdTreatmentDetail.setProperty(valueJsonArray.toString());
                    opdTreatmentDetail.setSpecialInstructions(keyValues.get(OpdConstants.JSON_FORM_KEY.SPECIAL_INSTRUCTIONS));
                    String treatmentType = keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE) == null ? "" : keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE);
                    //TODO spinner should not save the first item in the list, fix to be in native forms
                    if (!OpdConstants.JSON_FORM_EXTRA.TYPE_OF_TREATMENT_LABEL.equals(treatmentType.trim())) {
                        opdTreatmentDetail.setTreatmentType(keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE));
                        opdTreatmentDetail.setTreatmentTypeSpecify(keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE_SPECIFY));
                        saveOrUpdateTreatmentDetail(eventClient, opdTreatmentDetail);
                    }
                }
            }
        } else {
            OpdTreatmentDetail opdTreatmentDetail = new OpdTreatmentDetail();
            opdTreatmentDetail.setBaseEntityId(eventClient.getEvent().getBaseEntityId());
            opdTreatmentDetail.setVisitId(mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_ID));
            opdTreatmentDetail.setSpecialInstructions(keyValues.get(OpdConstants.JSON_FORM_KEY.SPECIAL_INSTRUCTIONS));
            opdTreatmentDetail.setTreatmentType(keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE));
            opdTreatmentDetail.setTreatmentTypeSpecify(keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE_SPECIFY));
            String treatmentType = keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE) == null ? "" : keyValues.get(OpdConstants.JSON_FORM_KEY.TREATMENT_TYPE);
            //TODO spinner should not save the first item in the list, fix to be in native forms
            if (!OpdConstants.JSON_FORM_EXTRA.TYPE_OF_TREATMENT_LABEL.equals(treatmentType.trim())) {
                saveOrUpdateTreatmentDetail(eventClient, opdTreatmentDetail);
            }
        }
    }

    private void saveOrUpdateTreatmentDetail(@NonNull EventClient eventClient, OpdTreatmentDetail opdTreatmentDetail) {
        opdTreatmentDetail.setCreatedAt(OpdUtils.convertDate(eventClient.getEvent().getEventDate().toLocalDate().toDate(), OpdDbConstants.DATE_FORMAT));
        boolean result = OpdLibrary.getInstance().getOpdTreatmentDetailRepository().save(opdTreatmentDetail);
        if (result) {
            Timber.i("Opd processTreatment for %s saved", eventClient.getEvent().getBaseEntityId());
            return;
        }
        Timber.e("Opd processTreatment for %s not saved", eventClient.getEvent().getBaseEntityId());
    }

    private void processDiagnosis(@NonNull EventClient eventClient) throws JSONException {
        Event event = eventClient.getEvent();
        Map<String, String> mapDetails = event.getDetails();
        String visitId = mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_ID);

        JSONArray valuesJsonArray = new JSONArray();
        if (mapDetails.containsKey(OpdConstants.KEY.VALUE)) {
            String strValue = mapDetails.get(OpdConstants.KEY.VALUE);
            if (!StringUtils.isBlank(strValue)) {
                valuesJsonArray = new JSONArray(strValue);
            }
        }

        HashMap<String, String> keyValues = new HashMap<>();
        generateKeyValuesFromEvent(event, keyValues);

        String diagnosis = keyValues.get(OpdConstants.JSON_FORM_KEY.DIAGNOSIS);
        String diagnosisType = keyValues.get(OpdConstants.JSON_FORM_KEY.DIAGNOSIS_TYPE);

        if (!TextUtils.isEmpty(diagnosis)) {
            if (valuesJsonArray.length() == 0) {
                OpdDiagnosisDetail opdDiagnosisDetail = new OpdDiagnosisDetail();
                opdDiagnosisDetail.setBaseEntityId(event.getBaseEntityId());
                opdDiagnosisDetail.setDiagnosis(diagnosis);
                opdDiagnosisDetail.setType(diagnosisType);
                opdDiagnosisDetail.setVisitId(visitId);
                opdDiagnosisDetail.setIsDiagnosisSame(keyValues.get(OpdConstants.JSON_FORM_KEY.DIAGNOSIS_SAME));
                saveOrUpdateDiagnosis(event, opdDiagnosisDetail);
            } else {
                for (int i = 0; i < valuesJsonArray.length(); i++) {
                    OpdDiagnosisDetail opdDiagnosisDetail = new OpdDiagnosisDetail();
                    opdDiagnosisDetail.setBaseEntityId(event.getBaseEntityId());
                    opdDiagnosisDetail.setDiagnosis(diagnosis);
                    opdDiagnosisDetail.setType(diagnosisType);
                    opdDiagnosisDetail.setVisitId(visitId);
                    JSONObject valueJsonObject = valuesJsonArray.optJSONObject(i);
                    JSONObject propertyJsonObject = valueJsonObject.optJSONObject(JsonFormConstants.MultiSelectUtils.PROPERTY);
                    opdDiagnosisDetail.setIcd10Code(propertyJsonObject.optString(OpdConstants.JSON_FORM_KEY.ICD10));
                    opdDiagnosisDetail.setCode(propertyJsonObject.optString(OpdConstants.JSON_FORM_KEY.CODE));
                    opdDiagnosisDetail.setDetails(propertyJsonObject.optString(OpdConstants.JSON_FORM_KEY.META));
                    opdDiagnosisDetail.setDisease(valueJsonObject.optString(JsonFormConstants.MultiSelectUtils.TEXT));
                    opdDiagnosisDetail.setIsDiagnosisSame(keyValues.get(OpdConstants.JSON_FORM_KEY.DIAGNOSIS_SAME));
                    saveOrUpdateDiagnosis(event, opdDiagnosisDetail);
                }
            }
        }
    }

    private void saveOrUpdateDiagnosis(Event event, OpdDiagnosisDetail opdDiagnosisDetail) {
        opdDiagnosisDetail.setCreatedAt(OpdUtils.convertDate(event.getEventDate().toLocalDate().toDate(), OpdDbConstants.DATE_FORMAT));
        boolean result = OpdLibrary.getInstance().getOpdDiagnosisDetailRepository().save(opdDiagnosisDetail);
        if (result) {
            Timber.i("Opd processDiagnosis for %s saved", event.getBaseEntityId());
        }
        Timber.e("Opd processDiagnosis for %s not saved", event.getBaseEntityId());
    }

    private void processTestConducted(@NonNull EventClient eventClient) {
        Event event = eventClient.getEvent();
        Map<String, String> mapDetails = event.getDetails();

        HashMap<String, String> keyValues = new HashMap<>();
        generateKeyValuesFromEvent(event, keyValues);

        String strRepeatingGroupMap = keyValues.get(OpdConstants.REPEATING_GROUP_MAP);
        if (StringUtils.isNotBlank(strRepeatingGroupMap)) {

            try {
                JSONObject jsonObject = new JSONObject(strRepeatingGroupMap);
                Iterator<String> repeatingGroupKeys = jsonObject.keys();
                while (repeatingGroupKeys.hasNext()) {
                    OpdTestConducted opdTestConducted = new OpdTestConducted();
                    JSONObject jsonTestObject = jsonObject.optJSONObject(repeatingGroupKeys.next());
                    Iterator<String> testStringIterator = jsonTestObject.keys();
                    HashMap<String, String> testNameResultMap = new HashMap<>();
                    while (testStringIterator.hasNext()) {
                        String resultKey = testStringIterator.next();
                        if (OpdConstants.DIAGNOSTIC_TEST.equals(resultKey)) {
                            opdTestConducted.setTestType(OpdUtils.removeHyphen(jsonTestObject.optString(resultKey)));
                        }
                        if (resultKey.startsWith(OpdConstants.DIAGNOSTIC_TEST_RESULT)) {
                            testNameResultMap.put(OpdUtils.createTestName(resultKey), jsonTestObject.optString(resultKey));
                        }
                    }

                    if (!opdTestConducted.getTestType().equals(OpdConstants.TYPE_OF_TEXT_LABEL)) {
                        for (Map.Entry<String, String> testsMapEntry : testNameResultMap.entrySet()) {
                            opdTestConducted.setResult(testsMapEntry.getValue());
                            opdTestConducted.setTestName(testsMapEntry.getKey());
                            opdTestConducted.setVisitId(mapDetails.get(OpdConstants.JSON_FORM_KEY.VISIT_ID));
                            opdTestConducted.setBaseEntityId(event.getBaseEntityId());
                            opdTestConducted.setCreatedAt(OpdUtils.convertDate(event.getEventDate().toLocalDate().toDate(), OpdDbConstants.DATE_FORMAT));
                            boolean result = OpdLibrary.getInstance().getOpdTestConductedRepository().save(opdTestConducted);
                            if (result) {
                                Timber.i("Opd processTestConducted for %s saved", event.getBaseEntityId());
                                continue;
                            }
                            Timber.e("Opd processTestConducted for %s not saved", event.getBaseEntityId());
                        }
                    }
                }
            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }

    protected void processCheckIn(@NonNull EventClient eventClient, @NonNull ClientClassification clientClassification) throws
            CheckInEventProcessException {
        HashMap<String, String> keyValues = new HashMap<>();
        Event event = eventClient.getEvent();
        // Todo: This might not work as expected when openmrs_entity_ids are added
        generateKeyValuesFromEvent(event, keyValues);


        String visitId = keyValues.get(OpdConstants.VISIT_ID);
        String visitDateString = keyValues.get(OpdConstants.VISIT_DATE);

        Date visitDate = null;

        try {
            visitDate = dateFormat.parse(visitDateString != null ? visitDateString : "");
        } catch (ParseException e) {
            Timber.e(e);

            visitDate = event.getEventDate().toDate();
        }

        if (visitDate != null && visitId != null) {
            // Start transaction
            OpdLibrary.getInstance().getRepository().getWritableDatabase().beginTransaction();

            boolean saved = saveVisit(event.getBaseEntityId(), event.getLocationId(), event.getProviderId(), visitId, visitDate);
            if (!saved) {
                abortTransaction();
                throw new CheckInEventProcessException(String.format("Visit with id %s could not be saved in the db. Fail operation failed", visitId));
            }

            try {
                processEvent(event, eventClient.getClient(), clientClassification);
            } catch (Exception e) {
                Timber.e(e);
            }

            //TODO: Make sure this does not override opd details which are latest

            // Update the detail
            OpdDetails opdDetails = generateOpdDetailsFromCheckInEvent(event, visitId, visitDate);
            saved = OpdLibrary.getInstance().getOpdDetailsRepository().saveOrUpdate(opdDetails);

            if (!saved) {
                VisitUtils.abortTransaction();
                throw new CheckInEventProcessException(String.format("OPD Details for visit with id %s updating status of client %s could not be saved in the db. Fail operation failed", visitId, event.getBaseEntityId()));
            }

            // Update the last interacted with of the user
            try {
                VisitUtils.updateLastInteractedWith(event, visitId);
            } catch (SQLiteException ex) {
                VisitUtils.abortTransaction();
                throw new CheckInEventProcessException("An error occurred saving last_interacted_with");
            }

            commitSuccessfulTransaction();
        } else {
            throw new CheckInEventProcessException(String.format("Check-in of event %s could not be processed because it the visitDate OR visitId is null", new Gson().toJson(event)));
        }
    }

    private boolean saveVisit(@NonNull String baseEntityId, @NonNull String
            locationId, @NonNull String providerId, @NonNull String visitId, @NonNull Date visitDate) {

        OpdVisit visit = new OpdVisit();
        visit.setId(visitId);
        visit.setBaseEntityId(baseEntityId);
        visit.setLocationId(locationId);
        visit.setProviderId(providerId);
        visit.setCreatedAt(new Date());
        visit.setVisitDate(visitDate);

        return OpdLibrary.getInstance().getVisitRepository().addVisit(visit);
    }

    private void generateKeyValuesFromEvent(@NonNull Event
                                                    event, @NonNull HashMap<String, String> keyValues) {
        List<Obs> obs = event.getObs();

        for (Obs observation : obs) {
            String key = observation.getFormSubmissionField();
            List<Object> values = observation.getValues();

            if (values.size() > 0) {
                String value = (String) values.get(0);

                if (!TextUtils.isEmpty(value)) {

                    if (values.size() > 1) {
                        value = values.toString();
                    }

                    keyValues.put(key, value);
                    continue;
                }
            }

            List<Object> humanReadableValues = observation.getHumanReadableValues();
            if (humanReadableValues.size() > 0) {
                String value = (String) humanReadableValues.get(0);

                if (!TextUtils.isEmpty(value)) {

                    if (values.size() > 1) {
                        value = values.toString();
                    }

                    keyValues.put(key, value);
                    continue;
                }
            }
        }
    }

    private void abortTransaction() {
        if (OpdLibrary.getInstance().getRepository().getWritableDatabase().inTransaction()) {
            OpdLibrary.getInstance().getRepository().getWritableDatabase().endTransaction();
        }
    }

    private void commitSuccessfulTransaction() {
        if (OpdLibrary.getInstance().getRepository().getWritableDatabase().inTransaction()) {
            OpdLibrary.getInstance().getRepository().getWritableDatabase().setTransactionSuccessful();
            OpdLibrary.getInstance().getRepository().getWritableDatabase().endTransaction();
        }
    }

    @NonNull
    private OpdDetails generateOpdDetailsFromCheckInEvent(@NonNull Event event, String
            visitId, Date visitDate) {
        OpdDetails opdDetails = new OpdDetails();
        opdDetails.setBaseEntityId(event.getBaseEntityId());
        opdDetails.setCurrentVisitId(visitId);
        opdDetails.setCurrentVisitStartDate(visitDate);
        opdDetails.setCurrentVisitEndDate(null);
        opdDetails.setCreatedAt(event.getEventDate().toLocalDate().toDate());

        // This code and flag is useless now - Todo: Work on disabling the flag in the query by deleting the current_visit_date & change this flag to diagnose_and_treat_ongoing
        // Set Pending diagnose and treat if we have not lapsed the max check-in duration in minutes set in the opd library configuration
        if (visitDate != null) {
            long timeDifferenceInMinutes = ((getDate().getTime()) - visitDate.getTime()) / (60 * 1000);
            opdDetails.setPendingDiagnoseAndTreat(timeDifferenceInMinutes <= OpdLibrary.getInstance().getOpdConfiguration().getMaxCheckInDurationInMinutes());
        }

        return opdDetails;
    }

    @VisibleForTesting
    Date getDate() {
        return new Date();
    }

    @Override
    public boolean unSync(@Nullable List<Event> events) {
        return false;
    }
}