package org.smartregister.opd.model;

import androidx.annotation.NonNull;

import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.contract.OpdProfileOverviewFragmentContract;
import org.smartregister.opd.pojo.OpdDetails;
import org.smartregister.opd.pojo.OpdVisit;
import org.smartregister.opd.utils.AppExecutors;

import java.util.Map;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-10-17
 */

public class OpdProfileOverviewFragmentModel implements OpdProfileOverviewFragmentContract.Model {

    private AppExecutors appExecutors;
    private OpdDetails opdDetails = null;

    public OpdProfileOverviewFragmentModel() {
        this.appExecutors = new AppExecutors();
    }

    @Override
    public void fetchLastCheckAndVisit(final @NonNull String baseEntityId, @NonNull final OnFetchedCallback onFetchedCallback) {
        appExecutors.diskIO().execute(new Runnable() {

            @Override
            public void run() {
                final OpdVisit visit = OpdLibrary.getInstance().getVisitRepository().getLatestVisit(baseEntityId);
                final Map<String, String> checkInMap = visit != null ? OpdLibrary.getInstance().getCheckInRepository().getCheckInByVisit(visit.getId()) : null;

                opdDetails = null;

                if (visit != null) {
                    opdDetails = new OpdDetails(baseEntityId, visit.getId());
                    opdDetails = OpdLibrary.getInstance().getOpdDetailsRepository().findOne(opdDetails);
                }

                appExecutors.mainThread().execute(new Runnable() {

                    @Override
                    public void run() {
                        onFetchedCallback.onFetched(checkInMap, visit, opdDetails);
                    }
                });
            }
        });
    }
}
