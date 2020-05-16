package com.gaurav.chat_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privatechatview;
    private RecyclerView chatList;
    private DatabaseReference chatsref, usersref;
    private FirebaseAuth mAuth;
    private String currentuserid;
    private ProgressDialog loadingbar;

    public ChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privatechatview =  inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentuserid = mAuth.getCurrentUser().getUid();

        chatsref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentuserid);
        usersref = FirebaseDatabase.getInstance().getReference().child("Users");

        chatList = (RecyclerView) privatechatview.findViewById(R.id.Chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privatechatview;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadingbar = new ProgressDialog(getContext());
        loadingbar.setTitle("Loading");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsref, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String userIDS = getRef(position).getKey();
                        final String[] retImage = {"default_image"};
                        loadingbar.dismiss();
                        usersref.child(userIDS).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if(dataSnapshot.hasChild("image")){
                                        retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image)
                                                .into(holder.profileimage);
                                    }

                                    final String retname = dataSnapshot.child("name").getValue().toString();
                                    final String retstatus = dataSnapshot.child("status").getValue().toString();
                                    holder.username.setText(retname);
                                    //holder.userstatus.setText("Last seen : ");
                                    loadingbar.dismiss();

                                    if(dataSnapshot.child("userState").hasChild("state")){
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();

                                        if(state.equals("online")){
                                            holder.userstatus.setText("online");
                                        }
                                        else if(state.equals("offline")){
                                            holder.userstatus.setText("Last seen : " + date + " " + time);
                                        }
                                    }
                                    else {
                                        holder.userstatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatintent = new Intent(getContext(), ChatActivity.class);
                                            chatintent.putExtra("visit_user_id", userIDS);
                                            chatintent.putExtra("visit_user_name", retname);
                                            chatintent.putExtra("visit_image", retImage[0]);
                                            startActivity(chatintent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };

        chatList.setAdapter(adapter);
        adapter.startListening();
        loadingbar.dismiss();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileimage;
        TextView username, userstatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.user_profile_name);
            userstatus = (TextView) itemView.findViewById(R.id.user_status);
            profileimage = (CircleImageView) itemView.findViewById(R.id.user_profile_image);
        }
    }
}
