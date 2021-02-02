package org.smartregister.opd.repository;

import android.content.ContentValues;

import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.smartregister.opd.BaseUnitTest;
import org.smartregister.opd.pojo.OpdDiagnosisDetail;
import org.smartregister.opd.utils.OpdDbConstants;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OpdDiagnosisDetailRepositoryTest extends BaseUnitTest {

    private OpdDiagnosisDetailRepository opdDiagnosisDetailRepository;

    @Mock
    private SQLiteDatabase sqLiteDatabase;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        opdDiagnosisDetailRepository = spy(new OpdDiagnosisDetailRepository());
    }

    @Test
    public void testCreateTableShouldInvokeExecSqlThreeTimes() {
        OpdDiagnosisDetailRepository.createTable(sqLiteDatabase);
        verify(sqLiteDatabase).execSQL(eq(OpdDiagnosisDetailRepository.INDEX_BASE_ENTITY_ID));
        verify(sqLiteDatabase).execSQL(eq(OpdDiagnosisDetailRepository.INDEX_VISIT_ID));
        verify(sqLiteDatabase).execSQL(eq(OpdDiagnosisDetailRepository.CREATE_TABLE_SQL));
    }

    @Test
    public void testSaveShouldInvokeDbInsert() {
        doReturn(sqLiteDatabase).when(opdDiagnosisDetailRepository).getWritableDatabase();

        OpdDiagnosisDetail opdDiagnosisDetail = new OpdDiagnosisDetail();

        opdDiagnosisDetailRepository.save(opdDiagnosisDetail);

        verify(sqLiteDatabase).insert(
                eq(OpdDbConstants.Table.OPD_DIAGNOSIS_DETAIL),
                isNull(),
                any(ContentValues.class));
    }
}