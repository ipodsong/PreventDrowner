package com.example.ipods.prevent_drowner;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Home_Information_Display extends Fragment {

    /*** widget ***/
    private TextView displayChildName;
    private TextView displayChildAge;
    private TextView displayChildWeight;

    public static Home_Information_Display newInstance(String name, int age, float weight) {
        Home_Information_Display fragment = new Home_Information_Display();
        Bundle args = new Bundle();
        args.putString(Common.CHILD_NAME, name);
        args.putInt(Common.CHILD_AGE, age);
        args.putFloat(Common.CHILD_WEIGHT, weight);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_home_information_display, null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayChildName = (TextView)view.findViewById(R.id.displayChildName);
        displayChildAge = (TextView)view.findViewById(R.id.displayChildAge);
        displayChildWeight = (TextView)view.findViewById(R.id.displayChildWeight);

        displayChildName.setText(getArguments().getString(Common.CHILD_NAME));
        displayChildAge.setText(getArguments().getString(Common.CHILD_AGE));
        displayChildWeight.setText(getArguments().getString(Common.CHILD_WEIGHT));
    }
}
