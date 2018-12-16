package com.ys.nearbypaint.utiles

import com.google.android.gms.nearby.messages.Message
import com.google.gson.GsonBuilder
import com.ys.nearbypaint.presentation.view.view.PaintData
import java.nio.charset.Charset

class GsonUtil {

    companion object {
        private val gson = GsonBuilder().create()

        fun newMessage(paintData: PaintData): Message {
            return Message(gson.toJson(paintData).toString().toByteArray(Charset.forName("UTF-8")))
        }

        fun fromMessage(message: Message): PaintData {
            val str = String(message.content).trim { it <= ' ' }
            return gson.fromJson<PaintData>(
                String(str.toByteArray(Charset.forName("UTF-8"))),
                PaintData::class.java
            )
        }
    }

}