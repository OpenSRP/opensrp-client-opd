package org.smartregister.opd.fragment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.opd.BaseTest;
import org.smartregister.opd.dao.VisitDao;
import org.smartregister.opd.domain.ProfileAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VisitDao.class)
public class NewOpdProfileOverviewFragmentTest extends BaseTest {

    private NewOpdProfileOverviewFragment fragment;

    @Before
    public void setUp() {
        fragment = Mockito.spy(NewOpdProfileOverviewFragment.newInstance(null));
    }

    @Test
    public void testLoadGlobals() throws Exception {
        PowerMockito.mockStatic(VisitDao.class);
        HashSet<String> globalKeys = new HashSet<>();
        globalKeys.add("opd_danger_signs_value");
        globalKeys.add("respiratory_rate");
        globalKeys.add("disease_code_primary");
        globalKeys.add("pregnancy_status");
        globalKeys.add("oximetry");
        globalKeys.add("medicine");
        globalKeys.add("disease_code_final_diagn");
        globalKeys.add("bp_systolic");
        globalKeys.add("opd_health_concerns_value");
        globalKeys.add("opd_danger_signs");
        globalKeys.add("bp_diastolic");
        globalKeys.add("diagnostic_test_lab_results");
        globalKeys.add("pulse_rate");
        globalKeys.add("current_weight");
        globalKeys.add("diagnostic_test_ordered");
        globalKeys.add("opd_health_concerns");
        globalKeys.add("convulsions_history");
        globalKeys.add("body_temp");
        globalKeys.add("height");
        globalKeys.add("medical_conditions");
        ReflectionHelpers.setField(fragment, "globalKeys", globalKeys);

        HashMap<String, String> savedValues = new HashMap<>();
        savedValues.put("tests_repeating_group_count", "3");
        savedValues.put("medicine_object", "\"[{\"key\":\"AA007840\",\"text\":\"Atenolol 50mg\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_entity_parent\":\"\",\"property\":{\"pack_size\":null,\"product_code\":\"AA007840\",\"dispensing_unit\":\"Tablet\",\"meta\":{\"duration\":\"78\",\"dosage\":\"12\",\"frequency\":\"3456\",\"info\":\"Dose: 12, Duration: 78, Frequency: 3456\"}}},{\"key\":\"FF006300\",\"text\":\"Bandage, WOW 10cm x 4m long, when stretched\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_entity_parent\":\"\",\"property\":{\"pack_size\":null,\"product_code\":\"FF006300\",\"dispensing_unit\":\"each\",\"meta\":{\"duration\":\"33\",\"dosage\":\"11\",\"frequency\":\"2244\",\"info\":\"Dose: 11, Duration: 33, Frequency: 2244\"}}}]");
        savedValues.put("test_ordered_avail", "yes");
        savedValues.put("diagnostic_test_ba1ed23029a44fd980784093a5c6f746", "ultra_sound");
        savedValues.put("diagnostic_test_24f8d3b0a73a49e9894c83d6d545b39f", "pregnancy_test");
        savedValues.put("repeatingGroupMap", "{\"24f8d3b0a73a49e9894c83d6d545b39f\":{\"diagnostic_test_result\":\"Negative\",\"diagnostic_test\":\"pregnancy_test\"},\"ba1ed23029a44fd980784093a5c6f746\":{\"diagnostic_test\":\"ultra_sound\",\"diagnostic_test_result_specify\":\"Ultra\"}}");
        savedValues.put("treatment_type", "Medicine, Suturing, Wound dressing, Foreign body removal");
        savedValues.put("medicine", "[{\"key\":\"AA007840\",\"text\":\"Atenolol 50mg\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_entity_parent\":\"\",\"property\":{\"pack_size\":null,\"product_code\":\"AA007840\",\"dispensing_unit\":\"Tablet\",\"meta\":{\"duration\":\"78\",\"dosage\":\"12\",\"frequency\":\"3456\",\"info\":\"Dose: 12, Duration: 78, Frequency: 3456\"}}},{\"key\":\"FF006300\",\"text\":\"Bandage, WOW 10cm x 4m long, when stretched\",\"openmrs_entity\":\"\",\"openmrs_entity_id\":\"\",\"openmrs_entity_parent\":\"\",\"property\":{\"pack_size\":null,\"product_code\":\"FF006300\",\"dispensing_unit\":\"each\",\"meta\":{\"duration\":\"33\",\"dosage\":\"11\",\"frequency\":\"2244\",\"info\":\"Dose: 11, Duration: 33, Frequency: 2244\"}}}]");
        savedValues.put("diagnostic_test_result_specify_ba1ed23029a44fd980784093a5c6f746", "Ultra");
        savedValues.put("diagnostic_test_result_24f8d3b0a73a49e9894c83d6d545b39f", "Negative");
        PowerMockito.doReturn(savedValues).when(VisitDao.class, "getSavedKeysForVisit", Mockito.anyString());

        HashMap<String, List<ProfileAction.ProfileActionVisit>> mapVisits = new HashMap<>();
        ArrayList<ProfileAction.ProfileActionVisit> visits = new ArrayList<>();
        ProfileAction.ProfileActionVisit visit = new ProfileAction.ProfileActionVisit();
        visit.setVisitID("abc123");
        visits.add(visit);
        mapVisits.put("OPD_treatment", visits);

        fragment.loadGlobals(mapVisits);
        HashMap<String, String > formGlobalValues = ReflectionHelpers.getField(fragment, "formGlobalValues");
        Assert.assertEquals(formGlobalValues.size(), globalKeys.size());

    }

}
