/*
 * Copyright 2018 Mayur Rokade
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.mayurrokade.chatapp.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mayurrokade.chatapp.R;
import com.mayurrokade.chatapp.data.ChatMessage;
import com.mayurrokade.chatapp.eventservice.EventListener;
import com.mayurrokade.chatapp.eventservice.EventService;
import com.mayurrokade.chatapp.eventservice.EventServiceImpl;
import com.mayurrokade.chatapp.util.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

/*
* */

public class ChatActivity
        extends AppCompatActivity
        implements ChatContract.View, EventListener {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private RecyclerView rvChatMessages;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatAdapter mChatAdapter;
    private EditText etSendMessage;
    private Button btnSendMessage;
    private EventService mEventService;

    // TODO show popup to set username
    private String mUsername = "SoUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etSendMessage = findViewById(R.id.etSendMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        mEventService = EventServiceImpl.getInstance();

        setupChatMessages();
        setupSendButton();

        try {
            mEventService.connect(mUsername);
            mEventService.setEventListener(this);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mEventService.disconnect();
    }

    @Override
    public void initView() {

    }

    @Override
    public void setPresenter(ChatContract.Presenter presenter) {

    }

    @Override
    public void showMessage(final String message, boolean isError) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        message,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void showProgress() {

    }

    @Override
    public void hideProgress() {

    }

    private void setupChatMessages() {
        mChatAdapter = new ChatAdapter(new ArrayList<ChatMessage>(), this);
        mLayoutManager = new LinearLayoutManager(this);
        rvChatMessages.setAdapter(mChatAdapter);
        rvChatMessages.setLayoutManager(mLayoutManager);
    }

    private void setupSendButton() {
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: sendMessage");
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = etSendMessage.getText().toString().trim();

        if (TextUtils.isValidString(message)) {
            Log.i(TAG, "sendMessage: ");
            ChatMessage chatMessage = new ChatMessage(mUsername, message);
            mEventService.sendMessage(chatMessage);
            addMessage(chatMessage);
            etSendMessage.setText("");
        }
    }

    private void addMessage(ChatMessage chatMessage) {
        mChatAdapter.addNewMessage(chatMessage);
        rvChatMessages.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    @Override
    public void onConnect(final Object... args) {
        showMessage("Connected", false);
    }

    @Override
    public void onDisconnect(final Object... args) {
        showMessage("Disconnected", false);

    }

    @Override
    public void onConnectError(final Object... args) {
        showMessage("Connection Error", false);
    }

    @Override
    public void onConnectTimeout(final Object... args) {
        showMessage("Connection Timeout", false);

    }

    @Override
    public void onNewMessage(final Object... args) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                    ChatMessage chatMessage = new ChatMessage(username, message);
                    addMessage(chatMessage);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
            }
        });
    }
}
