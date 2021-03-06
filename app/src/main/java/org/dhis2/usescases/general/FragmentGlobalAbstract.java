package org.dhis2.usescases.general;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dhis2.utils.Constants;
import org.dhis2.utils.OnDialogClickListener;

import java.lang.reflect.Type;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */

public abstract class FragmentGlobalAbstract extends android.support.v4.app.Fragment implements AbstractActivityContracts.View {
    public ViewDataBinding binding;
    public int containerId;

    //region lifecycle

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //endregion

    @Override
    public void startActivity(@NonNull Class<?> destination, @Nullable Bundle bundle, boolean finishCurrent, boolean finishAll, @Nullable ActivityOptionsCompat transition) {
        getAbstracContext().startActivity(destination, bundle, finishCurrent, finishAll, transition);
    }

    public int getContainerId() {
        return containerId;
    }

    @Override
    public ActivityGlobalAbstract getAbstractActivity() {
        return (ActivityGlobalAbstract) getActivity();
    }

    @Override
    public void back() {
        getAbstracContext().back();
    }

    @Override
    public ActivityGlobalAbstract getAbstracContext() {
        return (ActivityGlobalAbstract) getActivity();
    }

    @Override
    public void hideKeyboard() {

    }

    @Override
    public void displayMessage(String message) {
        getAbstractActivity().displayMessage(message);
    }

    @Override
    public void showInfoDialog(String title, String message) {
        getAbstractActivity().showInfoDialog(title, message);
    }

    @Override
    public AlertDialog showInfoDialog(String title, String message, OnDialogClickListener clickListener) {
        return getAbstractActivity().showInfoDialog(title, message, clickListener);
    }

    @Override
    public void setTutorial() {
    }

    @Override
    public void showTutorial(boolean shacked) {
    }

    @Override
    public void showDescription(String description) {
        getAbstractActivity().showDescription(description);
    }

    @Override
    public <T> void saveListToPreference(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        getSharedPreferences().edit().putString(key, json).apply();
    }

    @Override
    public <T> List<T> getListFromPreference(String key) {
        Gson gson = new Gson();
        String json = getAbstracContext().getSharedPreferences(Constants.SHARE_PREFS, MODE_PRIVATE).getString(key, null);
        Type type = new TypeToken<List<T>>() {
        }.getType();

        return gson.fromJson(json, type);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return getAbstractActivity().getSharedPreferences();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
