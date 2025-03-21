package com.example.visimpaired.Mail

import android.content.Context
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.mail.Folder

class FolderMailMode(private val context: Context, name: String?, private val folder: Folder): Item(context, name), LifecycleItem {
    private var page = 0
    private var borderReached = false
    private val PAGE_SIZE = 20
    private var mails : LinkedHashMap<String, Item> = linkedMapOf("Подождите, Пожалуйста" to Item(context,"Подождите, Пожалуйста"))


    fun loadMails(direction: String) {
         CoroutineScope(Dispatchers.IO).launch {
             val totalMessages = folder.messageCount
             if (totalMessages == 0) return@launch
             page = when (direction) {
                 "back" -> page - 1
                 "forward" -> page + 1
                 else -> { 0 }
             }
             println(page)
             val (start, end) = getMessageRange(page, totalMessages, PAGE_SIZE)
             val messages = if (PAGE_SIZE < totalMessages) {
                 folder.getMessages(start, end)
             } else {
                 folder.getMessages()
             }
             mails.clear()
             if (page != 1) mails["Страница " + (page - 1)] = NextPageMail(context, "back")
             for (message in messages.reversed()) {
                 mails[message.subject ?: "Нет темы"] = MessageItem(context, message.subject ?: "Нет темы", message)
             }
             if ((page * PAGE_SIZE) < totalMessages) mails["Страница " + (page + 1)] = NextPageMail(context, "forward")
             if (mails.containsKey("Подождите, Пожалуйста")) mails.remove("Подождите, Пожалуйста")
             items = mails
         }
    }

    private fun getMessageRange(page: Int, totalMessages: Int, pageSize: Int): Pair<Int, Int> {
        val start = maxOf(1, totalMessages - (page * pageSize))
        val end = totalMessages - ((page - 1) * pageSize)
        return start to end
    }

    override fun onEnter() {
        items = mails
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.IO) {
                    folder.open(Folder.READ_ONLY)
                    loadMails("forward")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onEnter()
    }

    override fun onEscape() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                folder.close(false);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        page = 1
        borderReached = false
        super.onEscape()
    }


    override fun loadItems() {}
}
