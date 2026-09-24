package org.aust.sms
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import org.aust.sms.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class MainActivity:ComponentActivity(){private val p=registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ };override fun onCreate(b:Bundle?){super.onCreate(b);if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED)p.launch(arrayOf(Manifest.permission.READ_SMS,Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_CONTACTS));setContent{MaterialTheme{KydoSmsApp()}}}}
class SmsViewModel(private val repo:SmsRepository):ViewModel(){var messages by mutableStateOf<List<SmsMessage>>(emptyList());private set;init{reload()};fun reload(){viewModelScope.launch(Dispatchers.IO){messages=repo.loadMessages()}}}
@Composable fun KydoSmsApp(){val c=androidx.compose.ui.platform.LocalContext.current;val vm:SmsViewModel=viewModel(factory=object:ViewModelProvider.Factory{override fun<T:ViewModel>create(k:Class<T>):T=SmsViewModel(SmsRepository(c.contentResolver)) as T});var search by remember{mutableStateOf("")};var selected by remember{mutableStateOf<Conversation?>(null)};var newMessage by remember{mutableStateOf(false)};if(selected!=null){ConversationScreen(selected!!){selected=null};return};if(newMessage){NewMessageScreen{newMessage=false};return};val cs=remember(vm.messages,search){ConversationGrouping.group(vm.messages).filter{search.isBlank()||it.title.contains(search,true)||it.address.contains(search,true)||it.latest.body.contains(search,true)}};Scaffold(topBar={TopAppBar(title={Text("KydoSMS")},actions={IconButton({newMessage=true}){Icon(Icons.Default.Add,"New message")}})}){pad->Column(Modifier.padding(pad)){OutlinedTextField(search,{search=it},Modifier.fillMaxWidth().padding(12.dp),singleLine=true,leadingIcon={Icon(Icons.Default.Search,null)},placeholder={Text("Search conversations")});LazyColumn{items(cs,key={it.threadId}){x->ListItem(Modifier.clickable{selected=x},headlineContent={Text(x.title)},supportingContent={Text(x.latest.body,maxLines=1)},trailingContent={if(x.unreadCount>0)Badge{Text(x.unreadCount.toString())}});HorizontalDivider()}}}}}
@OptIn(ExperimentalMaterial3Api::class)@Composable fun ConversationScreen(c:Conversation,back:()->Unit){Scaffold(topBar={TopAppBar(title={Text(c.title)},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,"Back")}})}){p->LazyColumn(Modifier.padding(p).fillMaxSize(),contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){items(c.messages.reversed(),key={it.id}){m->Surface(Modifier.fillMaxWidth(),shape=MaterialTheme.shapes.large,tonalElevation=2.dp){Text(m.body,Modifier.padding(14.dp))}}}}}
@OptIn(ExperimentalMaterial3Api::class)@Composable fun NewMessageScreen(back:()->Unit){var number by remember{mutableStateOf("")};var body by remember{mutableStateOf("")};Scaffold(topBar={TopAppBar(title={Text("New message")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,"Back")}},actions={IconButton(number.isNotBlank()&&body.isNotBlank(),{}){Icon(Icons.Default.Send,"Send")}})}){p->Column(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){OutlinedTextField(number,{number=it},label={Text("Recipient")},singleLine=true,modifier=Modifier.fillMaxWidth());OutlinedTextField(body,{body=it},label={Text("Message")},modifier=Modifier.fillMaxWidth().weight(1f))}}}
