package com.gaurav.chat_app.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gaurav.chat_app.Contacts;
import com.gaurav.chat_app.R;

import java.util.List;

public class Group_add_participants_adapter extends RecyclerView.Adapter<Group_add_participants_adapter.viewholder> {

    private Context mCtx;
    private List<Contacts> members;


    public Group_add_participants_adapter(Context mCtx, List<Contacts> members) {
        this.mCtx = mCtx;
        this.members = members;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.add_participants_user, viewGroup, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, final int position) {
        Contacts contacts = members.get(position);
        holder.checkBox.setSelected(members.get(position).getSelected());
        holder.checkBox.setTag(members.get(position));

        holder.username.setText(contacts.getName());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                Contacts contacts1 = (Contacts) cb.getTag();

                contacts1.setSelected(cb.isChecked());
                members.get(position).setSelected(cb.isChecked());


            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public List<Contacts> getmemberslist() {
        return members;
    }

    public class viewholder extends RecyclerView.ViewHolder {

        private TextView username;
        private CheckBox checkBox;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.user_profile_name);
            checkBox = itemView.findViewById(R.id.add_participants_checkbox);
        }
    }

}
