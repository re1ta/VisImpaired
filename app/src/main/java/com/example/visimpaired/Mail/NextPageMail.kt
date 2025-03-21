package com.example.visimpaired.Mail

import android.content.Context
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item

class NextPageMail(context: Context, name : String) : Item(name), LifecycleItem {

    override fun loadItems() {
        (parent as FolderMailMode).loadMails(name)
    }
}