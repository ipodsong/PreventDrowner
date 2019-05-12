package com.example.ipods.prevent_drowner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class Home_Information extends Fragment {
    /*** DB ***/
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;

    /*** widget ***/
    private EditText inputChildName;
    private EditText inputChildAge;
    private EditText inputChildWeight;
    private Button nextButton;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefEditor = mPref.edit();

        return inflater.inflate(R.layout.activity_home_information, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputChildName = (EditText)view.findViewById(R.id.inputChildName);
        inputChildAge = (EditText)view.findViewById(R.id.inputChildAge);
        inputChildWeight = (EditText)view.findViewById(R.id.inputChildWeight);

        nextButton = (Button)view.findViewById(R.id.inputChildInformationNext);
        nextButton.setOnClickListener(childInformationNextListener);
    }

    private Button.OnClickListener childInformationNextListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mPrefEditor.putString(Common.CHILD_NAME, inputChildName.getText().toString());
            mPrefEditor.putInt(Common.CHILD_AGE, Integer.parseInt(inputChildAge.getText().toString()));
            mPrefEditor.putFloat(Common.CHILD_WEIGHT, Float.parseFloat(inputChildWeight.getText().toString()));
            mPrefEditor.commit();

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.information_frame,  Home_Information_Display.newInstance(
                    inputChildName.getText().toString(),
                    Integer.parseInt(inputChildAge.getText().toString()),
                    Float.parseFloat(inputChildWeight.getText().toString())));
            fragmentTransaction.commit();
        }
    };
}
