package com.gaurav.chat_app;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.gaurav.chat_app.Adapters.Group_add_participants_adapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Add_paticipants extends AppCompatActivity {

    String groupname;
    private Toolbar mtoolbar;
    private RecyclerView add_participants_recyclerview;
    private FirebaseAuth mAuth;
    private String currentuserid;
    private DatabaseReference groupref, chatsref, usersref;
    private ProgressDialog loadingbar;

    private List<Contacts> members;
    private List<String> userids;
    private Group_add_participants_adapter madapter;
    private Button addbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_paticipants);

        addbtn = findViewById(R.id.add_participants_button);
        add_participants_recyclerview = findViewById(R.id.add_participants_recycler_view);
        add_participants_recyclerview.setHasFixedSize(true);
        add_participants_recyclerview.setLayoutManager(new LinearLayoutManager(this));

        groupname = getIntent().getStringExtra("groupname");
        mtoolbar = findViewById(R.id.add_participants_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add participants");

        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();
        chatsref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");
        groupref = FirebaseDatabase.getInstance().getReference("Group").child(groupname);

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        userids = new ArrayList<>();
                        List<Contacts> data = madapter.getmemberslist();
                        for (int i = 0; i < data.size(); i++) {
                            Contacts c = data.get(i);
                            if (c.getSelected() == true) {
                                userids.add(c.getUserid());
                            }
                        }
                        create_grp();
                    }
                }, 7000);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadingbar = new ProgressDialog(this);
        loadingbar.setTitle("Loading");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();
        members = new ArrayList<>();

        chatsref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    final String userIDS = snap.getKey();
                    loadingbar.dismiss();
                    if (userIDS != null) {
                        usersref.child(userIDS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String retname = dataSnapshot.child("name").getValue().toString();
                                    Contacts contacts = new Contacts();
                                    contacts.setName(retname);
                                    contacts.setUserid(userIDS);
                                    members.add(contacts);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                madapter = new Group_add_participants_adapter(Add_paticipants.this, members);
                add_participants_recyclerview.setAdapter(madapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    private void create_grp() {
        DatabaseReference rootref = FirebaseDatabase.getInstance().getReference();
        rootref.child("Group").child(groupname).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Add_paticipants.this, groupname + "is created.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentuserid = mAuth.getCurrentUser().getUid();
        rootref.child("Group").child(groupname).child("participants").child(currentuserid)
                .child("Access").setValue("Granted");

        for (int i = 0; i < userids.size(); i++) {
            rootref.child("Group").child(groupname).child("participants").child(userids.get(i))
                    .child("Access").setValue("Granted");
        }
        rootref.child("Group").child(groupname).child("messages").setValue("");
        finish();
    }
}
