package org.smartregister.opd.repository;

import android.content.ContentValues;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.opd.dao.OpdDiagnosisAndTreatmentFormDao;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;

public class OpdDiagnosisAndTreatmentFormRepository extends BaseRepository implements OpdDiagnosisAndTreatmentFormDao {

    protected static final String CREATE_TABLE_SQL = "CREATE TABLE " + OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM + "("
            + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " VARCHAR NOT NULL, "
            + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.FORM + " TEXT NOT NULL, "
            + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.CREATED_AT + " INTEGER NOT NULL ," +
            "UNIQUE(" + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + ") ON CONFLICT REPLACE)";

    protected static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM
            + "_" + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + "_index ON " + OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM +
            "(" + OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private String[] columns = new String[]{
            OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.ID,
            OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID,
            OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.FORM,
            OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.CREATED_AT};

    public static void createTable(@NonNull SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
        database.execSQL(INDEX_BASE_ENTITY_ID);
    }

    @Override
    public boolean saveOrUpdate(@NonNull OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID, opdDiagnosisAndTreatmentForm.getBaseEntityId());
        contentValues.put(OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.FORM, opdDiagnosisAndTreatmentForm.getForm());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        contentValues.put(OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.CREATED_AT, opdDiagnosisAndTreatmentForm.getCreatedAt());
        long rows = sqLiteDatabase.insertWithOnConflict(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
        return rows != -1;
    }

    @Override
    public boolean save(OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm) {
        throw new NotImplementedException("not implemented");
    }

    @Nullable
    @Override
    public OpdDiagnosisAndTreatmentForm findOne(@NonNull OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM
                , columns
                , OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " = ? "
                , new String[]{opdDiagnosisAndTreatmentForm.getBaseEntityId()}
                , null
                , null
                , null);

        if (cursor.getCount() == 0) {
            return null;
        }

        OpdDiagnosisAndTreatmentForm diagnosisAndTreatmentForm = null;
        if (cursor.moveToNext()) {
            diagnosisAndTreatmentForm = new OpdDiagnosisAndTreatmentForm(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3));
            cursor.close();
        }

        return diagnosisAndTreatmentForm;
    }

    @Override
    public boolean delete(@NonNull OpdDiagnosisAndTreatmentForm opdDiagnosisAndTreatmentForm) {
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        int rows = sqLiteDatabase.delete(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM
                , OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " = ? "
                , new String[]{opdDiagnosisAndTreatmentForm.getBaseEntityId()});

        return rows > 0;
    }

    @Override
    public List<OpdDiagnosisAndTreatmentForm> findAll() {
        throw new NotImplementedException("Not Implemented");
    }
}
