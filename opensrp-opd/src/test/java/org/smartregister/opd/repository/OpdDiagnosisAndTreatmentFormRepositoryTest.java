package org.smartregister.opd.repository;

import android.content.ContentValues;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.opd.BaseUnitTest;
import org.smartregister.opd.pojo.OpdDiagnosisAndTreatmentForm;
import org.smartregister.opd.utils.OpdDbConstants;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OpdDiagnosisAndTreatmentFormRepositoryTest extends BaseUnitTest {

    @Mock
    private SQLiteDatabase sqLiteDatabase;

    @Mock
    private Cursor cursor;

    private OpdDiagnosisAndTreatmentFormRepository treatmentFormRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        treatmentFormRepository = spy(new OpdDiagnosisAndTreatmentFormRepository());
    }

    @Test
    public void testCreateTableShouldInvokeExecSqlTwice() {
        OpdDiagnosisAndTreatmentFormRepository.createTable(sqLiteDatabase);
        verify(sqLiteDatabase).execSQL(eq(OpdDiagnosisAndTreatmentFormRepository.CREATE_TABLE_SQL));
        verify(sqLiteDatabase).execSQL(eq(OpdDiagnosisAndTreatmentFormRepository.INDEX_BASE_ENTITY_ID));
    }

    @Test
    public void testSaveOrUpdateShouldInvokeUpdate() {
        doReturn(sqLiteDatabase).when(treatmentFormRepository).getWritableDatabase();
        OpdDiagnosisAndTreatmentForm form = new OpdDiagnosisAndTreatmentForm();
        form.setBaseEntityId(UUID.randomUUID().toString());
        form.setForm("{}");

        treatmentFormRepository.saveOrUpdate(form);
        verify(sqLiteDatabase).insertWithOnConflict(eq(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM), isNull(), any(ContentValues.class), eq(SQLiteDatabase.CONFLICT_REPLACE));
    }

    @Test
    public void testDeleteShouldInvokeDelete() {
        doReturn(sqLiteDatabase).when(treatmentFormRepository).getReadableDatabase();
        OpdDiagnosisAndTreatmentForm form = new OpdDiagnosisAndTreatmentForm();
        form.setBaseEntityId(UUID.randomUUID().toString());
        treatmentFormRepository.delete(form);
        verify(sqLiteDatabase).delete(eq(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM), eq(OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " = ? "), eq(new String[]{form.getBaseEntityId()}));
    }

    @Test
    public void testFindOneShouldReturnFormIfFound() {
        OpdDiagnosisAndTreatmentForm form = new OpdDiagnosisAndTreatmentForm();
        form.setBaseEntityId(UUID.randomUUID().toString());

        doReturn(1).when(cursor).getCount();
        doReturn(true).when(cursor).moveToNext();

        doReturn(sqLiteDatabase).when(treatmentFormRepository).getReadableDatabase();

        doReturn(cursor).when(sqLiteDatabase).query(
                eq(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM),
                any(String[].class),
                eq(OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " = ? "),
                eq(new String[]{form.getBaseEntityId()}), isNull(), isNull(), isNull());

        OpdDiagnosisAndTreatmentForm resultForm = treatmentFormRepository.findOne(form);

        assertNotNull(resultForm);

        verify(sqLiteDatabase).query(
                eq(OpdDbConstants.Table.OPD_DIAGNOSIS_AND_TREATMENT_FORM),
                eq(ReflectionHelpers.getField(treatmentFormRepository, "columns")),
                eq(OpdDbConstants.Column.OpdDiagnosisAndTreatmentForm.BASE_ENTITY_ID + " = ? "),
                eq(new String[]{form.getBaseEntityId()}), isNull(), isNull(), isNull());
    }
}