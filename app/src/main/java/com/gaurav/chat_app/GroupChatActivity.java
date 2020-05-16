package com.gaurav.chat_app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentgroupname, currentuserid, currentusername, currentdate, currenttime;
    private FirebaseAuth mAuth;
    private DatabaseReference usersref,groupnameref, groupmessagekeyref, group_info_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentgroupname = getIntent().getExtras().get("groupname").toString();

        mAuth = FirebaseAuth.getInstance();
        currentuserid= mAuth.getCurrentUser().getUid();
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        groupnameref = FirebaseDatabase.getInstance().getReference().child("Group").child(currentgroupname).child("messages");
        group_info_ref = FirebaseDatabase.getInstance().getReference().child("Group").child(currentgroupname).child("participants");

        InitialiseFields();
        GetUserInfo();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessageinfotodatabase();
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_info_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.group_info_option:
            {
                display_info();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void display_info() {
        final ProgressDialog lb = new ProgressDialog(this);
        lb.setTitle("Please Wait");
        lb.setCanceledOnTouchOutside(false);
        lb.show();

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(GroupChatActivity.this, android.R.layout.simple_list_item_1);
        group_info_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()){
                    String name = snap.getKey();
                    usersref.child(name).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String naam = dataSnapshot.child("name").getValue().toString();
                            String id = dataSnapshot.getKey();
                            if(id.equalsIgnoreCase(currentuserid)){
                                arrayAdapter.add("You");
                            }
                            else {
                                arrayAdapter.add(naam);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }}
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                lb.dismiss();
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(GroupChatActivity.this);
                builderSingle.setTitle("This Group has: ");
                builderSingle.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                builderSingle.show();
            }
        },3000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        groupnameref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()){
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatdate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatmessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatname = (String) ((DataSnapshot)iterator.next()).getValue();
            String chattime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatname + ":\n" + chatmessage + "\n" + chattime + "   " + chatdate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void sendmessageinfotodatabase() {
        String message = userMessageInput.getText().toString().trim();
        String messagekey = groupnameref.push().getKey();
        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"enter message", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat currentDataFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentdate = currentDataFormat.format(calfordate.getTime());

            Calendar calfortime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currenttime = currentTimeFormat.format(calfortime.getTime());

            HashMap<String, Object> groupmessagekey = new HashMap<>();
            groupnameref.updateChildren(groupmessagekey);

            groupmessagekeyref = groupnameref.child(messagekey);
            HashMap<String, Object> messageinfomap = new HashMap<>();
            messageinfomap.put("name", currentusername);
            messageinfomap.put("message", message);
            messageinfomap.put("date", currentdate);
            messageinfomap.put("time", currenttime);
            groupmessagekeyref.updateChildren(messageinfomap);

        }
    }

    private void GetUserInfo() {
        usersref.child(currentuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentusername = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitialiseFields() {
        mtoolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentgroupname);

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.my_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
    }
}
