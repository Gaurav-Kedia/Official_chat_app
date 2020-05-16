package com.gaurav.chat_app;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch(i){
            case 0:
                ChatsFragment chatsfragments = new ChatsFragment();
                return chatsfragments;
            case 1:
                GroupsFragment GroupsFragments = new GroupsFragment();
                return GroupsFragments;
            case 2:
                ContactsFragment contactsfragments = new ContactsFragment();
                return contactsfragments;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        //return super.getPageTitle(position);
        switch(position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            default:
                return null;

        }
    }
}
