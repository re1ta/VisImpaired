package com.example.visimpaired.Mail

import android.content.Context
import com.example.visimpaired.Interfaces.LifecycleItem
import com.example.visimpaired.Menu.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.mail.BodyPart
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

class MessageItem (private val context: Context, name: String?, private val messageItem: Message): Item(context, "Выберите, что прослушать в письме"), LifecycleItem {

    private var senders : String = ""
    private var mail : LinkedHashMap<String, Item> = linkedMapOf("Подождите, Пожалуйста" to Item(context,"Подождите, Пожалуйста"))

    init {
        val sendersFromMessage = messageItem.from
        if (sendersFromMessage != null) {
            sendersFromMessage.forEach { address ->
                val email = (address as InternetAddress).address
                val name = address.personal ?: "Без имени"
                senders += "$name, $email"
            }
        } else {
            senders = "Отправители не найдены"
        }
    }
    private fun parseMailMessage(message: Message): String {
        return try {
            ((message.content as MimeMultipart).getBodyPart(0).content as String)
        } catch (e : Exception) {
            e.printStackTrace()
            return (((message.content as MimeMultipart).getBodyPart(0).content as MimeMultipart).getBodyPart(0).content as String)
        }
    }

    override fun loadItems() {
        items = mail
        CoroutineScope(Dispatchers.IO).launch {
            val mail = LinkedHashMap<String, Item>()
            mail["Отправитель"] = Item(context,"Отправитель", senders)
            mail["Тема"] = Item(context,"Тема", messageItem.subject?:"Нет темы")
            mail["Содержание письма"] = Item(context,"Содержание письма", parseMailMessage(messageItem))
            items = mail
            if (items.containsKey("Подождите, Пожалуйста")) items.remove("Подождите, Пожалуйста")
        }
    }
}