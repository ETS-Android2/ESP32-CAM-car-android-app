package com.theretrocenter.esp32_camandroidapp;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.theretrocenter.esp32_camandroidapp.RemoteWIFICar.RemoteWIFICar;
import com.theretrocenter.esp32_camandroidapp.utilities.ListViewHeightBasedOnChildren;
import com.theretrocenter.esp32_camandroidapp.utilities.Preferences;

import java.util.ArrayList;
import java.util.HashMap;

public class ConfigurationFragment extends Fragment {

    private MainActivity mainActivity = new MainActivity();
    private RemoteWIFICar remoteWIFICar = new RemoteWIFICar();
    private Preferences preferences = Preferences.getInstance(mainActivity);
    private Context thisContext;
    private String selectedUIControl = "";

    // Get saved preferences
    private String carUIControl = preferences.getData("CarUIControl");
    private String remoteWIFICarIP = preferences.getData("RemoteWIFICarIP");
    private String ssidWIFISaved = preferences.getData("SSIDWIFI");


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        thisContext = container.getContext();

        //ConnectivityManager cm = (ConnectivityManager) thisContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return inflater.inflate(R.layout.configuration, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Manage text edit
        EditText ssidWIFIET = view.findViewById(R.id.SSIDWIFIEditText);
        EditText passWIFIET = view.findViewById(R.id.PassWIFIEditText);

        // Show current saved IP
        TextView currentIP = view.findViewById(R.id.currentIPStatus);
        currentIP.setText("Current IP: " + remoteWIFICarIP);

        // Set SSID saved in text edit
        ssidWIFIET.setText(ssidWIFISaved);

        // Set UI control radio button
        if (carUIControl.equals("joystick")) {
            RadioButton joystickUIControl = view.findViewById(R.id.radioButtonjoystick);
            joystickUIControl.setChecked(true);
        } else {
            RadioButton buttonsUIControl = view.findViewById(R.id.radioButtonButtons);
            buttonsUIControl.setChecked(true);
        }

        // Add Radio group listener
        RadioGroup carUIControlRadioGroup = (RadioGroup) view.findViewById(R.id.carUIControlRadioGroup);
        carUIControlRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Set var for UI control selected
                switch(checkedId){
                    case R.id.radioButtonButtons:
                        selectedUIControl = "buttons";
                        break;
                    case R.id.radioButtonjoystick:
                        selectedUIControl = "joystick";
                        break;
                }
            }
        });

        view.findViewById(R.id.scanWIFIBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clickView) {
                try {
                    // Get json WIFI list
                    // ArrayList<HashMap<String, String>> jsonlist = remoteWIFICar.getWIFIList(remoteWIFICarIP);
                    ArrayList<HashMap<String, String>> jsonlist = remoteWIFICar.getWIFIList("192.168.29.176:8080");

                    // Set json WIFI list on listview
                    ListView listView = (ListView) view.findViewById(R.id.listWIFI);
                    listView.setAdapter(new WIFIListAdapter(
                            thisContext,
                            R.layout.wifi_list,
                            jsonlist,
                            ssidWIFIET));

                    // Adjust height to ListView
                    ListViewHeightBasedOnChildren ListViewHeightBasedOnChildren = new ListViewHeightBasedOnChildren();
                    ListViewHeightBasedOnChildren.setHeight(listView);

                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        view.findViewById(R.id.saveBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UIControl = "";

                // Get data from input edit text
                String ssidWIFI = ssidWIFIET.getText().toString();
                String passWIFI = passWIFIET.getText().toString();

                // Validate SSID or Password changed
                if (!ssidWIFISaved.equals(ssidWIFI) || !passWIFI.equals("")) {
                    try {
                        // Save ssid in preferences
                        preferences.saveData("SSIDWIFI", ssidWIFI);

                        // Convert ssid and password to Base64
                        byte[] bytes = ssidWIFI.getBytes("UTF-8");
                        String text = new String(bytes, "UTF-8");
                        String ssidB64 = Base64.encodeToString(ssidWIFI.getBytes("UTF-8"), Base64.NO_WRAP + Base64.URL_SAFE);
                        String passB64 = Base64.encodeToString(passWIFI.getBytes("UTF-8"), Base64.NO_WRAP + Base64.URL_SAFE);

                        // Set default UI control
                        if (selectedUIControl.equals("")) {
                            UIControl = "buttonstext";
                        } else {
                            UIControl = selectedUIControl.equals("buttons") ? "buttonstext" : selectedUIControl;
                        }

                        // Send new configuration to WIFI car
                        remoteWIFICar.saveWIFISettings(ssidB64, passB64, UIControl, remoteWIFICarIP);

                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }

                // Save preferred UI control in preferences
                preferences.saveData("CarUIControl", selectedUIControl);

                // Return to control car fragment layout
                NavHostFragment.findNavController(ConfigurationFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

        view.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Return to control car fragment layout
                NavHostFragment.findNavController(ConfigurationFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });

    }
}