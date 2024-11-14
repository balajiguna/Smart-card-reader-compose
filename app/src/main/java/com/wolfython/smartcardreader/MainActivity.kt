package com.wolfython.smartcardreader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private var onNfcTagRead: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            NFCReaderScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        tag?.let {
            val isoDep = IsoDep.get(tag)
            isoDep?.connect()
            val response = isoDep.transceive(Utils.hexStringToByteArray("00A4040007A0000002471001"))
            val hexResponse = Utils.toHex(response)
            isoDep.close()

            onNfcTagRead?.invoke("Card Response: $hexResponse")
        }
    }

    @Composable
    fun NFCReaderScreen() {
        var cardResponse by remember { mutableStateOf("Ready to Communicate...") }
        var displayTemporaryMessage by remember { mutableStateOf(false) }

        // Capture the response and display it for 2 seconds
        onNfcTagRead = { response ->
            cardResponse = response
            displayTemporaryMessage = true
        }

        LaunchedEffect(displayTemporaryMessage) {
            if (displayTemporaryMessage) {
                delay(2000) // Wait for 2 seconds
                cardResponse = "Ready to Communicate..."
                displayTemporaryMessage = false
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = cardResponse,
                fontSize = 24.sp // Adjust text size as needed
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.hid),
                contentDescription = "NFC Icon",
                modifier = Modifier.size(500.dp)
                //colorFilter = ColorFilter.tint(color = androidx.compose.ui.graphics.Color.White)
            )
        }}

    @Preview
    @Composable
    fun PreviewNFCReaderScreen() {
        NFCReaderScreen()
    }
}
