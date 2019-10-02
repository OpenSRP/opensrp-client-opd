package org.smartregister.opd.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.opd.OpdLibrary;
import org.smartregister.opd.R;
import org.smartregister.opd.configuration.OpdRegisterQueryProviderContract;
import org.smartregister.opd.contract.OpdRegisterFragmentContract;
import org.smartregister.opd.dialog.NoMatchDialogFragment;
import org.smartregister.opd.model.OpdRegisterFragmentModel;
import org.smartregister.opd.presenter.OpdRegisterFragmentPresenter;
import org.smartregister.opd.provider.OpdRegisterProvider;
import org.smartregister.opd.utils.ConfigurationInstancesHelper;
import org.smartregister.opd.utils.OpdUtils;
import org.smartregister.opd.utils.OpdViewConstants;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-13
 */

public abstract class BaseOpdRegisterFragment extends BaseRegisterFragment implements OpdRegisterFragmentContract.View {

    private static final String DUE_FILTER_TAG = "PRESSED";
    private View view;
    private View dueOnlyLayout;
    private boolean dueFilterActive = false;
    private OpdRegisterQueryProviderContract opdRegisterQueryProvider;

    public BaseOpdRegisterFragment() {
        super();

        opdRegisterQueryProvider = ConfigurationInstancesHelper.newInstance(OpdLibrary.getInstance().getOpdConfiguration().getOpdRegisterQueryProvider());
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);

        this.view = view;

        // Update top left icon
        qrCodeScanImageView = view.findViewById(org.smartregister.R.id.scanQrCode);
        if (qrCodeScanImageView != null) {
            qrCodeScanImageView.setVisibility(View.GONE);
        }

        // Update Search bar
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        View searchBarLayout = view.findViewById(org.smartregister.R.id.search_bar_layout);
        searchBarLayout.setLayoutParams(params);
        searchBarLayout.setBackgroundResource(R.color.chw_primary);
        searchBarLayout.setPadding(searchBarLayout.getPaddingLeft()
                , searchBarLayout.getPaddingTop()
                , searchBarLayout.getPaddingRight()
                , (int) OpdUtils.convertDpToPixel(10, getActivity()));


