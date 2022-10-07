package com.joshuameasurehughes.networkdevices;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private static final String[] paths = {"AP", "NAS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        spinner = (Spinner)findViewById(R.id.spinner1);
        ArrayAdapter<String>adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
    public void onBtnClick (View view) throws RuntimeException {
        TextView tv = findViewById(R.id.textHello);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        String username;
        String password;
        String host;

        int pos = spinner.getSelectedItemPosition();
        switch (pos) {
            case 0:
                username = getString(R.string.username_ap);
                password = getString(R.string.password_ap);
                host = getString(R.string.host_ap);
                break;
            case 1:
                username = getString(R.string.username_nas);
                password = getString(R.string.password_nas);
                host = getString(R.string.host_nas);
                break;
            default:
                throw new RuntimeException("spinner needs to be set");
        }

        final String TAG = "TESTING";
        final String[] result = new String[1];

        Resources res = getResources();

        String finalUsername = username;
        String finalPassword = password;
        String finalHost = host;
        new AsyncTask<Integer, Void, Void>() {
            @Override
            protected Void doInBackground(Integer... params) {
                try {
                    //todo TAG
                    //Log.d(TAG,
                    result[0] = SSHCommand.executeRemoteCommand(finalUsername, finalPassword, finalHost, res.getInteger(R.integer.port));
                    runOnUiThread(() -> tv.append(result[0]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(1);
    }

    public void onClrClick (View view) {
        TextView tv = findViewById(R.id.textHello);
        tv.setText("");
        tv.scrollTo(0,0);

        int pos = spinner.getSelectedItemPosition();
        if (pos == 0) {
            tv.setText("AP\n----------------\n");
        }
        else {
            tv.setText("NAS\n----------------\n");
        }
    }

    public void shell_exec(String cmd, TextView tv)
    {
        try
        {
            Process p=Runtime.getRuntime().exec(cmd);
            BufferedReader b=new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line=b.readLine())!=null)
            {
                tv.append("\n"+line);

            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}