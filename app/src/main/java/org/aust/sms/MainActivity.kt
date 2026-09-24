package org.aust.sms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aust.sms.data.Conversation
import org.aust.sms.data.ConversationGrouping
import org.aust.sms.data.SmsMessage
import org.aust.sms.data.SmsRepository

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_SMS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_CONTACTS
                )
            )
        }

        setContent {
            MaterialTheme {
                KydoSmsApp()
            }
        }
    }
}

class SmsViewModel(private val repo: SmsRepository) : ViewModel() {
    var messages by mutableStateOf<List<SmsMessage>>(emptyList())
        private set

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            messages = repo.loadMessages()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KydoSmsApp() {
    val context = LocalContext.current
    val vm: SmsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SmsViewModel(SmsRepository(context.contentResolver)) as T
            }
        }
    )

    var search by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Conversation?>(null) }
    var newMessage by remember { mutableStateOf(false) }

    if (selected != null) {
        ConversationScreen(selected!!, onBack = { selected = null })
        return
    }

    if (newMessage) {
        NewMessageScreen(onBack = { newMessage = false })
        return
    }

    val conversations = remember(vm.messages, search) {
        ConversationGrouping.group(vm.messages).filter {
            search.isBlank() ||
                it.title.contains(search, ignoreCase = true) ||
                it.address.contains(search, ignoreCase = true) ||
                it.latest.body.contains(search, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KydoSMS") },
                actions = {
                    IconButton(onClick = { newMessage = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New message")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Search conversations") }
            )

            LazyColumn(Modifier.fillMaxSize()) {
                items(conversations, key = { it.threadId }) { conversation ->
                    ListItem(
                        modifier = Modifier.clickable { selected = conversation },
                        headlineContent = {
                            Text(conversation.title)
                        },
                        supportingContent = {
                            Text(conversation.latest.body, maxLines = 1)
                        },
                        trailingContent = {
                            if (conversation.unreadCount > 0) {
                                Badge {
                                    Text(conversation.unreadCount.toString())
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationScreen(c: Conversation, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(c.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(c.messages.reversed(), key = { it.id }) { message ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 2.dp
                ) {
                    Text(message.body, Modifier.padding(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewMessageScreen(onBack: () -> Unit) {
    var number by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New message") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Sending will be added in the SMS provider layer. */ },
                        enabled = number.isNotBlank() && body.isNotBlank()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = number,
                onValueChange = { number = it },
                label = { Text("Recipient") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}