        if (getSearchView() != null) {
            getSearchView().setBackgroundResource(R.color.white);
            getSearchView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_search, 0, 0, 0);
            getSearchView().setTextColor(getResources().getColor(R.color.text_black));
        }

        // Update title name
        ImageView logo = view.findViewById(R.id.opensrp_logo_image_view);
        if (logo != null) {
            logo.setVisibility(View.GONE);
        }

        AppCompatTextView titleView = view.findViewById(R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(getString(getToolBarTitle()));
            //titleView.setFontVariant(FontVariant.REGULAR);
            titleView.setPadding(0, titleView.getTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }

        View navbarContainer = view.findViewById(R.id.register_nav_bar_container);
        navbarContainer.setFocusable(false);

        View topLeftLayout = view.findViewById(R.id.top_left_layout);
        topLeftLayout.setVisibility(View.GONE);

        View topRightLayout = view.findViewById(R.id.top_right_layout);
        topRightLayout.setVisibility(View.VISIBLE);

        View sortFilterBarLayout = view.findViewById(R.id.register_sort_filter_bar_layout);
        sortFilterBarLayout.setVisibility(View.GONE);

        View filterSortLayout = view.findViewById(R.id.filter_sort_layout);
        filterSortLayout.setVisibility(View.GONE);

        dueOnlyLayout = view.findViewById(R.id.due_only_layout);
        dueOnlyLayout.setVisibility(View.VISIBLE);
        dueOnlyLayout.setOnClickListener(registerActionHandler);

        topRightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistration();
            }
        });
    }

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        presenter = new OpdRegisterFragmentPresenter(this, new OpdRegisterFragmentModel());
    }

    @Override
    public void setUniqueID(String s) {
        if (getSearchView() != null) {
            getSearchView().setText(s);
        }
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> hashMap) {
        //// TODO: 15/08/19
    }

    @Override
    protected void onResumption() {
        if (dueFilterActive && dueOnlyLayout != null) {
            enableDueOnlyFilter(dueOnlyLayout, dueFilterActive);
        } else {
            super.onResumption();
        }
    }

    @Override
    protected String getMainCondition() {
        return "";
    }

    @Override
    protected String getDefaultSortQuery() {
        return "";
    }

    @Override
    abstract protected void startRegistration();

    @Override
    protected void onViewClicked(View view) {
        // TODO: Abstract
        if (getActivity() == null) {
            return;
        }

        if (view.getId() == R.id.due_only_layout) {
            toggleFilterSelection(view);
        } else if (view.getTag(R.id.VIEW_TYPE) != null) {
            Object viewClient = view.getTag(R.id.VIEW_CLIENT);

            if (viewClient != null) {
                if (viewClient instanceof CommonPersonObjectClient) {
                    if (view.getTag(R.id.VIEW_TYPE).equals(OpdViewConstants.Provider.CHILD_COLUMN)) {

                        goToClientDetailActivity((CommonPersonObjectClient) viewClient);
                    } else if (view.getTag(R.id.VIEW_TYPE).equals(OpdViewConstants.Provider.ACTION_BUTTON_COLUMN)) {
                        performPatientAction((CommonPersonObjectClient) viewClient);
                    }
                } else {
                    Timber.e(new Exception(), "Value for key[%d] is not a CommonPersonObjectClient but is of type %s"
                            , R.id.VIEW_CLIENT
                            , viewClient.getClass().getName());
                }
            }
        }
    }

    abstract protected void performPatientAction(@NonNull CommonPersonObjectClient commonPersonObjectClient);

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus) || FetchStatus.nothingFetched.equals(fetchStatus)) && dueFilterActive && dueOnlyLayout != null) {
            enableDueOnlyFilter(dueOnlyLayout, dueFilterActive);
            Utils.showShortToast(getActivity(), getString(R.string.sync_complete));
            refreshSyncProgressSpinner();
        } else {
            super.onSyncInProgress(fetchStatus);
        }
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        if (!SyncStatusBroadcastReceiver.getInstance().isSyncing() && (FetchStatus.fetched.equals(fetchStatus)
                || FetchStatus.nothingFetched.equals(fetchStatus)) && (dueFilterActive && dueOnlyLayout != null)) {
            enableDueOnlyFilter(dueOnlyLayout, dueFilterActive);
            Utils.showShortToast(getActivity(), getString(R.string.sync_complete));
            refreshSyncProgressSpinner();
        } else {
            super.onSyncComplete(fetchStatus);
        }

        if (syncProgressBar != null) {
            syncProgressBar.setVisibility(View.GONE);
        }
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void refreshSyncProgressSpinner() {
        super.refreshSyncProgressSpinner();
        if (syncButton != null) {
            syncButton.setVisibility(View.GONE);
        }
    }

    abstract protected void goToClientDetailActivity(@NonNull CommonPersonObjectClient commonPersonObjectClient);

    public void toggleFilterSelection(View dueOnlyLayout) {
        if (dueOnlyLayout != null) {
            if (dueOnlyLayout.getTag() == null) {
                // Let's enable the due-filter
                dueFilterActive = true;
                enableDueOnlyFilter(dueOnlyLayout, true);
            } else if (dueOnlyLayout.getTag().toString().equals(DUE_FILTER_TAG)) {
                // Let's disable the due-filter
                dueFilterActive = false;
                enableDueOnlyFilter(dueOnlyLayout, false);
            }
        }
    }

    private void enableDueOnlyFilter(@NonNull View dueOnlyLayout, boolean enable) {
        String tag = enable ? DUE_FILTER_TAG : null;
        String mainConditionString = enable ? presenter().getDueFilterCondition() : "";

        filter(searchText(), "", mainConditionString);
        dueOnlyLayout.setTag(tag);
        switchViews(dueOnlyLayout, false);
    }

    protected void filter(String filterString, String joinTableString, String mainConditionString) {
        filters = filterString;
        joinTable = joinTableString;
        mainCondition = mainConditionString;
        filterandSortExecute(countBundle());
    }

    private String searchText() {
        return (getSearchView() == null) ? "" : getSearchView().getText().toString();
    }

    private void switchViews(View dueOnlyLayout, boolean isPress) {
        TextView dueOnlyTextView = dueOnlyLayout.findViewById(R.id.due_only_text_view);
        if (isPress) {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_on, 0);
        } else {
            dueOnlyTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_due_filter_off, 0);

        }
    }

    @Override
    public void initializeAdapter() {
        OpdRegisterProvider childRegisterProvider = new OpdRegisterProvider(getActivity(), registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, childRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public OpdRegisterFragmentContract.Presenter presenter() {
        return (OpdRegisterFragmentContract.Presenter) presenter;
    }

    protected int getToolBarTitle() {
        return R.string.opd_register_title_name;
    }

    @Override
    public void showNotFoundPopup(String uniqueId) {
        if (getActivity() == null) {
            return;
        }
        NoMatchDialogFragment.launchDialog((BaseRegisterActivity) getActivity(), DIALOG_TAG, uniqueId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {

        if (id == LOADER_ID) {// Returns a new CursorLoader
            return new CursorLoader(getActivity()) {
                @Override
                public Cursor loadInBackground() {
                    // Count query
                    // Select register query
                    String query = filterAndSortQuery();
                    return commonRepository().rawCustomQueryForAdapter(query);
                }
            };
        }// An invalid id was passed in
        return null;
    }

    private String filterAndSortQuery() {
        SmartRegisterQueryBuilder sqb = new SmartRegisterQueryBuilder(mainSelect);

        String query = "";
        try {
            if (isValidFilterForFts(commonRepository())) {
                String sql = opdRegisterQueryProvider.getObjectIdsQuery(filters);
                sql = sqb.addlimitandOffset(sql, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset());

                List<String> ids = commonRepository().findSearchIds(sql);
                query = opdRegisterQueryProvider.mainSelectWhereIDsIn();

                String joinedIds = "'" + StringUtils.join(ids, "','") + "'";
                return query.replace("%s", joinedIds);
            } else {
                if (!TextUtils.isEmpty(filters) && TextUtils.isEmpty(Sortqueries)) {
                    sqb.addCondition(filters);
                    query = sqb.orderbyCondition(Sortqueries);
                    query = sqb.Endquery(sqb.addlimitandOffset(query
                            , clientAdapter.getCurrentlimit()
                            , clientAdapter.getCurrentoffset()));
                }

            }
        } catch (Exception e) {
            Timber.e(e);
        }

        return query;
    }

    @Override
    public void countExecute() {
        try {
            int totalCount = 0;
            for (String sql: opdRegisterQueryProvider.countExecuteQueries(filters)) {
                Timber.i(sql);
                totalCount += commonRepository().countSearchIds(sql);
            }

            clientAdapter.setTotalcount(totalCount);
            Timber.i("Total Register Count %d", clientAdapter.getTotalcount());

            clientAdapter.setCurrentlimit(20);
            clientAdapter.setCurrentoffset(0);
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}