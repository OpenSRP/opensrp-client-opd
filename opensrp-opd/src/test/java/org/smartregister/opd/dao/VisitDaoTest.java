package org.smartregister.opd.dao;

import net.sqlcipher.MatrixCursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.opd.repository.OpdVisitRepository;
import org.smartregister.opd.repository.VisitDetailsRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;

public class VisitDaoTest extends VisitDao{

    @Mock
    private Repository repository;

    @Mock
    private SQLiteDatabase database;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setRepository(repository);
    }

    @Test
    public void testGetSavedKeysForVisit() {
        doReturn(database).when(repository).getReadableDatabase();
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                "visit_details_id", "visit_id", "visit_key", "parent_code",
                "details", "human_readable_details", "updated_at", "created_at"
        });
        matrixCursor.addRow(new Object[]{
                "85e5dd54-ba27-46b1-b5c2-2bab06fd77e2", "85e5dd54-ba27-46b1-b5c2-uwj20sk5m6hue", "treatment_type", "",
                "", "Medicine, Suturing, Wound dressing, Foreign body removal", "", ""
        });

        doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        Map<String, String> values = VisitDao.getSavedKeysForVisit("85e5dd54-ba27-46b1-b5c2-uwj20sk5m6hue");

        Assert.assertEquals(values.size(), 1);
        Assert.assertEquals(values.get("treatment_type"), "Medicine, Suturing, Wound dressing, Foreign body removal");
    }

    @Test
    public void testGetDateStringForId() {
        doReturn(database).when(repository).getReadableDatabase();
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                "visit_details_id", "visit_id", "visit_key", "parent_code",
                "details", "human_readable_details", "updated_at", "created_at"
        });


        matrixCursor.addRow(new Object[]{
                "85e5dd54-ba27-46b1-b5c2-2bab06fd77e2", "7a6e450f-0b25-4e93-bf89-bd5eb58185a2", "treatment_type", "",
                "", "Medicine, Suturing, Wound dressing, Foreign body removal", "", "1626451003565"
        });

        doReturn(matrixCursor).when(database).rawQuery(Mockito.any(), Mockito.any());

        String value = VisitDao.getDateStringForId("7a6e450f-0b25-4e93-bf89-bd5eb58185a2");

        Assert.assertEquals(value, "16 Jul 2021");
    }

}
