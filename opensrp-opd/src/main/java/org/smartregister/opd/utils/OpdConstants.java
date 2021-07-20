package org.smartregister.opd.utils;

import org.smartregister.AllConstants;

public class OpdConstants extends AllConstants {

    public static final String SEX = "Sex";
    public static final String CLIENT_TYPE = "client";
    public static final String CONFIG = "opd_register";
    public static final String TYPE_OF_TEXT_LABEL = "The type of test conducted";
    public static final String DIAGNOSTIC_TEST_RESULT = "diagnostic_test_result";
    public static final String DIAGNOSTIC_TEST = "diagnostic_test";
    public static final String REPEATING_GROUP_MAP = "repeatingGroupMap";
    public static final String BIND_TYPE = "bind_type";
    public static final String VISIT_ID = "visit_id";
    public static final String VISIT_DATE = "visit_date";
    public static final String METADATA = "metadata";

    public static final String DETAILS = "details";
    public static final String RELATIONSHIPS = "relationships";

    public interface StepTitle {
        String TEST_CONDUCTED = "Test Conducted";
        String DIAGNOSIS = "Diagnosis";
        String TREATMENT = "Treatment";
        String SERVICE_FEE = "Service Fee";
    }

    public interface IntentKey {
        String BASE_ENTITY_ID = "base-entity-id";
        String CLIENT_MAP = "client_map";
        String CLIENT_OBJECT = "common_person_object_client";
        String CONTACT_NO = "contact_number";
        String ENTITY_TABLE = "entity_table";
    }

    public interface Event {

        interface Visit {
            interface Detail {
                String VISIT_ID = "visitId";
                String VISIT_DATE = "visitDate";
            }
        }

        interface CheckIn {
            interface Detail {
                String VISIT_ID = "visitId";
                String VISIT_DATE = "visitDate";
            }
        }
    }

    public interface FactKey {

        String VISIT_TO_APPOINTMENT_DATE = "visit_to_appointment_date";

        interface ProfileOverview {
            String PREGNANCY_STATUS = "pregnancy_status";
            String IS_PREVIOUSLY_TESTED_HIV = "is_previously_tested_hiv";
            String PREVIOUSLY_HIV_STATUS_RESULTS = "previous_hiv_status";
            String PATIENT_ON_ART = "patient_on_art";
            String HIV_STATUS = "hiv_status";
            String CURRENT_HIV_STATUS = "current_hiv_status";
            String VISIT_TYPE = "visit_type";
            String APPOINTMENT_SCHEDULED_PREVIOUSLY = "previous_appointment";
            String DATE_OF_APPOINTMENT = "date_of_appointment";
            String PENDING_DIAGNOSE_AND_TREAT = "pending_diagnose_and_treat";
        }

        interface OpdVisit {
            String VISIT_DATE = "visit_date";

            String TEST_NAME = "test_name";
            String TEST_RESULT = "test_result";

            String TEST_TYPE = "test_type";
            String TEST_TYPE_LABEL = "test_type_label";

            String TESTS = "tests";
            String TESTS_LABEL = "tests_label";

            String DIAGNOSIS = "diagnosis";
            String DIAGNOSIS_LABEL = "diagnosis_label";

            String DIAGNOSIS_SAME = "diagnosis_same";
            String DIAGNOSIS_SAME_LABEL = "diagnosis_same_label";

            String DIAGNOSIS_TYPE = "diagnosis_type";
            String DIAGNOSIS_TYPE_LABEL = "diagnosis_type_label";

            String DISEASE_CODE = "disease_code";
            String DISEASE_CODE_LABEL = "disease_code_label";

            String TREATMENT = "treatment";
            String TREATMENT_LABEL = "treatment_label";

            String TREATMENT_TYPE = "treatment_type";
            String TREATMENT_TYPE_LABEL = "treatment_type_label";

            String TREATMENT_TYPE_SPECIFY = "treatment_type_specify";
            String TREATMENT_TYPE_SPECIFY_LABEL = "treatment_type_specify_label";

            String DISCHARGED_ALIVE = "discharged_alive";
            String DISCHARGED_ALIVE_LABEL = "discharged_alive_label";

