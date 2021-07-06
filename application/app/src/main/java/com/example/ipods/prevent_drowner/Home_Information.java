package com.example.ipods.prevent_drowner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class Home_Information extends Fragment {
    /*** DB ***/
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;

    /*** widget ***/
    private EditText inputChildName;
    private EditText inputChildAge;
    private EditText inputChildWeight;
    private ImageButton nextButton;


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

        inputChildName.addTextChangedListener(textWatcher);
        inputChildAge.addTextChangedListener(textWatcher);
        inputChildWeight.addTextChangedListener(textWatcher);

        nextButton = (ImageButton)view.findViewById(R.id.inputChildInformationNext);
        nextButton.setOnClickListener(childInformationNextListener);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if(!inputChildName.getText().toString().isEmpty()
                && !inputChildAge.getText().toString().isEmpty()
                && !inputChildWeight.getText().toString().isEmpty()){

                nextButton.setVisibility(View.VISIBLE);
            }else{
                nextButton.setVisibility(View.GONE);
            }
        }
    };

    private ImageButton.OnClickListener childInformationNextListener = new View.OnClickListener() {
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
