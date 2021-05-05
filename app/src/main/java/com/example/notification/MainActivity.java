package com.example.notification;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

//import android.support.v4.app.NotificationCompat;
//import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {


    List<String> data = new ArrayList<>();

    int notifier_counter = 0;
    NotificationManager notificationManager;
    RadioButton radioButtonFirst;
    RadioButton radioButtonSecond;
    RadioGroup radioGroup;
    EditText editText;
    Button btnNotification;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);


        data.add("Bitcoin");
        data.add("Ethereum");
        data.add("Litecoin");
        data.add("Ripple");

        createNotificationGroups();
        createNotificationChannels();


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, data);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);



        radioGroup.check(R.id.radioButton1);
        radioGroup.check(R.id.radioButton2);

        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editText.getText().toString().length() > 0) {
                    String channel_id = "";
                    String group_id = "";
                    PendingIntent contentIntent = PendingIntent.getActivity
                            (MainActivity.this, 0, new Intent
                                    (MainActivity.this, NotificationView.class), 0);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


                        RadioButton radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                        group_id = radioButton.getText().toString();
                        channel_id = notificationManager.getNotificationChannel
                                (spinner.getSelectedItem().toString() + "_" + group_id).getId();
                        contentIntent = PendingIntent.getActivity
                                (MainActivity.this, 0, new Intent
                                                (MainActivity.this, NotificationView.class).putExtra
                                                ("importance", notificationManager.getNotificationChannel
                                                        (channel_id).getImportance()).putExtra("channel_id", channel_id),
                                        PendingIntent.FLAG_UPDATE_CURRENT);
                    }


                    NotificationCompat.Builder notification = new NotificationCompat.Builder
                            (MainActivity.this, channel_id)
                            .setContentTitle(spinner.getSelectedItem().toString())
                            .setContentText(editText.getText().toString())
                            .setGroup(group_id)
                            .setContentIntent(contentIntent)
                            .setSmallIcon(R.mipmap.ic_launcher);

                    notifier_counter++;
                    notificationManager.notify(notifier_counter, notification.build());
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter something in EditText", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void initViews() {
        spinner = findViewById(R.id.spinner);
        btnNotification = findViewById(R.id.btnNotification);
        editText = findViewById(R.id.inContent);
        radioGroup = findViewById(R.id.radioGroup);
        radioButtonFirst = findViewById(R.id.radioButton1);
        radioButtonSecond = findViewById(R.id.radioButton2);
    }

    private void createNotificationGroups() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<NotificationChannelGroup> list = new ArrayList<>();
            list.add(new NotificationChannelGroup(radioButtonFirst.getText().toString(), radioButtonFirst.getText()));
            list.add(new NotificationChannelGroup(radioButtonSecond.getText().toString(), radioButtonSecond.getText()));

            notificationManager.createNotificationChannelGroups(list);

        }
    }


    private void createNotificationChannels() {
        for (String s : data) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

                NotificationChannel notificationChannel = new NotificationChannel
                        (s + "_" + radioButtonFirst.getText().toString(), s, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);
                notificationChannel.setGroup(radioButtonFirst.getText().toString());
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                NotificationChannel notificationChannel2 = new NotificationChannel(s + "_" + radioButtonSecond.getText().toString(), s, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel2.enableLights(true);
                notificationChannel2.enableVibration(true);
                notificationChannel2.setGroup(radioButtonSecond.getText().toString());
                notificationChannel2.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});


                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                    notificationManager.createNotificationChannel(notificationChannel2);
                }
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final Bundle bundle = intent.getExtras();
            int importance = -1;
            if (bundle != null) {
                importance = bundle.getInt("importance");
            }
            if (importance != -1) {

                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage("Goto settings to change the Notification channel")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateNotificationSettings(bundle.getString
                                        ("channel_id"));
                            }
                        }).setNegativeButton("CANCEL", null)
                        .show();


            }

        }


    }

    private void updateNotificationSettings(String channel_id) {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel_id);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }


}

