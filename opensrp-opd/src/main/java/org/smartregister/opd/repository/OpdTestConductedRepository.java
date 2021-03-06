package org.smartregister.opd.repository;

import android.content.ContentValues;
import androidx.annotation.NonNull;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.NotImplementedException;
import org.smartregister.opd.dao.OpdTestConductedDao;
import org.smartregister.opd.pojo.OpdTestConducted;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.repository.BaseRepository;

import java.util.List;

public class OpdTestConductedRepository extends BaseRepository implements OpdTestConductedDao {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE " + OpdDbConstants.Table.OPD_TEST_CONDUCTED + "("
            + OpdDbConstants.Column.OpdTestConducted.BASE_ENTITY_ID + " VARCHAR NOT NULL, "
            + OpdDbConstants.Column.OpdTestConducted.TEST_TYPE + " VARCHAR NOT NULL, "
            + OpdDbConstants.Column.OpdTestConducted.TEST_NAME + " VARCHAR NOT NULL, "
            + OpdDbConstants.Column.OpdTestConducted.RESULT + " VARCHAR NOT NULL, "
            + OpdDbConstants.Column.OpdTestConducted.DETAILS + " VARCHAR NULL, "
            + OpdDbConstants.Column.OpdTestConducted.VISIT_ID + " VARCHAR NOT NULL, "
            + OpdDbConstants.Column.OpdTestConducted.CREATED_AT + " VARCHAR NULL )";

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + OpdDbConstants.Table.OPD_TEST_CONDUCTED
            + "_" + OpdDbConstants.Column.OpdTestConducted.BASE_ENTITY_ID + "_index ON " + OpdDbConstants.Table.OPD_TEST_CONDUCTED +
            "(" + OpdDbConstants.Column.OpdTestConducted.BASE_ENTITY_ID + " COLLATE NOCASE);";

    private static final String INDEX_VISIT_ID = "CREATE INDEX " + OpdDbConstants.Table.OPD_TEST_CONDUCTED
            + "_" + OpdDbConstants.Column.OpdTestConducted.VISIT_ID + "_index ON " + OpdDbConstants.Table.OPD_TEST_CONDUCTED +
            "(" + OpdDbConstants.Column.OpdTestConducted.VISIT_ID + " COLLATE NOCASE);";

    public static void createTable(@NonNull SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
        database.execSQL(INDEX_BASE_ENTITY_ID);
        database.execSQL(INDEX_VISIT_ID);
    }


    @Override
    public boolean save(@NonNull OpdTestConducted opdTestConducted) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.BASE_ENTITY_ID, opdTestConducted.getBaseEntityId());
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.TEST_TYPE, opdTestConducted.getTestType());
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.TEST_NAME, opdTestConducted.getTestName());
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.RESULT, opdTestConducted.getResult());
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.DETAILS, opdTestConducted.getDetails());
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.VISIT_ID, opdTestConducted.getVisitId());
        contentValues.put(OpdDbConstants.Column.OpdTestConducted.CREATED_AT, opdTestConducted.getCreatedAt());
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();
        long rows = sqLiteDatabase.insert(OpdDbConstants.Table.OPD_TEST_CONDUCTED, null, contentValues);
        return rows != -1;
    }

    @Override
    public boolean saveOrUpdate(OpdTestConducted opdTestConducted) {
        throw new NotImplementedException("Not Implemented");
    }

    @Override
    public OpdTestConducted findOne(OpdTestConducted opdTestConducted) {
        throw new NotImplementedException("Not Implemented");
    }

    @Override
    public boolean delete(OpdTestConducted opdTestConducted) {
        throw new NotImplementedException("Not Implemented");
    }

    @Override
    public List<OpdTestConducted> findAll() {
        throw new NotImplementedException("Not Implemented");
    }
}
