package org.smartregister.opd;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.configuration.OpdFormProcessor;
import org.smartregister.opd.domain.YamlConfig;
import org.smartregister.opd.domain.YamlConfigItem;
import org.smartregister.opd.helper.OpdRulesEngineHelper;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdVisit;
import org.smartregister.opd.repository.OpdCheckInRepository;
import org.smartregister.opd.repository.OpdDetailsRepository;
import org.smartregister.opd.repository.OpdDiagnosisAndTreatmentFormRepository;
import org.smartregister.opd.repository.OpdDiagnosisDetailRepository;
import org.smartregister.opd.repository.OpdTestConductedRepository;
import org.smartregister.opd.repository.OpdTreatmentDetailRepository;
import org.smartregister.opd.repository.OpdVisitRepository;
import org.smartregister.opd.repository.OpdVisitSummaryRepository;
import org.smartregister.opd.repository.VisitDetailsRepository;
import org.smartregister.opd.repository.VisitRepository;
import org.smartregister.opd.utils.FilePath;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdJsonFormUtils;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.AppProperties;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.NativeFormProcessor;
import org.smartregister.view.activity.DrishtiApplication;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import id.zelory.compressor.Compressor;

import static org.smartregister.opd.utils.OpdJsonFormUtils.METADATA;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-13
 */

public class OpdLibrary {

    private static OpdLibrary instance;
    private final Context context;
    private final Repository repository;
    private OpdConfiguration opdConfiguration;
    private ECSyncHelper syncHelper;

    private UniqueIdRepository uniqueIdRepository;
    private OpdCheckInRepository checkInRepository;
    private OpdVisitRepository visitRepository;
    private OpdDetailsRepository opdDetailsRepository;
    private OpdDiagnosisAndTreatmentFormRepository opdDiagnosisAndTreatmentFormRepository;
    private OpdDiagnosisDetailRepository opdDiagnosisDetailRepository;
    private OpdTreatmentDetailRepository opdTreatmentDetailRepository;
    private OpdTestConductedRepository opdTestConductedRepository;
    private OpdVisitSummaryRepository opdVisitSummaryRepository;
    private EventClientRepository eventClientRepository;

    private Compressor compressor;
    private int applicationVersion;
    private int databaseVersion;

    private VisitRepository opdVisitRepository;
    private VisitDetailsRepository visitDetailsRepository;
    private NativeFormProcessorFactory formProcessorFactory;

    private Yaml yaml;

    private OpdRulesEngineHelper opdRulesEngineHelper;

    protected OpdLibrary(@NonNull Context context, @NonNull OpdConfiguration opdConfiguration
            , @NonNull Repository repository, int applicationVersion, int databaseVersion) {
        this.context = context;
        this.opdConfiguration = opdConfiguration;
        this.repository = repository;
        this.applicationVersion = applicationVersion;
        this.databaseVersion = databaseVersion;

        // Initialize configs processor
        initializeYamlConfigs();
    }

    public static void init(Context context, @NonNull Repository repository, @NonNull OpdConfiguration opdConfiguration
            , int applicationVersion, int databaseVersion) {
        if (instance == null) {
            instance = new OpdLibrary(context, opdConfiguration, repository, applicationVersion, databaseVersion);
        }
    }

    public static void initializeFormFactory(NativeFormProcessorFactory nativeFormProcessorFactory){
        instance.formProcessorFactory = nativeFormProcessorFactory;
    }

    public NativeFormProcessorFactory getFormProcessorFactory(){
        return instance.formProcessorFactory;
    }

