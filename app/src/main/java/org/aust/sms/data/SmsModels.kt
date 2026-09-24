package org.aust.sms.data
data class SmsMessage(val id:Long,val threadId:Long,val address:String,val body:String,val date:Long,val type:Int,val read:Boolean)
data class Conversation(val threadId:Long,val address:String,val title:String,val messages:List<SmsMessage>,val unreadCount:Int){val latest:SmsMessage get()=messages.maxBy{it.date}}
object ConversationGrouping { fun group(messages:List<SmsMessage>,contactNames:Map<String,String> = emptyMap()):List<Conversation> = messages.groupBy{it.threadId}.values.map{g->val o=g.sortedByDescending{it.date}; val a=o.first().address; Conversation(o.first().threadId,a,contactNames[a]?:a,o,o.count{!it.read})}.sortedByDescending{it.latest.date} }
