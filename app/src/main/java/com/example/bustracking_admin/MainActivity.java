package com.example.bustracking_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ca.antonious.materialdaypicker.MaterialDayPicker;

public class MainActivity extends AppCompatActivity {
    Button btnFetch, btnUpdate, btnEditStartTime, btnEditEndTime;
    TextView tvStartTime, tvEndTime;
    Switch swMasterEnable;
    MaterialDayPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFetch = findViewById(R.id.btn_fetchvalues);
        btnUpdate = findViewById(R.id.btn_updatevalues);
        btnEditEndTime = findViewById(R.id.btn_change_endtime);
        btnEditStartTime = findViewById(R.id.btn_change_starttime);
        tvEndTime = findViewById(R.id.tv_endtime);
        tvStartTime = findViewById(R.id.tv_starttime);
        swMasterEnable = findViewById(R.id.sw_masterenable);
        picker = findViewById(R.id.picker);

        btnUpdate.setEnabled(false);
        swMasterEnable.setEnabled(false);
        btnEditEndTime.setEnabled(false);
        btnEditStartTime.setEnabled(false);
        picker.setEnabled(false);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Bus Locations").child("Rules");
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String endtime = dataSnapshot.child("endTime").getValue(String.class),
                                startTime = dataSnapshot.child("startTime").getValue(String.class);
                        Boolean masterEnable = dataSnapshot.child("masterEnable").getValue(Boolean.class);
                        String daysAllowed = dataSnapshot.child("allowedDays").getValue(String.class);

                        if (endtime == null) endtime = "<nothing set yet>";
                        if (startTime == null) startTime = "<nothing set yet>";
                        if (masterEnable == null) masterEnable = false;

                        swMasterEnable.setChecked(masterEnable);
                        tvStartTime.setText(startTime);
                        tvEndTime.setText(endtime);

                        btnEditEndTime.setEnabled(true);
                        btnEditStartTime.setEnabled(true);
                        btnUpdate.setEnabled(true);
                        swMasterEnable.setEnabled(true);
                        picker.setEnabled(true);
                        if (daysAllowed == null || daysAllowed.isEmpty()) return;

                        for (int i = 0; i < daysAllowed.length(); i++) {
                            switch (daysAllowed.charAt(i)) {
                                case '1': picker.selectDay(MaterialDayPicker.Weekday.MONDAY); break;
                                case '2': picker.selectDay(MaterialDayPicker.Weekday.TUESDAY); break;
                                case '3': picker.selectDay(MaterialDayPicker.Weekday.WEDNESDAY); break;
                                case '4': picker.selectDay(MaterialDayPicker.Weekday.THURSDAY); break;
                                case '5': picker.selectDay(MaterialDayPicker.Weekday.FRIDAY); break;
                                case '6': picker.selectDay(MaterialDayPicker.Weekday.SATURDAY); break;
                                case '7': picker.selectDay(MaterialDayPicker.Weekday.SUNDAY); break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<MaterialDayPicker.Weekday> l = picker.getSelectedDays();
                StringBuilder finalDayString = new StringBuilder();
                for (MaterialDayPicker.Weekday w : l) {
                    switch (w) {
                        case MONDAY: finalDayString.append("1"); break;
                        case TUESDAY: finalDayString.append("2"); break;
                        case WEDNESDAY: finalDayString.append("3"); break;
                        case THURSDAY: finalDayString.append("4"); break;
                        case FRIDAY: finalDayString.append("5"); break;
                        case SATURDAY: finalDayString.append("6"); break;
                        case SUNDAY: finalDayString.append("7"); break;
                    }
                }
                if (finalDayString.toString().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please select at least one allowed day!", Toast.LENGTH_LONG).show();
                    return;
                }
                if (tvStartTime.getText().toString().equals(tvEndTime.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Can't have same start and end time!", Toast.LENGTH_LONG).show();
                    return;
                }
                ref.child("allowedDays").setValue(finalDayString.toString());
                if (!tvEndTime.getText().toString().contains("<")) ref.child("endTime").setValue(tvEndTime.getText().toString());
                if (!tvStartTime.getText().toString().contains("<")) ref.child("startTime").setValue(tvStartTime.getText().toString());
                ref.child("masterEnable").setValue(swMasterEnable.isChecked());

                Toast.makeText(getApplicationContext(), "Updated!", Toast.LENGTH_LONG).show();
            }
        });

        btnEditStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        tvStartTime.setText(String.format("%02d:%02d:00", i, i1));
                    }
                }, 0,0,true).show();
            }
        });

        btnEditEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        tvEndTime.setText(String.format("%02d:%02d:00", i, i1));
                    }
                }, 0,0,true).show();
            }
        });
    }
}