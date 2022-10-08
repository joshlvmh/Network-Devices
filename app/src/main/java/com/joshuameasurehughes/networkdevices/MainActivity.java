package com.joshuameasurehughes.networkdevices;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.ConnectException;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private static final String[] paths = {"AP", "NAS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        spinner = findViewById(R.id.spinner1);
        ArrayAdapter<String>adapter = new ArrayAdapter<>(MainActivity.this,
                android.R.layout.simple_spinner_item,paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void clearScreen (View view) {
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

    public void buttonClick (View view) {
        String command;
        switch(view.getTag().toString()) {
            case "clear":
                clearScreen(view);
                return;
            case "temp":
                command = "./temp.sh";
                break;
            case "shutdown":
                command = "sudo halt";
                break;
            case "disk":
                command = "df -H /dev/sda1";
                break;
            default:
                throw new RuntimeException();
        }

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

        new executeButton().execute(username, password, host, command);
        tv.append("\n\n");
    }

    @SuppressLint("StaticFieldLeak")
    private class executeButton extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            final String[] result = new String[1];
            Resources res = getResources();
            try {
                result[0] = SSHCommand.executeRemoteCommand(strings[0], strings[1], strings[2], res.getInteger(R.integer.port), strings[3]);
            } catch (ConnectException e) {
                result[0] = "Failed to connect\n";
            } catch (Exception e) {
                result[0] = "SSH Failed\n";
            }
            return result[0];
        }

        protected void onPostExecute(String result) {
            TextView tv = findViewById(R.id.textHello);
            tv.append(result);
        }
    }

}