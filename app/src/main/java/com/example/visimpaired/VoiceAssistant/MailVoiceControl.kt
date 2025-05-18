package com.example.visimpaired.VoiceAssistant

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.example.visimpaired.MainActivity
import com.example.visimpaired.TTSConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.util.Properties
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeMultipart

class MailVoiceControl(private val context : Context, activity: Activity) {

    private val shard: SharedPreferences = (activity as MainActivity).getPreferences(Context.MODE_PRIVATE)
    private lateinit var folder : Folder
    private var indexMessage: Int = 0

    init {
        val props = Properties()
        props["mail.store.protocol"] = "imaps"
        props["mail.imap.host"] = "imap.mail.ru"
        props["mail.imap.port"] = "993"
        props["mail.imap.ssl.enable"] = "true"
        props["mail.user"]
        store = Session.getInstance(props, null).store
    }

    companion object {
        lateinit var store : Store
    }

    fun routeCommand(command : String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!store.isConnected) {
                val login = shard.getString("login", "")
                val password = shard.getString("password", "")
                if (login == "" || password == "") {
                    TTSConfig.getInstance(context).speak("Введите данные в режиме Почта")
                } else {
                    store.connect("imap.mail.ru", login, password)
                    if (!store.isConnected) {
                        TTSConfig.getInstance(context).speak("Неправильные данные для входа")
                    } else {
                        val rootFolder = store.defaultFolder
                        folder = rootFolder.list()[0]
                        folder.open(Folder.READ_ONLY)
                    }
                }
            }
            if (store.isConnected) {
                when {
                    command.contains("последн") && command.contains("письм") -> lastMessage()
                    command.contains("след") && command.contains("письм") -> nextMessage()
                    command.contains("пред") && command.contains("письм") -> prevMessage()
                    command.contains("перв") && command.contains("письм") -> firstMessage()
                }
            }
        }
    }

    fun lastMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            indexMessage = folder.messageCount
            if (indexMessage != 0) {
                val message = folder.getMessage(indexMessage)
                readMessage(message)
            } else {
                TTSConfig.getInstance(context).speak("У вас нет писем")
            }
        }
    }

    fun firstMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            indexMessage = 1
            if (folder.messageCount != 0) {
                val message = folder.getMessage(indexMessage)
                readMessage(message)
            } else {
                TTSConfig.getInstance(context).speak("У вас нет писем")
            }
        }
    }

    fun nextMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            indexMessage -= 1
            if (indexMessage > 0) {
                val message = folder.getMessage(indexMessage)
                readMessage(message)
            } else {
                indexMessage += 1
                TTSConfig.getInstance(context).speak("Вы уже на первом письме")
            }
        }
    }

    fun prevMessage() {
        CoroutineScope(Dispatchers.IO).launch {
            indexMessage += 1
            if (indexMessage <= folder.messageCount ) {
                val message = folder.getMessage(indexMessage)
                readMessage(message)
            } else {
                indexMessage -= 1
                TTSConfig.getInstance(context).speak("Вы уже на последнем письме")
            }
        }
    }

    fun readMessage(message : Message) {
        var text : String = try {
            if (message.content is String) {
                (message.content as String)
            } else {
                ((message.content as MimeMultipart).getBodyPart(0).content as String)
            }
        } catch (_ : Exception) {
            (((message.content as MimeMultipart).getBodyPart(0).content as MimeMultipart).getBodyPart(0).content as String)
        }
        if (containsHtml(text)) {
            text = Jsoup.parse(text).text()
        }
        println(text)
        TTSConfig.getInstance(context).speak(text)
    }

    private fun containsHtml(text: String): Boolean {
        val htmlRegex = Regex("<([a-z][a-z0-9]*)\\b[^>]*>(.*?)</\\1>|<[a-z][a-z0-9]*\\b[^>]*/>", RegexOption.IGNORE_CASE)
        return htmlRegex.containsMatchIn(text)
    }
}