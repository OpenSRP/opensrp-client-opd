package org.smartregister.opd.presenter;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.BaseTest;
import org.smartregister.opd.fragment.NewOpdProfileOverviewFragment;
import org.smartregister.opd.fragment.NewOpdProfileVisitsFragment;
import org.smartregister.util.JsonFormUtils;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
public class NewOpdProfileVisitFragmentPresenterTest extends BaseTest {
    
    private NewOpdProfileVisitsFragmentPresenter presenter;
    private NewOpdProfileVisitsFragment view;

    @Before
    public void setUp() {
        view  = NewOpdProfileVisitsFragment.newInstance(null);
        presenter = (NewOpdProfileVisitsFragmentPresenter) Mockito.spy(new NewOpdProfileVisitsFragmentPresenter().with(view));
    }
    
    @Test
    public void testAttachAgeAndGender() throws Exception{
        String jsonString = "{\n" +
                "  \"count\": \"1\",\n" +
                "  \"encounter_type\": \"OPD_Diagnosis\",\n" +
                "  \"entity_id\": \"\",\n" +
                "  \"step1\": {\n" +
                "    \"title\": \"Diagnosis\",\n" +
                "    \"fields\": [\n" +
                "      {\n" +
                "        \"key\": \"danger_signs_opd_note\",\n" +
                "        \"openmrs_entity_parent\": \"\",\n" +
                "        \"openmrs_entity\": \"\",\n" +
                "        \"openmrs_entity_id\": \"\",\n" +
                "        \"type\": \"toaster_notes\",\n" +
                "        \"text\": \"Danger Signs {danger_signs}\",\n" +
                "        \"toaster_type\": \"problem\",\n" +
                "        \"relevance\": {\n" +
                "          \"rules-engine\": {\n" +
                "            \"ex-rules\": {\n" +
                "              \"rules-file\": \"opd/opd_diagnosis_relevance_rules.yml\"\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"calculation\": {\n" +
                "          \"rules-engine\": {\n" +
                "            \"ex-rules\": {\n" +
                "              \"rules-file\": \"opd/opd_diagnosis_calculation.yml\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      {\n" +
                "        \"key\": \"gender\",\n" +
                "        \"openmrs_entity_parent\": \"\",\n" +
                "        \"openmrs_entity\": \"\",\n" +
                "        \"openmrs_entity_id\": \"\",\n" +
                "        \"type\": \"hidden\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"key\": \"age\",\n" +
                "        \"openmrs_entity_parent\": \"\",\n" +
                "        \"openmrs_entity\": \"\",\n" +
                "        \"openmrs_entity_id\": \"\",\n" +
                "        \"type\": \"hidden\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"baseEntityId\": \"43f2675c-a1f3-4d24-9788-a83c68ed48e5\"\n" +
                "}";
        JSONObject json= new JSONObject(jsonString);
        HashMap<String, String> map = new HashMap<>();
        map.put("gender",  "Female");
        map.put("dob",  "1960-01-01T17:00:00.000+05:00");
        CommonPersonObjectClient commonPersonObject = new CommonPersonObjectClient("", map, "");
        commonPersonObject.setColumnmaps(map);
        ReflectionHelpers.setField(view, "commonPersonObjectClient", commonPersonObject);
        presenter.attachAgeAndGender(json);
        String gender = JsonFormUtils.getFieldValue(json.toString(), "gender");
        String age = JsonFormUtils.getFieldValue(json.toString(), "age");
        Assert.assertEquals(gender, "Female");
        Assert.assertEquals(age, "61");
    }
    
    
}
