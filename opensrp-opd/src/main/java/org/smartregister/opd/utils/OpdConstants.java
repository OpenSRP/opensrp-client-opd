package org.smartregister.opd.utils;

import org.smartregister.AllConstants;

import java.text.SimpleDateFormat;

public class OpdConstants extends AllConstants {

    public static final String SEX = "Sex";
    public static final String HOME_FACILITY = "Home_Facility";
    public static final String CLIENT_TYPE = "client";
    public static final String CONFIG = "opd_register";

    public static class JSON_FORM_KEY {
        public static final String OPTIONS = "options";
        public static final String UNIQUE_ID = "unique_id";
        public static final String LAST_INTERACTED_WITH = "last_interacted_with";
        public static final String DOB = "dob";
        public static final String DOB_UNKNOWN = "dob_unknown";
        public static final String AGE = "age";

        public static final String AGE_ENTERED = "age_entered" ;
        public static final String DOB_ENTERED = "dob_entered";
        public static final String ADDRESS_WIDGET_KEY = "home_address";
        public static final String NAME = "opd_registration";
    }

    public static class JSON_FORM_EXTRA {
        public static final String NEXT = "next";
        public static final String JSON = "json";
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
        public static final String ZEIR_ID = "zeir_id";
        public static final String MER_ID = "mer_id";
        public static final String DATE_REMOVED = "date_removed";
        public static final String RELATIONALID = "relationalid";
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

        public static final String BITRH_REGISTRATION = "Birth Registration";
        public static final String NEW_WOMAN_REGISTRATION = "New Woman Registration";
        public static final String OPD_REGISTRATION = "Opd Registration";
        public static final String UPDATE_OPD_REGISTRATION = "Update Opd Registration";
    }
}