package com.gaurav.chat_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference rootref;
    private String currentuserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mtoolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("chat app");
        rootref = FirebaseDatabase.getInstance().getReference();

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            updateuserstatus("online");
            verifyuserexistance();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateuserstatus("offline");
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateuserstatus("offline");
        }
        super.onDestroy();
    }

    private void verifyuserexistance() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        rootref.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    //Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    //Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.main_logout_option:
            {
                updateuserstatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                finish();
            }
            break;

            case R.id.main_settings_option:
            {
                //SendUserToSettingsActivity();
                Intent settingsIntent1 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent1);
            }
            break;

            case R.id.main_find_friends_option:
            {
                SendUserToFindFriendActivity();
            }
            break;

            case R.id.main_create_group_option:
            {
                requestnewgroup();
            }
            break;

            default:
            {}
        }
        return true;
    }

    private void SendUserToFindFriendActivity() {
        Intent findfriends = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findfriends);
    }

    private void requestnewgroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter group name");

        final EditText groupnamefield = new EditText(MainActivity.this);
        groupnamefield.setHint("e.g. Covid Group");
        builder.setView(groupnamefield);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupname = groupnamefield.getText().toString();
                if(TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                }
                else{
                    CreateNewGroup(groupname);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupname) {
        /*rootref.child("Group").child(groupname).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupname + "is created.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
        addparticipatens(groupname);
    }

    private void addparticipatens(String groupname) {
        Intent addparticipants = new Intent(MainActivity.this, Add_paticipants.class);
        addparticipants.putExtra("groupname", groupname);
        startActivity(addparticipants);
    }

    private void SendUserToLoginActivity() {
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
    }

    private void updateuserstatus(String state){
        String savecurrenttime, savecurrentdate;
        Calendar calender = Calendar.getInstance();

        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd, yyyy");
        savecurrentdate = currentdate.format(calender.getTime());

        SimpleDateFormat currenttime = new SimpleDateFormat("hh:mm a");
        savecurrenttime = currenttime.format(calender.getTime());

        HashMap<String, Object> onlineStatemap = new HashMap<>();
        onlineStatemap.put("time", savecurrenttime);
        onlineStatemap.put("date", savecurrentdate);
        onlineStatemap.put("state", state);

        currentuserid = mAuth.getCurrentUser().getUid();
        rootref.child("Users").child(currentuserid).child("userState")
                .updateChildren(onlineStatemap);
    }
}
