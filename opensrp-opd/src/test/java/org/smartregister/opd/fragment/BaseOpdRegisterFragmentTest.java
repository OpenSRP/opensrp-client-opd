package org.smartregister.opd.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.opd.BaseUnitTest;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.R;
import org.smartregister.opd.activity.BaseOpdFormActivity;
import org.smartregister.opd.activity.BaseOpdProfileActivity;
import org.smartregister.opd.configuration.BaseOpdRegisterProviderMetadata;
import org.smartregister.opd.configuration.OpdConfiguration;
import org.smartregister.opd.configuration.OpdRegisterQueryProviderContract;
import org.smartregister.opd.pojo.OpdMetadata;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class BaseOpdRegisterFragmentTest extends BaseUnitTest {

    private FragmentScenario<TestBaseOpdRegisterFragment> fragmentScenario;

    @Mock
    private OpdLibrary opdLibrary;

    @Mock
    private OpdConfiguration opdConfiguration;

    @Mock
    private CoreLibrary coreLibrary;

    @Mock
    private Context opensrpContext;

    @Mock
    private SyncStatusBroadcastReceiver syncStatusBroadcastReceiver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        OpdMetadata opdMetadata = new OpdMetadata("form-name"
                , "table-name"
                , "register-event-type"
                , "update-event-type"
                , "config"
                , BaseOpdFormActivity.class
                , BaseOpdProfileActivity.class
                , false);

        doReturn(BaseOpdRegisterProviderMetadata.class).when(opdConfiguration).getOpdRegisterProviderMetadata();
        doReturn(opdMetadata).when(opdConfiguration).getOpdMetadata();
        doReturn(TestOpdRegisterQueryProviderContract.class).when(opdConfiguration).getOpdRegisterQueryProvider();
        doReturn(opdConfiguration).when(opdLibrary).getOpdConfiguration();
        ReflectionHelpers.setStaticField(OpdLibrary.class, "instance", opdLibrary);

        doReturn(opensrpContext).when(opensrpContext).updateApplicationContext(any(android.content.Context.class));
        doReturn(opensrpContext).when(coreLibrary).context();
        ReflectionHelpers.setStaticField(CoreLibrary.class, "instance", coreLibrary);

        ReflectionHelpers.setStaticField(SyncStatusBroadcastReceiver.class, "singleton", syncStatusBroadcastReceiver);

        Bundle bundle = new Bundle();
        FragmentFactory fragmentFactory = new FragmentFactory();
        fragmentScenario = FragmentScenario.launch(TestBaseOpdRegisterFragment.class, bundle, R.style.AppTheme, fragmentFactory);
    }

    @Test
    public void testCountExecuteShouldUpdateAdapterAccordingly() {
        fragmentScenario.onFragment(fragment -> {
            Fragment spyFragment = spy(fragment);
            RecyclerViewPaginatedAdapter adapter = ReflectionHelpers.getField(spyFragment, "clientAdapter");
            assertEquals(20, adapter.currentlimit);
            assertEquals(0, adapter.currentoffset);
        });
    }

    public static class TestBaseOpdRegisterFragment extends BaseOpdRegisterFragment {

        @Override
        protected void startRegistration() {

        }

        @Override
        protected void performPatientAction(@NonNull CommonPersonObjectClient commonPersonObjectClient) {

        }

        @Override
        protected void goToClientDetailActivity(@NonNull CommonPersonObjectClient commonPersonObjectClient) {

        }

        @Override
        public CommonRepository commonRepository() {
            return mock(CommonRepository.class);
        }

        @Override
        protected boolean isValidFilterForFts(CommonRepository commonRepository) {
            return true;
        }

        @Override
        protected int getLayout() {
            return R.layout.fragment_base_register;
        }
    }

    public static class TestOpdRegisterQueryProviderContract extends OpdRegisterQueryProviderContract {

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
            return null;
        }
    }

}
