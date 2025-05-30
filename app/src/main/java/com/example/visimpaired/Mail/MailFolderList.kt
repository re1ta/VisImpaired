package com.example.visimpaired.Mail

import android.content.Context
import android.content.SharedPreferences
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.MainActivity
import com.example.visimpaired.Menu.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Properties
import javax.mail.Session
import javax.mail.Store

class MailFolderList(name: String?, private val context: Context) : Item(context, "Выберите папку с письмами"), LifecycleItem {

    private var store : Store
    private var folders : LinkedHashMap<String, Item> = linkedMapOf("Подождите, Пожалуйста" to Item(context,"Подождите, Пожалуйста"))
    private val shard: SharedPreferences = (context as MainActivity).activity.getPreferences(Context.MODE_PRIVATE)
    ///DRmbQ8wry0zygciMjxxV

    init {
        val props = Properties()
        props["mail.store.protocol"] = "imaps"
        props["mail.imap.host"] = "imap.mail.ru"
        props["mail.imap.port"] = "993"
        props["mail.imap.ssl.enable"] = "true"
        props["mail.user"]
        store = Session.getInstance(props, null).store
    }

    override fun loadItems() {
        (context as MainActivity).disableKeyboard()
        items = folders
        openMailSession()
    }

    override fun onEscape() {
        super.onEscape()
        CoroutineScope(Dispatchers.IO).launch {
            store.close()
        }
        (context as MainActivity).enableKeyboard()
    }

    private fun openMailSession(){
        CoroutineScope(Dispatchers.IO).launch {
            val login = shard.getString("login", "")
            val password = shard.getString("password", "")
            store.connect("imap.mail.ru", login, password)
            if (store.isConnected) {
                val rootFolder = store.defaultFolder
                val foldersMail = rootFolder.list()
                for (folder in foldersMail) {
                    items[folder.name] = FolderMailMode(context, folder.name, folder, this@MailFolderList)
                }
                items.remove("Подождите, Пожалуйста")
            } else {
                onEscape()
            }
        }
    }

}
