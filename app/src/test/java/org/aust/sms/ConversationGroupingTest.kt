package org.aust.sms
import org.aust.sms.data.*
import org.junit.Assert.assertEquals
import org.junit.Test
class ConversationGroupingTest{@Test fun groupsMessagesByThread(){val m=listOf(SmsMessage(1,10,"111","A",3000,1,true),SmsMessage(2,10,"111","B",2000,2,true),SmsMessage(3,11,"222","C",1000,1,true));val g=ConversationGrouping.group(m);assertEquals(2,g.size);assertEquals(2,g.first{it.threadId==10L}.messages.size)}@Test fun countsUnread(){val m=listOf(SmsMessage(1,10,"111","A",3000,1,false),SmsMessage(2,10,"111","B",2000,1,false),SmsMessage(3,10,"111","C",1000,1,true));assertEquals(2,ConversationGrouping.group(m).single().unreadCount)}@Test fun newestThreadFirst(){val m=listOf(SmsMessage(1,10,"111","A",1000,1,true),SmsMessage(2,11,"222","B",3000,1,true),SmsMessage(3,10,"111","C",2000,1,true));assertEquals(11L,ConversationGrouping.group(m).first().threadId)}}
