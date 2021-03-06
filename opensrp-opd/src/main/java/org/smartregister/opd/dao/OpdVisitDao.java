package org.smartregister.opd.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.opd.pojo.OpdVisit;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-10-01
 */

public interface OpdVisitDao {


    @Nullable
    OpdVisit getLatestVisit(@NonNull String clientBaseEntityId);
}
