package org.smartregister.opd.repository;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdDbConstants.Column.OpdDetails;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-30
 */

public class OpdDetailsRepository extends BaseRepository {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE " + OpdDbConstants.Table.OPD_DETAILS + "("
            + OpdDetails.ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
            + OpdDetails.BASE_ENTITY_ID + " VARCHAR NOT NULL, "
            + OpdDetails.PENDING_DIAGNOSE_AND_TREAT + " BOOLEAN NOT NULL, "
            + OpdDetails.CURRENT_VISIT_START_DATE + " DATETIME, "
            + OpdDetails.CURRENT_VISIT_END_DATE + " DATETIME, "
            + OpdDetails.CURRENT_VISIT_ID + " VARCHAR NOT NULL, "
            + OpdDetails.CREATED_AT + " DATETIME NOT NULL DEFAULT (DATETIME('now')), UNIQUE(" + OpdDetails.BASE_ENTITY_ID + ") ON CONFLICT REPLACE)";


    private String[] columns = new String[]{
            OpdDetails.BASE_ENTITY_ID
            , OpdDetails.PENDING_DIAGNOSE_AND_TREAT
            , OpdDetails.CURRENT_VISIT_START_DATE
            , OpdDetails.CURRENT_VISIT_END_DATE
            , OpdDetails.CURRENT_VISIT_ID
    };

    private SimpleDateFormat dateFormat = new SimpleDateFormat(OpdDbConstants.DATE_FORMAT, Locale.US);

    public OpdDetailsRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(@NonNull SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
    }

    @NonNull
    public ContentValues createValuesFor(@NonNull org.smartregister.opd.pojos.OpdDetails opdDetails) {
        ContentValues contentValues = new ContentValues();

        if (opdDetails.getId() != 0) {
            contentValues.put(OpdDetails.ID, opdDetails.getId());
        }

        contentValues.put(OpdDetails.BASE_ENTITY_ID, opdDetails.getBaseEntityId());
        contentValues.put(OpdDetails.PENDING_DIAGNOSE_AND_TREAT, opdDetails.isPendingDiagnoseAndTreat());
        contentValues.put(OpdDetails.CURRENT_VISIT_START_DATE, dateFormat.format(opdDetails.getCurrentVisitStartDate()));

        if (opdDetails.getCurrentVisitEndDate() != null) {
            contentValues.put(OpdDetails.CURRENT_VISIT_START_DATE, dateFormat.format(opdDetails.getCurrentVisitEndDate()));
        }

        contentValues.put(OpdDetails.CURRENT_VISIT_ID, opdDetails.getCurrentVisitId());

        return contentValues;
    }

    public boolean addOrUpdateOpdDetails(@NonNull org.smartregister.opd.pojos.OpdDetails opdDetails) {
        ContentValues contentValues = createValuesFor(opdDetails);

        SQLiteDatabase database = getWritableDatabase();
        long recordId = database.insertWithOnConflict(OpdDbConstants.Table.OPD_DETAILS, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);

        return recordId != -1;
    }
}