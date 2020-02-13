package org.smartregister.opd.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.jeasy.rules.api.Facts;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.Obs;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.pojo.CompositeObs;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.util.FormUtils;
import org.smartregister.util.JsonFormUtils;
import org.smartregister.util.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-13
 */

public class OpdUtils extends org.smartregister.util.Utils {

    private static final String OTHER_SUFFIX = ", other]";
    private static FormUtils formUtils;

    public static float convertDpToPixel(float dp, @NonNull Context context) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @NonNull
    public static String fillTemplate(boolean isHtml, @NonNull String stringValue, @NonNull Facts facts) {
        String stringValueResult = stringValue;
        while (stringValueResult.contains("{")) {
            String key = stringValueResult.substring(stringValueResult.indexOf("{") + 1, stringValueResult.indexOf("}"));
            String value = processValue(key, facts);
            stringValueResult = stringValueResult.replace("{" + key + "}", value).replaceAll(", $", "").trim();
        }

        //Remove unnecessary commas by cleaning the returned string
        return isHtml ? stringValueResult : cleanValueResult(stringValueResult);
    }

    public static String fillTemplate(@NonNull String stringValue, @NonNull Facts facts) {
        return fillTemplate(false, stringValue, facts);
    }

    public static boolean isTemplate(@NonNull String stringValue) {
        return stringValue.contains("{") && stringValue.contains("}");
    }

    public static void setTextAsHtml(@NonNull TextView textView, @NonNull String html) {
        textView.setAllCaps(false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        } else {
            textView.setText(Html.fromHtml(html));
        }
    }

    @NonNull
    private static String processValue(@NonNull String key, @NonNull Facts facts) {
        String value = "";
        if (facts.get(key) instanceof String) {
            value = facts.get(key);
            if (value != null && value.endsWith(OTHER_SUFFIX)) {
                Object otherValue = value.endsWith(OTHER_SUFFIX) ? facts.get(key + ConstantsUtils.SuffixUtils.OTHER) : "";
                value = otherValue != null ?
                        value.substring(0, value.lastIndexOf(",")) + ", " + otherValue.toString() + "]" :
                        value.substring(0, value.lastIndexOf(",")) + "]";

            }
        }

        return keyToValueConverter(value);
    }

    @NonNull
    private static String cleanValueResult(@NonNull String result) {
        List<String> nonEmptyItems = new ArrayList<>();

        for (String item : result.split(",")) {
            if (item.length() > 1) {
                nonEmptyItems.add(item);
            }
        }
        //Get the first item that usually  has a colon and remove it form list, if list has one item append separator
        String itemLabel = "";
        if (!nonEmptyItems.isEmpty() && nonEmptyItems.get(0).contains(":")) {
            String[] separatedLabel = nonEmptyItems.get(0).split(":");
            itemLabel = separatedLabel[0];
            if (separatedLabel.length > 1) {
                nonEmptyItems.set(0, nonEmptyItems.get(0).split(":")[1]);
            }//replace with extracted value
        }
        return itemLabel + (!TextUtils.isEmpty(itemLabel) ? ": " : "") + StringUtils.join(nonEmptyItems.toArray(), ",");
    }

    @NonNull
    public static org.smartregister.Context context() {
        return OpdLibrary.getInstance().context();
    }

    @Nullable
    public static OpdMetadata metadata() {
        return OpdLibrary.getInstance().getOpdConfiguration().getOpdMetadata();
    }

    @Nullable
    public static String getIntentValue(@Nullable Intent data, @NonNull String key) {
        if (data == null) {
            return null;
        }

        return data.hasExtra(key) ? data.getStringExtra(key) : null;
    }

    @NonNull
    public static String convertDate(@NonNull Date date, @NonNull String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
        return simpleDateFormat.format(date);
    }

    @Nullable
    public static Date convertStringToDate(@NonNull String pattern, @NonNull String dateString) {
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

        if (!TextUtils.isEmpty(dateString) && !TextUtils.isEmpty(pattern)) {
            try {
                date = dateFormat.parse(dateString);
            } catch (ParseException e) {
                Timber.e(e);
            }
        }
        return date;
    }

