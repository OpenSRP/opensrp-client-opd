package org.smartregister.opd.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.smartregister.opd.R;
import org.smartregister.opd.contract.OpdProfileFragmentContract;
import org.smartregister.opd.domain.YamlConfigWrapper;
import org.smartregister.opd.presenter.OpdProfileFragmentPresenter;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-09-27
 */

public class ProfileVisitFragment extends BaseProfileFragment implements OpdProfileFragmentContract.View {

    private List<YamlConfigWrapper> lastContactDetails;
    private List<YamlConfigWrapper> lastContactTests;
    private LinearLayout testsDisplayLayout;
    private OpdProfileFragmentContract.Presenter presenter;

    public static ProfileVisitFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        ProfileVisitFragment fragment = new ProfileVisitFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new OpdProfileFragmentPresenter(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
    }

    @Override
    protected void onCreation() {
        lastContactDetails = new ArrayList<>();
        lastContactTests = new ArrayList<>();
        if (testsDisplayLayout != null) {
            testsDisplayLayout.removeAllViews();
        }
    }

    @Override
    protected void onResumption() {
        lastContactDetails = new ArrayList<>();
        lastContactTests = new ArrayList<>();
        if (testsDisplayLayout != null) {
            testsDisplayLayout.removeAllViews();
        }
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy(false);
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_profile_contacts, container, false);
        LinearLayout testLayout = fragmentView.findViewById(R.id.test_layout);

        testsDisplayLayout = testLayout.findViewById(R.id.test_display_layout);
        return fragmentView;
    }
}