            String DISCHARGED_HOME = "discharged_home";
            String DISCHARGED_HOME_LABEL = "discharged_home_label";

            String REFERRAL = "referral";
            String REFERRAL_LABEL = "referral_label";

            String REFERRAL_LOCATION = "referral_location";
            String REFERRAL_LOCATION_LABEL = "referral_location_label";

            String REFERRAL_LOCATION_SPECIFY = "referral_location_specify";
            String REFERRAL_LOCATION_SPECIFY_LABEL = "referral_location_specify_label";

        }

    }

    public interface JsonFormField {
        String PATIENT_GENDER = "patient_gender";
        String PREGNANCY_STATUS = "pregnancy_status";
        String HIV_TESTED = "hiv_tested";
        String HIV_PREVIOUS_STATUS = "hiv_prev_status";
        String IS_PATIENT_TAKING_ART = "hiv_prev_pos_art";
        String CURRENT_HIV_STATUS = "current_hiv_status";
        String VISIT_TYPE = "visit_type";
        String APPOINTMENT_DUE = "appointment_due";
        String APPOINTMENT_DUE_DATE = "appointment_due_date";
        String APPOINTMENT_DUE_INLESS_TIME = "appointment_done_inless_time";
    }

    public interface JsonFormWidget {
        String MULTI_SELECT_DRUG_PICKER = "multi_select_drug_picker";
    }

    public interface SettingsConfig {
        String OPD_MEDICINE = "opd_medicine";
        String OPD_DISEASE_CODES = "opd_disease_codes";
    }

    public interface OpdMultiDrugPicker {
        String CONFIRMED_ID = "consumed-id";
        String PRESUMED_ID = "presumed-id";
    }

    public interface ColumnMapKey {
        String REGISTER_ID = "register_id";
        String REGISTER_TYPE = "register_type";
    }

    public interface DateFormat {
        String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
        String d_MMM_yyyy = "d MMM yyyy";
        String d_MMM_yyyy_hh_mm_ss = "d MMM yyyy HH:mm:ss";
        String YYYY_MM_DD = "yyyy-MM-dd";
    }

    public interface Form {
        String OPD_DIAGNOSIS_AND_TREAT = "opd_diagnose_and_treat";
        String OPD_CHECK_IN = "opd_checkin";
        String OPD_CLOSE = "opd_close";
    }

    public interface FormValue {
        String IS_DOB_UNKNOWN = "isDobUnknown";
        String IS_ENROLLED_IN_MESSAGES = "isEnrolledInSmsMessages";
        String OTHER = "other";
    }

    public interface RegisterType {
        String OPD = "opd";
    }

    public interface ClientMapKey {
        String GENDER = "gender";
    }

    public static class JSON_FORM_KEY {
        public static final String DEATH_DATE_APPROX = "deathdateApprox";
        public static final String OPTIONS = "options";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String DOB = "dob";
        public static final String DOB_UNKNOWN = "dob_unknown";

        public static final String AGE_ENTERED = "age_entered";
        public static final String DOB_ENTERED = "dob_entered";
        public static final String ADDRESS_WIDGET_KEY = "home_address";
        public static final String NAME = "opd_registration";
        public static final String REMINDERS = "reminders";

        public static final String SERVICE_FEE = "service_fee";
        public static final String VISIT_ID = "visitId";
        public static final String MEDICINE = "medicine";
        public static final String DOSAGE = "dosage";
        public static final String DURATION = "duration";
        public static final String INFO = "info";
        public static final String META = "meta";
        public static final String DIAGNOSIS = "diagnosis";
        public static final String DIAGNOSIS_TYPE = "diagnosis_type";
        public static final String DISEASE_CODE = "disease_code";
        public static final String CODE = "code";
        public static final String ICD10 = "icd10";
        public static final String DIAGNOSTIC_TEST_RESULT_SPINNER = "diagnostic_test_result_spinner";
        public static final String DIAGNOSTIC_TEST_OTHER = "diagnostic_test_other";
        public static final String DIAGNOSTIC_TEST = "diagnostic_test";
        public static final String DIAGNOSTIC_TEST_RESULT_SPECIFY = "diagnostic_test_result_specify";
        public static final String ID = "ID";
        public static final String VISIT_END_DATE = "visit_end_date";

        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String BHT_ID = "bht_mid";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String NATIONAL_ID = "national_id";
        public static final String HOME_ADDRESS = "home_address";
        public static final String AGE_CALCULATED = "age_calculated";
        public static final String GENDER = "gender";
        public static final String ENCOUNTER_TYPE = "encounter_type";
        public static final String ENTITY_ID = "entity_id";
        public static final String ENCOUNTER = "encounter";
        public static final String ENCOUNTER_LOCATION = "encounter_location";
        public static final String UNIQUE_ID = "unique_id";
        public static final String AGE = "age";
        public static final String OPD_EDIT_FORM_TITLE = "Update Opd Registration";
        public static final String FORM_TITLE = "title";
        public static final String OPENSRP_ID = "opensrp_id";
        public static final String FIELDS = "fields";
        public static final String VALUE = "value";