    @NonNull
    public static String generateNIds(int n) {
        StringBuilder strIds = new StringBuilder();
        for (int i = 0; i < n; i++) {
            if ((i + 1) == n) {
                strIds.append(JsonFormUtils.generateRandomUUIDString());
            } else {
                strIds.append(JsonFormUtils.generateRandomUUIDString()).append(",");
            }
        }
        return strIds.toString();
    }

    @NotNull
    public static String getClientAge(String dobString, String translatedYearInitial) {
        String age = dobString;
        if (dobString.contains(translatedYearInitial)) {
            String extractedYear = dobString.substring(0, dobString.indexOf(translatedYearInitial));
            int year = dobString.contains(translatedYearInitial) ? Integer.parseInt(extractedYear) : 0;
            if (year >= 5) {
                age = extractedYear;
            }
        }
        return age;
    }

    @NonNull
    public static Intent buildFormActivityIntent(JSONObject jsonForm, HashMap<String, String> parcelableData, Context context) {
        Intent intent = new Intent(context, OpdLibrary.getInstance().getOpdConfiguration().getOpdMetadata().getOpdFormActivity());
        intent.putExtra(OpdConstants.JSON_FORM_EXTRA.JSON, jsonForm.toString());
        Form form = new Form();
        form.setWizard(false);
        form.setName("");
        String encounterType = jsonForm.optString(OpdJsonFormUtils.ENCOUNTER_TYPE);
        if (encounterType.equals(OpdConstants.EventType.DIAGNOSIS_AND_TREAT)) {
            form.setName(OpdConstants.EventType.DIAGNOSIS_AND_TREAT);
            form.setWizard(true);
        }
        form.setHideSaveLabel(true);
        form.setPreviousLabel("");
        form.setNextLabel("");
        form.setHideNextButton(false);
        form.setHidePreviousButton(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);

        if (parcelableData != null) {
            for (String intentKey : parcelableData.keySet()) {
                intent.putExtra(intentKey, parcelableData.get(intentKey));
            }
        }
        return intent;
    }

    @Nullable
    public static JSONObject getJsonFormToJsonObject(String formName) {
        if (getFormUtils() == null) {
            return null;
        }

        return getFormUtils().getFormJson(formName);
    }


