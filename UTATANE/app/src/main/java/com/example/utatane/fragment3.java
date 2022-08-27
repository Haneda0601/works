package com.example.utatane;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class fragment3 extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout3, container, false);
    }

    public void onViewCreated(View view,Bundle sevedInstanceState) {
        super.onViewCreated(view, sevedInstanceState);

    }
}
