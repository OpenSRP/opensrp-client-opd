package org.smartregister.opd.presenter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jeasy.rules.api.Facts;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.R;
import org.smartregister.opd.contract.OpdProfileOverviewFragmentContract;
import org.smartregister.opd.domain.YamlConfig;
import org.smartregister.opd.domain.YamlConfigItem;
import org.smartregister.opd.domain.YamlConfigWrapper;
import org.smartregister.opd.model.OpdProfileOverviewFragmentModel;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdVisit;
import org.smartregister.opd.utils.FilePath;
import org.smartregister.opd.utils.OpdConstants;
import org.smartregister.opd.utils.OpdDbConstants;
import org.smartregister.opd.utils.OpdFactsUtil;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.util.DateUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-10-17
 */

public class OpdProfileOverviewFragmentPresenter implements OpdProfileOverviewFragmentContract.Presenter {

    private OpdProfileOverviewFragmentModel model;
    private CommonPersonObjectClient client;
    private WeakReference<OpdProfileOverviewFragmentContract.View> view;

    public OpdProfileOverviewFragmentPresenter(@NonNull OpdProfileOverviewFragmentContract.View view) {
        this.view = new WeakReference<>(view);
        model = new OpdProfileOverviewFragmentModel();
    }

    @Override
    public void loadOverviewFacts(@NonNull String baseEntityId, @NonNull final OnFinishedCallback onFinishedCallback) {
        model.fetchLastCheckAndVisit(baseEntityId, new OpdProfileOverviewFragmentContract.Model.OnFetchedCallback() {
            @Override
            public void onFetched(@Nullable Map<String, String> opdCheckIn, @Nullable OpdVisit opdVisit, @Nullable OpdDetails opdDetails) {
                loadOverviewDataAndDisplay(opdCheckIn, opdVisit, opdDetails, onFinishedCallback);
            }
        });
    }

    @Override
    public void loadOverviewDataAndDisplay(@Nullable Map<String, String> opdCheckIn, @Nullable OpdVisit opdVisit, @Nullable OpdDetails opdDetails, @NonNull final OnFinishedCallback onFinishedCallback) {
        List<YamlConfigWrapper> yamlConfigListGlobal = new ArrayList<>(); //This makes sure no data duplication happens
        Facts facts = new Facts();
        setDataFromCheckIn(opdCheckIn, opdVisit, opdDetails, facts);

        try {
            generateYamlConfigList(facts, yamlConfigListGlobal);
        } catch (IOException ioException) {
            Timber.e(ioException);
        }

        onFinishedCallback.onFinished(facts, yamlConfigListGlobal);
    }

    private void generateYamlConfigList(@NonNull Facts facts, @NonNull List<YamlConfigWrapper> yamlConfigListGlobal) throws IOException {
        Iterable<Object> ruleObjects = loadFile(FilePath.FILE.OPD_PROFILE_OVERVIEW);

        for (Object ruleObject : ruleObjects) {
            List<YamlConfigWrapper> yamlConfigList = new ArrayList<>();
            int valueCount = 0;

            YamlConfig yamlConfig = (YamlConfig) ruleObject;
            if (yamlConfig.getGroup() != null) {
                yamlConfigList.add(new YamlConfigWrapper(yamlConfig.getGroup(), null, null));
            }

            if (yamlConfig.getSubGroup() != null) {
                yamlConfigList.add(new YamlConfigWrapper(null, yamlConfig.getSubGroup(), null));
            }

            List<YamlConfigItem> configItems = yamlConfig.getFields();

            if (configItems != null) {

                for (YamlConfigItem configItem : configItems) {
                    String relevance = configItem.getRelevance();
                    if (relevance != null && OpdLibrary.getInstance().getOpdRulesEngineHelper()
                            .getRelevance(facts, relevance)) {
                        yamlConfigList.add(new YamlConfigWrapper(null, null, configItem));
                        valueCount += 1;
                    }
                }
            }

            if (valueCount > 0) {
                yamlConfigListGlobal.addAll(yamlConfigList);
            }
        }
    }

    @Override
    public void setDataFromCheckIn(@Nullable Map<String, String> checkIn, @Nullable OpdVisit visit, @Nullable OpdDetails opdDetails, @NonNull Facts facts) {
        String unknownString = getString(R.string.unknown);
        if (checkIn != null) {
            // Client is currently checked-in, show the current check-in details
            if (OpdLibrary.getInstance().isClientCurrentlyCheckedIn(visit, opdDetails)) {
                OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.ProfileOverview.VISIT_TYPE, checkIn.get("visit_type"));
                OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.ProfileOverview.APPOINTMENT_SCHEDULED_PREVIOUSLY, checkIn.get("appointment_scheduled"));
                OpdFactsUtil.putNonNullFact(facts, OpdConstants.FactKey.ProfileOverview.DATE_OF_APPOINTMENT, checkIn.get("appointment_made_date"));
            }
        }

        boolean shouldCheckIn = OpdLibrary.getInstance().canPatientCheckInInsteadOfDiagnoseAndTreat(visit, opdDetails);
        facts.put(OpdDbConstants.Column.OpdDetails.PENDING_DIAGNOSE_AND_TREAT, !shouldCheckIn);

        if (visit != null && visit.getVisitDate() != null && checkIn != null && checkIn.get("appointment_made_date") != null) {
            facts.put(OpdConstants.FactKey.VISIT_TO_APPOINTMENT_DATE, getVisitToAppointmentDateDuration(visit.getVisitDate(), checkIn.get("appointment_made_date")));
        }
    }

    private Iterable<Object> loadFile(@NonNull String filename) throws IOException {
        return OpdLibrary.getInstance().readYaml(filename);
    }

    @NonNull
    private String getVisitToAppointmentDateDuration(@NonNull Date visitDate, @NonNull String appointmentDueDateString) {
        Date appointmentDueDate = OpdUtils.convertStringToDate(OpdConstants.DateFormat.YYYY_MM_DD, appointmentDueDateString);
        if (appointmentDueDate != null) {
            return DateUtil.getDuration(appointmentDueDate.getTime() - visitDate.getTime());
        }

        return "";
    }

    public void setClient(@NonNull CommonPersonObjectClient client) {
        this.client = client;
    }

    @Nullable
    @Override
    public OpdProfileOverviewFragmentContract.View getProfileView() {
        OpdProfileOverviewFragmentContract.View view = this.view.get();
        if (view != null) {
            return view;
        }

        return null;
    }

    @Nullable
    @Override
    public String getString(int stringId) {
        if (getProfileView() != null) {
            return getProfileView().getString(stringId);
        }

        return null;
    }


}
