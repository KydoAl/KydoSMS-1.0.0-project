package org.aust.sms.sms
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
class SmsReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent){if(intent.action!=Telephony.Sms.Intents.SMS_DELIVER_ACTION)return}}
