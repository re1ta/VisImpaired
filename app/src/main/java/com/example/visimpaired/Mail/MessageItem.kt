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

class MessageItem (context: Context, name: String?, private val messageItem: Message): Item(name), LifecycleItem {

    private var senders : String = ""

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
            parseBytesToText(message.content, getBoundary(messageItem))
        } catch (e : Exception) {
            e.printStackTrace()
            "Не удалось загрузить письмо"
        }
    }

    private fun parseBytesToText(bytesInput: Any, contentType: String) : String {
        var contentTypeBody = contentType
        var inputStream = bytesInput as InputStream
        var multipartBody : BodyPart? = null
        if (contentTypeBody.contains("multipart/")){
            if (contentTypeBody.contains("mixed")) {
                multipartBody = MimeMultipart(ByteArrayDataSource(bytesInput, contentType)).getBodyPart(0)
                multipartBody = MimeMultipart(ByteArrayDataSource(multipartBody.inputStream, multipartBody.contentType)).getBodyPart(0)
                inputStream = multipartBody.inputStream as InputStream
                contentTypeBody = multipartBody.contentType.lowercase()
            }
            if (contentTypeBody.contains("related")) {
                multipartBody = MimeMultipart(ByteArrayDataSource(multipartBody?.inputStream?:inputStream, multipartBody?.contentType?:contentTypeBody)).getBodyPart(0)
                contentTypeBody = multipartBody.contentType.lowercase()
                inputStream = multipartBody.inputStream as InputStream
            }
            if (contentTypeBody.contains("alternative")) {
                val multipart = MimeMultipart(ByteArrayDataSource(inputStream, contentTypeBody))
                for (i in 0..<multipart.count) {
                    val bodyPart = multipart.getBodyPart(i)
                    if (bodyPart.content != null) {
                        contentTypeBody = bodyPart.contentType.toString().lowercase()
                        if (contentTypeBody.contains("text/plain")) {
                            return bytesArrayToPlainText(bodyPart.content)
                        } else if (contentTypeBody.contains("text/html")) {
                            return bytesArrayToHtmlText(bodyPart.content)
                        }
                    }
                }
            }
        }
        if (contentTypeBody.contains("text/html")) {
            return bytesArrayToHtmlText(inputStream)
        }
        return "Ошибка вывода содержимого письма!"
    }

    //message.getContentType() иногда работает неисправно и отдаёт пустое boundary=""
    //Для этого нужен метод getBoundary(), который 100% достаёт не пустое boundary
    private fun getBoundary(message: Message) : String{
        val headers = message.allHeaders
        for (header in headers) {
            if (header.name.lowercase() == "content-type") return header.value
        }
        return "Не удалось загрузить письмо"
    }

    private fun bytesArrayToPlainText(bytesInput: Any) : String {
        return parseInputStream(bytesInput as InputStream).toString().trim().replace("\\s+".toRegex(), " ")
    }

    private fun bytesArrayToHtmlText(bytesInput: Any) : String{
        val parsedBody = parseInputStream(bytesInput as InputStream)
        val htmlText = Jsoup.parse(parsedBody.toString()).text()
        return htmlText.trim().replace("\\s+".toRegex(), " ")
    }

    private fun parseInputStream(content: InputStream) : ByteArrayOutputStream {
        val out = ByteArrayOutputStream()
        var c: Int
        while ((content.read().also { c = it }) != -1) {
            out.write(c)
        }
        return out
    }



    override fun loadItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val mail = LinkedHashMap<String, Item>()
            mail["Отправитель"] = Item(senders)
            mail["Тема"] = Item(messageItem.subject?:"Нет темы")
            mail["Содержание письма"] = Item(parseMailMessage(messageItem))
        }
    }
}