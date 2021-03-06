package org.smartregister.opd.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.opd.configuration.OpdRegisterQueryProviderContract;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-10-17
 */

public class OpdRegisterQueryProviderTest extends OpdRegisterQueryProviderContract {

    @NonNull
    @Override
    public String getObjectIdsQuery(@Nullable String filters, @Nullable String mainCondition) {
        return null;
    }

    @NonNull
    @Override
    public String[] countExecuteQueries(@Nullable String filters, @Nullable String mainCondition) {
        return new String[0];
    }

    @NonNull
    @Override
    public String mainSelectWhereIDsIn() {
        return "select * from ec_client where baseEntityId IN (%s)";
    }
}