        public static final String DIAGNOSTIC_TEST_RESULT_GLUCOSE = "diagnostic_test_result_glucose";
        public static final String DIAGNOSTIC_TEST_RESULT_SPINNER_BLOOD_TYPE = "diagnostic_test_result_spinner_blood_type";
        public static final String FREQUENCY = "frequency";
        public static final String HOME_FACILITY = "Home_Facility";
        public static final String SPECIAL_INSTRUCTIONS = "special_instructions";
        public static final String TREATMENT_TYPE = "treatment_type";
        public static final String TREATMENT_TYPE_SPECIFY = "treatment_type_specify";
        public static final String DIAGNOSIS_SAME = "diagnosis_same";
        public static final String VILLAGE = "village";
        public static final String ATTRIBUTES = "attributes";
        public static final String DATE_REMOVED = "date_removed";
        public static final String DATE_OF_DEATH = "date_of_death";
        public static final String OPD_CLOSE_REASON = "opd_close_reason";
        public static String TESTS_REPEATING_GROUP = "tests_repeating_group";
        public static final String DEATH_DATE = "deathdate";
    }

    public static class JsonFormNameUtils {
        public static final String CHECK_IN = "opd_checkin";
    }

    public static class JSON_FORM_EXTRA {
        public static final String NEXT = "next";
        public static final String STEP = "step";
        public static final String JSON = "json";
        public static final String STEP1 = "step1";
        public static final String STEP2 = "step2";
        public static final String STEP3 = "step3";
        public static final String STEP4 = "step4";

        public static final String ZEIR_ID = "zeir_id";
        public static final String ID = "id";
        public static final String TYPE_OF_TREATMENT_LABEL = "The type of treatment provided";
    }

    public static class OPENMRS {
        public static final String ENTITY = "openmrs_entity";
        public static final String ENTITY_ID = "openmrs_entity_id";
    }

    public static final class KEY {
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String PHOTO = "photo";
        public static final String LOOK_UP = "look_up";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String DOB = "dob";//Date Of Birth
        public static final String OPENSRP_ID = "opensrp_id";
        public static final String RELATIONALID = "relationalid";
        public static final String NATIONAL_ID = "national_id";
        public static final String GENDER = "gender";
        public static final String ID = "id";
        public static final String DOD = "dod";
        public static final String DATE_REMOVED = "date_removed";
        public static final String DIED = "died";
        public static final String REFERRAL_FACILITY = "referral_facility_specify";
        public static final String REFERRAL_LOCATION = "referral_location_med";
        public static final String REFERRAL_LOCATION_LAB = "referral_location_med_lab";
    }

    public static class ENTITY {
        public static final String PERSON = "person";
    }

    public static class BOOLEAN_INT {
        public static final int TRUE = 1;
    }

    public static class FormActivity {
        public static final String EnableOnCloseDialog = "EnableOnCloseDialog";
    }

