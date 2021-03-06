package org.smartregister.opd.utils;

import android.database.Cursor;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.Context;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.event.Listener;
import org.smartregister.opd.pojo.OpdMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;


public class OpdLookUpUtils {

    public static void lookUp(@NonNull final Context context, @NonNull final Map<String, String> entityLookUp, @NonNull final Listener<List<CommonPersonObject>> listener) {
        org.smartregister.util.Utils
                .startAsyncTask(new AsyncTask<Void, Void, List<CommonPersonObject>>() {
                    @Override
                    protected List<CommonPersonObject> doInBackground(Void... params) {
                        publishProgress();
                        return clientLookUp(context, entityLookUp);
                    }

                    @Override
                    protected void onPostExecute(List<CommonPersonObject> result) {
                        listener.onEvent(result);
                    }

                    @Override
                    protected void onProgressUpdate(Void... values) {
                        //show progress bar
                    }
                }, null);
    }

    private static List<CommonPersonObject> clientLookUp(@Nullable Context context, @NonNull Map<String, String> entityLookUp) {
        List<CommonPersonObject> results = new ArrayList<>();
        if (context == null) {
            return results;
        }

        if (entityLookUp.isEmpty()) {
            return results;
        }

        OpdMetadata opdMetadata = OpdUtils.metadata();
        if (opdMetadata != null) {
            String tableName = opdMetadata.getTableName();

            CommonRepository commonRepository = context.commonrepository(tableName);
            String query = lookUpQuery(entityLookUp);
            if (query == null) {
                return results;
            }
            try (Cursor cursor = commonRepository.rawCustomQueryForAdapter(query)) {

                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        CommonPersonObject commonPersonObject = commonRepository.readAllcommonforCursorAdapter(cursor);
                        results.add(commonPersonObject);
                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }

        return results;
    }

    protected static String lookUpQuery(@NonNull Map<String, String> entityMap) {
        String mainCondition = getMainConditionString(entityMap);
        if (!TextUtils.isEmpty(mainCondition)) {
            return OpdUtils.metadata().getLookUpQueryForOpdClient().replace("[condition]", mainCondition) + ";";
        }
        return null;
    }

    protected static String getMainConditionString(@NonNull Map<String, String> entityMap) {
        String mainConditionString = "";
        for (Map.Entry<String, String> entry : entityMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (StringUtils.isBlank(mainConditionString)) {
                mainConditionString += " " + key + " Like '%" + value + "%'";
            } else {
                mainConditionString += " AND " + key + " Like '%" + value + "%'";

            }
        }

        return mainConditionString;
    }
}