    public static OpdLibrary getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance does not exist!!! Call "
                    + OpdLibrary.class.getName()
                    + ".init method in the onCreate method of "
                    + "your Application class");
        }
        return instance;
    }

    @NonNull
    public Context context() {
        return context;
    }

    @NonNull
    public UniqueIdRepository getUniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository();
        }
        return uniqueIdRepository;
    }

    @NonNull
    public OpdCheckInRepository getCheckInRepository() {
        if (checkInRepository == null) {
            checkInRepository = new OpdCheckInRepository();
        }

        return checkInRepository;
    }

    @NonNull
    public OpdVisitRepository getVisitRepository() {
        if (visitRepository == null) {
            visitRepository = new OpdVisitRepository();
        }

        return visitRepository;
    }

    @NonNull
    public OpdDetailsRepository getOpdDetailsRepository() {
        if (opdDetailsRepository == null) {
            opdDetailsRepository = new OpdDetailsRepository();
        }
        return opdDetailsRepository;
    }

    @NonNull
    public OpdDiagnosisAndTreatmentFormRepository getOpdDiagnosisAndTreatmentFormRepository() {
        if (opdDiagnosisAndTreatmentFormRepository == null) {
            opdDiagnosisAndTreatmentFormRepository = new OpdDiagnosisAndTreatmentFormRepository();
        }
        return opdDiagnosisAndTreatmentFormRepository;
    }

    @NonNull
    public OpdDiagnosisDetailRepository getOpdDiagnosisDetailRepository() {
        if (opdDiagnosisDetailRepository == null) {
            opdDiagnosisDetailRepository = new OpdDiagnosisDetailRepository();
        }
        return opdDiagnosisDetailRepository;
    }

    @NonNull
    public OpdTestConductedRepository getOpdTestConductedRepository() {
        if (opdTestConductedRepository == null) {
            opdTestConductedRepository = new OpdTestConductedRepository();
        }
        return opdTestConductedRepository;
    }

    @NonNull
    public OpdTreatmentDetailRepository getOpdTreatmentDetailRepository() {
        if (opdTreatmentDetailRepository == null) {
            opdTreatmentDetailRepository = new OpdTreatmentDetailRepository();
        }
        return opdTreatmentDetailRepository;
    }

    @NonNull
    public OpdVisitSummaryRepository getOpdVisitSummaryRepository() {
        if (opdVisitSummaryRepository == null) {
            opdVisitSummaryRepository = new OpdVisitSummaryRepository();
        }
        return opdVisitSummaryRepository;
    }

    public EventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository();
        }
        return eventClientRepository;
    }

    @NonNull
    public Repository getRepository() {
        return repository;
    }


    @NonNull
    public ECSyncHelper getEcSyncHelper() {
        if (syncHelper == null) {
            syncHelper = ECSyncHelper.getInstance(context().applicationContext());
        }
        return syncHelper;
    }

    @NonNull
    public OpdConfiguration getOpdConfiguration() {
        return opdConfiguration;
    }

    @NonNull
    public Compressor getCompressor() {
        if (compressor == null) {
            compressor = new Compressor(context().applicationContext());
        }

        return compressor;
    }

    @NonNull
    public ClientProcessorForJava getClientProcessorForJava() {
        return DrishtiApplication.getInstance().getClientProcessor();
    }


    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public int getApplicationVersion() {
        return applicationVersion;
    }

    private void initializeYamlConfigs() {
        Constructor constructor = new Constructor(YamlConfig.class);
        TypeDescription customTypeDescription = new TypeDescription(YamlConfig.class);
        customTypeDescription.addPropertyParameters(YamlConfigItem.GENERIC_YAML_ITEMS, YamlConfigItem.class);
        constructor.addTypeDescription(customTypeDescription);
        yaml = new Yaml(constructor);
    }

    @NonNull
    public Iterable<Object> readYaml(@NonNull String filename) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(
                context.applicationContext().getAssets().open((FilePath.FOLDER.CONFIG_FOLDER_PATH + filename)));
        return yaml.loadAll(inputStreamReader);
    }

    @NonNull
    public OpdRulesEngineHelper getOpdRulesEngineHelper() {
        if (opdRulesEngineHelper == null) {
            opdRulesEngineHelper = new OpdRulesEngineHelper();
        }

        return opdRulesEngineHelper;
    }

    @NonNull
    public Event processOpdCheckInForm(@NonNull String eventType, String jsonString, @Nullable Intent data) throws JSONException {
        JSONObject jsonFormObject = new JSONObject(jsonString);

        JSONObject stepOne = jsonFormObject.getJSONObject(OpdJsonFormUtils.STEP1);
        JSONArray fieldsArray = stepOne.getJSONArray(OpdJsonFormUtils.FIELDS);

        HashMap<String, String> injectedFields = new HashMap<>();
        injectedFields.put("visit_id", JsonFormUtils.generateRandomUUIDString());
        injectedFields.put("visit_date", OpdUtils.convertDate(new Date(), OpdDbConstants.DATE_FORMAT));

        OpdJsonFormUtils.populateInjectedFields(jsonFormObject, injectedFields);

        FormTag formTag = OpdJsonFormUtils.formTag(OpdUtils.getAllSharedPreferences());

        String baseEntityId = OpdUtils.getIntentValue(data, OpdConstants.IntentKey.BASE_ENTITY_ID);
        String entityTable = OpdUtils.getIntentValue(data, OpdConstants.IntentKey.ENTITY_TABLE);
        Event opdCheckinEvent = OpdJsonFormUtils.createEvent(fieldsArray, jsonFormObject.getJSONObject(METADATA)
                , formTag, baseEntityId, eventType, entityTable)
                .withChildLocationId(OpdLibrary.getInstance().context().allSharedPreferences().fetchCurrentLocality());

        AllSharedPreferences allSharedPreferences = OpdUtils.getAllSharedPreferences();
        String providerId = allSharedPreferences.fetchRegisteredANM();
        opdCheckinEvent.setProviderId(providerId);
        opdCheckinEvent.setLocationId(OpdJsonFormUtils.locationId(allSharedPreferences));
        opdCheckinEvent.setFormSubmissionId(opdCheckinEvent.getFormSubmissionId());

        opdCheckinEvent.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        opdCheckinEvent.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));

        opdCheckinEvent.setClientDatabaseVersion(OpdLibrary.getInstance().getDatabaseVersion());
        opdCheckinEvent.setClientApplicationVersion(OpdLibrary.getInstance().getApplicationVersion());

        return opdCheckinEvent;
    }

    public List<Event> processOpdForm(@NonNull String jsonString, @NonNull Intent data) throws JSONException {
        JSONObject jsonFormObject = new JSONObject(jsonString);
        OpdFormProcessor<List<Event>> opdFormProcessor = OpdLibrary.getInstance()
                .getOpdConfiguration()
                .getOpdFormProcessingClass(jsonFormObject.optString(JsonFormConstants.ENCOUNTER_TYPE));
        return opdFormProcessor.processForm(jsonFormObject, data);
    }

    /**
     * This method enables us to configure how-long ago we should consider a valid check-in so that
     * we enable the next step which is DIAGNOSE & TREAT. This method returns the latest date that a check-in
     * should be so that it can be considered for moving to DIAGNOSE & TREAT
     *
     * @return Date
     */
    @NonNull
    public Date getLatestValidCheckInDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);

        return calendar.getTime();
    }

    public boolean isPatientInTreatedState(@NonNull String strVisitEndDate) {
        Date visitEndDate = OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD_HH_MM_SS, strVisitEndDate);
        if (visitEndDate != null) {
            return isPatientInTreatedState(visitEndDate);
        }

        return false;
    }

    public boolean isPatientInTreatedState(@NonNull Date visitEndDate) {
        // Get the midnight of that day when the visit happened
        Calendar date = Calendar.getInstance();
        date.setTime(visitEndDate);
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        // next day
        date.add(Calendar.DAY_OF_MONTH, 1);
        return getDateNow().before(date.getTime());
    }

    @VisibleForTesting
    @NonNull
    protected Date getDateNow() {
        return new Date();
    }

    /**
     * This checks if the patient can perform a Check-In evaluated based on their latest visit details & opd details. This however does not consider the TREATED status
     * which appears after a visit is completed within the same day. If you need to consider the TREATED status, you should first call {@link #isPatientInTreatedState(Date)}
     * and then call this method if the result is false.
     *
     * @param visit
     * @param opdDetails
     * @return
     */
    public boolean canPatientCheckInInsteadOfDiagnoseAndTreat(@Nullable OpdVisit visit, @Nullable OpdDetails opdDetails) {
        Date latestValidCheckInDate = OpdLibrary.getInstance().getLatestValidCheckInDate();

        // If we are past the 24 hours or so, then the status should be check-in
        // If your opd
        return visit == null || visit.getVisitDate().before(latestValidCheckInDate) || (opdDetails != null && opdDetails.getCurrentVisitEndDate() != null);
    }

    public boolean isClientCurrentlyCheckedIn(@Nullable OpdVisit opdVisit, @Nullable OpdDetails opdDetails) {
        return !canPatientCheckInInsteadOfDiagnoseAndTreat(opdVisit, opdDetails);
    }

    public AppProperties getProperties() {
        return CoreLibrary.getInstance().context().getAppProperties();
    }

    public VisitRepository visitRepository() {
        if (opdVisitRepository == null) {
            opdVisitRepository = new VisitRepository();
        }
        return opdVisitRepository;
    }

    public VisitDetailsRepository visitDetailsRepository() {
        if (visitDetailsRepository == null) {
            visitDetailsRepository = new VisitDetailsRepository();
        }
        return visitDetailsRepository;
    }


    public interface NativeFormProcessorFactory {
        NativeFormProcessor createInstance(String jsonString) throws JSONException ;
        NativeFormProcessor createInstance(JSONObject jsonObject);
        NativeFormProcessor createInstanceFromAsset(String filePath) throws JSONException ;
    }
}