    @Nullable
    private static FormUtils getFormUtils() {
        if (formUtils == null) {
            try {
                formUtils = FormUtils.getInstance(OpdLibrary.getInstance().context().applicationContext());
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return formUtils;
    }


    @NonNull
    public static String keyToValueConverter(String keys) {
        if (!TextUtils.isEmpty(keys)) {
            String cleanKey;

            //If this contains html then don't capitalize it because it fails and the output is in lowercase
            if (keys.contains("<") && keys.contains(">")) {
                cleanKey = keys;
            } else {
                cleanKey = WordUtils.capitalizeFully(cleanValue(keys), ',');
            }

            return cleanKey.replaceAll("_", " ");
        } else {
            return "";
        }
    }

    public static String cleanValue(String raw) {
        if (raw.length() > 0 && raw.charAt(0) == '[') {
            return raw.substring(1, raw.length() - 1);
        } else {
            return raw;
        }
    }

    public static int buildRepeatingGroupTests(@NonNull JSONObject step1JsonObject) throws JSONException {
        ArrayList<String> keysArrayList = new ArrayList<>();
        JSONArray fields = step1JsonObject.optJSONArray(OpdJsonFormUtils.FIELDS);
        JSONObject jsonObject = JsonFormUtils.getFieldJSONObject(fields, OpdConstants.JSON_FORM_KEY.TESTS_REPEATING_GROUP);
        if (jsonObject != null) {
            JSONArray jsonArray = jsonObject.optJSONArray(JsonFormConstants.VALUE);
            int repeatedGroupNum = 0;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject valueField = jsonArray.optJSONObject(i);
                String fieldKey = valueField.optString(JsonFormConstants.KEY);
                keysArrayList.add(fieldKey);
            }

            for (int k = 0; k < fields.length(); k++) {
                JSONObject valueField = fields.optJSONObject(k);
                String fieldKey = valueField.optString(JsonFormConstants.KEY);
                String fieldValue = valueField.optString(JsonFormConstants.VALUE);

                if (fieldKey.contains("_")) {
                    fieldKey = fieldKey.substring(0, fieldKey.lastIndexOf("_"));
                    if (keysArrayList.contains(fieldKey) && StringUtils.isNotBlank(fieldValue)) {
                        valueField.put(JsonFormConstants.KEY, fieldKey);
                        valueField.put(OpdConstants.KEY.RELATION_KEY, fieldKey.substring(fieldKey.lastIndexOf("_")));
                        repeatedGroupNum++;
                    }
                }
            }
            //divide by 2 to count number of test&&result pair
            repeatedGroupNum = repeatedGroupNum / 2;
            return repeatedGroupNum;
        }
        return 0;
    }

    public static List<CompositeObs> getAllObsObject(@NonNull Event event) {
        List<Obs> obs = event.getObs();
        List<CompositeObs> compositeObsArrayList = new ArrayList<>();
        for (Obs observation : obs) {
            CompositeObs compositeObs = new CompositeObs();
            String key = observation.getFormSubmissionField();
            compositeObs.setFormSubmissionFieldKey(key);
            List<Object> values = observation.getValues();
            String value = "";
            if (values.size() > 0) {
                String obsValue = (String) values.get(0);
                if (StringUtils.isNotBlank(obsValue)) {
                    value = obsValue;
                }
            }

            List<Object> humanReadableValues = observation.getHumanReadableValues();
            if (humanReadableValues.size() > 0) {
                String humanReadableValue = (String) humanReadableValues.get(0);
                if (StringUtils.isNotBlank(humanReadableValue)) {
                    value = humanReadableValue;
                }
            }

            compositeObs.setValue(value);
            compositeObsArrayList.add(compositeObs);
        }

        return compositeObsArrayList;
    }

    public static void injectRelevanceFields(@NonNull JSONObject jsonForm, @NonNull String baseEntityId) {
        DetailsRepository detailsRepository = OpdLibrary.getInstance().context().detailsRepository();
        Map<String, String> map = detailsRepository.getAllDetailsForClient(baseEntityId);
        try {

            JSONObject genderJsonObject = new JSONObject();
            genderJsonObject.put(JsonFormConstants.KEY, OpdConstants.JSON_FORM_KEY.GENDER);
            genderJsonObject.put(JsonFormConstants.VALUE, map.get(OpdDbConstants.Column.Client.GENDER));
            genderJsonObject.put(JsonFormConstants.TYPE, JsonFormConstants.LABEL);
            genderJsonObject.put(JsonFormConstants.HIDDEN, "true");


            String strDob = map.get(OpdDbConstants.Column.Client.DOB);

            String age = "";
            if (StringUtils.isNotBlank(strDob)) {
                age = String.valueOf(Utils.getAgeFromDate(strDob));
            }

            JSONObject dobJsonObject = new JSONObject();
            dobJsonObject.put(JsonFormConstants.KEY, OpdConstants.JSON_FORM_KEY.AGE);
            dobJsonObject.put(JsonFormConstants.VALUE, age);
            dobJsonObject.put(JsonFormConstants.TYPE, JsonFormConstants.LABEL);
            dobJsonObject.put(JsonFormConstants.HIDDEN, "true");

            jsonForm.getJSONObject(JsonFormConstants.FIRST_STEP_NAME).getJSONArray(JsonFormConstants.FIELDS).put(genderJsonObject).put(dobJsonObject);

        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public static String opdLookUpQuery() {
        String lookUpQueryForOpdClient = "select id as _id, %s, %s, %s, %s, %s, %s, %s, national_id from ec_client where [condition] ";
        lookUpQueryForOpdClient = String.format(lookUpQueryForOpdClient, OpdConstants.KEY.RELATIONALID, OpdConstants.KEY.FIRST_NAME,
                OpdConstants.KEY.LAST_NAME, OpdConstants.KEY.GENDER, OpdConstants.KEY.DOB, OpdConstants.KEY.BASE_ENTITY_ID, OpdDbConstants.KEY.OPENSRP_ID);
        return lookUpQueryForOpdClient;
    }

}
