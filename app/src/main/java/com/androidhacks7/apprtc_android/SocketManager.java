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

import android.util.Log;

import com.androidhacks7.apprtc_android.utils.ServerConfiguration;
import com.androidhacks7.apprtc_android.listeners.SignalingListener;
import com.androidhacks7.apprtc_android.listeners.SocketMessageListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

/**
 * Created by androidhacks7 on 12/19/2015.
 */
public class SocketManager {

    public static final String TAG = SocketManager.class.getSimpleName();

    private static SocketManager instance = null;

    private WebSocket mSocket;

    private SocketMessageListener socketMessageListener;
    private SignalingListener signalingListener;

    private static final Gson gson = new GsonBuilder().create();


    private SocketManager() {
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void init() {
            AsyncHttpClient.getDefaultInstance().websocket(ServerConfiguration.SOCKET_ENDPOINT,
                    null, null).then(new FutureCallback<WebSocket>() {

                @Override
                public void onCompleted(Exception e, WebSocket result) {
                    if (e != null) {
                        Log.d(TAG,"connect to " +
                                ServerConfiguration.SOCKET_ENDPOINT + " failed");
                        e.printStackTrace();
                        return;
                    }
                    mSocket = result;
                    mSocket.setStringCallback(new One2OneStringCallback());
                    Log.d(TAG,"connect to " +
                            ServerConfiguration.SOCKET_ENDPOINT + " completed");
                }
            });
    }

    private class One2OneStringCallback implements WebSocket.StringCallback {
        @Override
        public void onStringAvailable(String s) {
            Log.i(TAG,"Incoming msg:" + s);

            if (signalingListener == null) {
                return ;
            }

            JsonObject jsonMessage = gson.fromJson(s, JsonObject.class);
            switch (jsonMessage.get("id").getAsString()) {
                case "registerResponse":
                    signalingListener.onRegisterResponse(jsonMessage);
                    break;
                case "callResponse":
                    signalingListener.onCallResponse(jsonMessage);
                    break;
                case "startCommunication":
                    signalingListener.onStartCommunication(jsonMessage);
                    break;
                case "incomingCall":
                   signalingListener.onCallReceived(jsonMessage);
                    break;
                case "iceCandidate":
                    signalingListener.onIceCandidate(jsonMessage);
                    break;
                case "getUserListResponse":
                    signalingListener.onUserList(jsonMessage);
                    break;
                default:
                    break;
            }
        }
    }

    public void setReceiver(SignalingListener receiver) {
        this.signalingListener = (SignalingListener) receiver;
    }



    public void onSend(String message) {
        Log.i(this.getClass().toString(),"Sending msg:" + message);
        mSocket.send(message);
    }

}
