package org.dhis2.utils.CustomViews.orgUnitCascade;

import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.chip.Chip;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.jakewharton.rxbinding2.widget.RxTextView;

import org.dhis2.R;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.databinding.DialogCascadeOrgunitBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 21/05/2018.
 */

public class OrgUnitCascadeDialog extends DialogFragment {
    DialogCascadeOrgunitBinding binding;

    private String title;
    private CascadeOrgUnitCallbacks callbacks;
    private CompositeDisposable disposable;
    private List<Quintet<String, String, String, Integer, Boolean>> orgUnits;
    private ArrayList<Quintet<String, String, String, Integer, Boolean>> chipResults;


    public OrgUnitCascadeDialog() {

    }

    public OrgUnitCascadeDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public OrgUnitCascadeDialog setCallbacks(CascadeOrgUnitCallbacks callbacks) {
        this.callbacks = callbacks;
        return this;
    }

    public OrgUnitCascadeDialog setOrgUnits(List<OrganisationUnitModel> orgUnits) {
        this.orgUnits = new ArrayList<>();
        List<String> orgUnitsUid = new ArrayList<>();

        for (OrganisationUnitModel orgUnit : orgUnits) { //Users OrgUnits
            this.orgUnits.add(Quintet.create(orgUnit.uid(),
                    orgUnit.displayName(),
                    orgUnit.parent() != null ? orgUnit.parent() : "",
                    orgUnit.level(),
                    true));//OrgUnit Uid, OrgUnit Name, Parent Uid, Level, CanBeSelected
            orgUnitsUid.add(orgUnit.uid());
        }

        for (OrganisationUnitModel orgUnit : orgUnits) { //Path OrgUnits
            String[] uidPath = orgUnit.path().split("/");
            String[] namePath = orgUnit.displayNamePath().split("/");
            for (int i = 1; i < uidPath.length; i++) {
                if (!uidPath[i].isEmpty() && !uidPath[i].equals(orgUnit.uid())) {
                    Quintet<String, String, String, Integer, Boolean> quartet = Quintet.create(uidPath[i], namePath[i], i != 1 ? uidPath[i - 1] : "", i, false); //OrgUnit Uid, OrgUnit Name, Parent Uid, Level, CanBeSelected
                    if (!orgUnitsUid.contains(quartet.val0())) {
                        this.orgUnits.add(quartet);
                        orgUnitsUid.add(quartet.val0());
                    }
                }
            }
        }
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_cascade_orgunit, container, false);

        binding.orgUnitEditText.setHint(title);
        binding.acceptButton.setOnClickListener(view->{
            String selectedOrgUnit = ((OrgUnitCascadeAdapter)binding.recycler.getAdapter()).getSelectedOrgUnit();
        });
        binding.cancelButton.setOnClickListener(view->dismiss());
        binding.clearButton.setOnClickListener(view -> binding.orgUnitEditText.getText().clear());

        disposable = new CompositeDisposable();

        disposable.add(RxTextView.textChanges(binding.orgUnitEditText)
                .skipInitialValue()
                .debounce(500, TimeUnit.MILLISECONDS)
                .filter(data -> data != null && !data.toString().isEmpty() && orgUnits != null && !orgUnits.isEmpty())
                .map(textTofind -> {
                    ArrayList<Quintet<String, String, String, Integer, Boolean>> matches = new ArrayList<>();
                    for (Quintet<String, String, String, Integer, Boolean> quartet : orgUnits)
                        if (quartet.val1().toLowerCase().contains(textTofind.toString()))
                            matches.add(quartet);
                    return matches;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> showChips(data),
                        Timber::e
                ));

        binding.recycler.setAdapter(new OrgUnitCascadeAdapter(orgUnits));

        return binding.getRoot();
    }

    private void showChips(ArrayList<Quintet<String, String, String, Integer, Boolean>> data) {
        binding.results.removeAllViews();
        this.chipResults = data;
        for (Quintet<String, String, String, Integer, Boolean> trio : data) {
            if (trio.val4()) { //Only shows selectable orgUnits
                Chip chip = new Chip(getContext());
                chip.setText(trio.val1());
                chip.setOnClickListener(view -> ((OrgUnitCascadeAdapter) binding.recycler.getAdapter()).setOrgUnit(trio));
                binding.results.addView(chip);
            }
        }
    }

    public interface CascadeOrgUnitCallbacks {
        Consumer<CharSequence> textChangedConsumer();
    }

    @Override
    public void dismiss() {
        disposable.clear();
        super.dismiss();
    }
}
