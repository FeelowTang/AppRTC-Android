/*
 * Copyright (c) 2016 Androidhacks7
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.androidhacks7.apprtc_android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.androidhacks7.apprtc_android.utils.AppConstants;
import com.androidhacks7.apprtc_android.utils.JSONConstants;
import com.androidhacks7.apprtc_android.listeners.SignalingListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by androidhacks7 on 12/24/2015.
 */
public class RegistrationActivity extends Activity implements SignalingListener {

    private EditText userName;

    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private String currentUser;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        SocketManager socketManager = SocketManager.getInstance();

        socketManager.setReceiver(this);
        socketManager.init();
        showUserDialog(AppConstants.DIALOG_USERNAME, null);
        progressBar = new ProgressBar(this);
    }

    private void showUserDialog(int type, final Object... args) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (type == AppConstants.DIALOG_USERNAME) {
            builder.setMessage("Enter User Name: ");
            userName = new EditText(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            userName.setLayoutParams(lp);
            builder.setView(userName);
            builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    register();
                }
            });
            builder.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        } else if (type == AppConstants.DIALOG_ACCEPT_REJECT) {
            try {
                final JSONObject jsonObject = new JSONObject(args[0].toString());
                builder.setMessage("Incoming call from " + jsonObject.get(JSONConstants.CALLER));
                builder.setPositiveButton("Attend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(RegistrationActivity.this, VideoCallActivity.class);
                        intent.putExtra(JSONConstants.CALL_PARAMS, args[0].toString());
                        intent.putExtra(JSONConstants.REJECT_CALL, false);
                        startActivity(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            declineCall(jsonObject.get(JSONConstants.CALLER).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        builder.create().show();
    }

    private void declineCall(String caller) {
        try {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", "incomingCallResponse");
            jsonObject.put("from", caller);
            jsonObject.put("callResponse", "reject");
            SocketManager.getInstance().onSend(jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void register() {
        currentUser = userName.getText().toString();
        JsonObject register = new JsonObject();
        register.addProperty("id", "register");
        register.addProperty("name", userName.getText().toString());

        SocketManager socketManager = SocketManager.getInstance();

        socketManager.onSend(register.toString());
    }

    private void parseUserList(JsonObject jsonObject) {
        ArrayList<String> userNames = new ArrayList<String>();

        JsonArray jsonArray = jsonObject.getAsJsonArray("response");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonElement user = jsonArray.get(i);
            if (!userName.getText().toString().equals(user.getAsString())) {
                userNames.add((String) user.getAsString());
            }
        }
        updateUI(userNames);
    }

    private void updateUI(final ArrayList<String> userNames) {
        ListView listView = (ListView) findViewById(R.id.userList);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "Initiating call to " + userNames.get(i));
                Intent intent = new Intent(RegistrationActivity.this, VideoCallActivity.class);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(JSONConstants.CALLER, currentUser);
                jsonObject.addProperty(JSONConstants.RECEIVER, userNames.get(i));

                intent.putExtra(JSONConstants.MAKE_CALL, true);
                intent.putExtra(JSONConstants.CALL_PARAMS, jsonObject.toString());
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    public void onCallReceived(final JsonObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showUserDialog(AppConstants.DIALOG_ACCEPT_REJECT, jsonObject);
            }
        });
    }

    @Override
    public void onUserList(final JsonObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parseUserList(jsonObject);
            }
        });
    }

    @Override
    public void onRegisterResponse(JsonObject jsonObject) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
                //get User list
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", "getUserList");
                jsonObject.addProperty("name", userName.getText().toString());
                SocketManager socketManager = SocketManager.getInstance();
                socketManager.onSend(jsonObject.toString());
            }
        });
    }

    @Override
    public void onCallResponse(JsonObject jsonObject) {

    }

    @Override
    public void onIceCandidate(JsonObject jsonObject) {

    }

    @Override
    public void onStartCommunication(JsonObject jsonObject) {

    }
}