    public static final class EventType {
        public static final String OPD_REGISTRATION = "Opd Registration";
        public static final String UPDATE_OPD_REGISTRATION = "Update Opd Registration";
        public static final String CHECK_IN = "OPD Check-In";
        public static final String DIAGNOSIS_AND_TREAT = "OPD Diagnosis and Treatment";
        public static final String DIAGNOSIS = "OPD Diagnosis";
        public static final String TREATMENT = "OPD Treatment";
        public static final String TEST_CONDUCTED = "OPD Test Conducted";
        public static final String SERVICE_DETAIL = "OPD Service Detail";
        public static final String VISIT = "OPD Visit";
        public static final String PREGNANCY_STATUS = "OPD Pregnancy Status";
        public static final String MEDICAL_CONDITIONS_AND_HIV_DETAILS = "OPD Medical Conditions And HIV Details";
        public static final String OUTCOME = "OPD Outcome";
        public static final String CLOSE_OPD_VISIT = "OPD Close Visit";
        public static final String OPD_CLOSE = "OPD Close";
        public static final String DEATH = "Death";
    }


    //New OPD Module WorkFlow
    public interface DateTimeFormat {
        String HH_MM_AMPM = "h:mm a";
        String YYYY_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
        String dd_MMM_yyyy = "dd MMM yyyy";
        String hh_mm_ss = "hh:mm:ss";
        String hh_mm = "hh:mm";
        String dd_MM_yyyy = "dd/MM/yyyy";
        String ddMMyyyy = "ddMMyyyy";
        String yyyy_MM_dd = "yyyy-MM-dd";
    }


    public static class OpdModuleEventConstants {
        public static final String OPD_REGISTRATION = "OPD_Registration";
        public static final String OPD_CHECK_IN = "OPD_Check_in";
        public static final String OPD_VITAL_DANGER_SIGNS_CHECK = "OPD_Vital_Danger_signs_check";
        public static final String OPD_DIAGNOSIS = "OPD_Diagnosis";
        public static final String OPD_LABORATORY = "OPD_Laboratory";
        public static final String OPD_TREATMENT = "OPD_Treatment";
        public static final String OPD_PHARMACY = "OPD_Pharmacy";
        public static final String OPD_FINAL_OUTCOME = "OPD_Final_Outcome";
        public static final String OPD_SERVICE_CHARGE= "OPD_Service_Charge";
        public static final String OPD_VISIT_GROUP = "opd_visit_group";

    }

    public interface Properties {
        String TASK_IDENTIFIER = "taskIdentifier";
        String TASK_BUSINESS_STATUS = "taskBusinessStatus";
        String TASK_STATUS = "taskStatus";
        String TASK_CODE = "taskCode";
        String LOCATION_UUID = "locationUUID";
        String LOCATION_VERSION = "locationVersion";
        String LOCATION_TYPE = "locationType";
        String LOCATION_PARENT = "locationParent";
        String LOCATION_ID = "location_id";
        String FEATURE_SELECT_TASK_BUSINESS_STATUS = "featureSelectTaskBusinessStatus";
        String BASE_ENTITY_ID = "baseEntityId";
        String FORM_SUBMISSION_ID = "formSubmissionId";
        String STRUCTURE_NAME = "structure_name";
        String APP_VERSION_NAME = "appVersionName";
        String FORM_VERSION = "form_version";
        String TASK_CODE_LIST = "task_code_list";
        String FAMILY_MEMBER_NAMES = "family_member_names";
        String PLAN_IDENTIFIER = "planIdentifier";
        String LOCATION_STATUS = "status";
        String LOCATION_NAME = "name";
    }

    public interface JsonForm {
        // TODO
        String OPD_CHECKIN = "json.form/opd_checkin.json";
        String VITAL_DANGER_SIGNS = "json.form/opd_danger_signs.json";
        String DIAGNOSIS = "json.form/opd_diagnosis.json";
        String LAB_RESULTS = "json.form/opd_laboratory.json";
        String TREATMENT = "json.form/opd_treatment.json";
        String PHARMACY = "json.form/opd_pharmacy.json";
        String FINAL_OUTCOME = "json.form/opd_outcome.json";
        String SERVICE_FEE = "json.form/opd_service_fee.json";
    }

    public static class TableNameConstants {
        public static final String ALL_CLIENTS = "ec_client";
    }


    public static class PropertyConstants {
        public static final String USE_NEW_OPD_MODULE = "use.new.opd.module";
    }

    public static class FileUtils {
        public static final String OPD_GLOBALS = "opd-globals.yml";
    }
}
