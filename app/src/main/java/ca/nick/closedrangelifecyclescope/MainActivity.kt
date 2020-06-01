package ca.nick.closedrangelifecyclescope

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    private val networkState by lazy { NetworkState(applicationContext) }

    init {
        /**
         * Uncomment the code below to see the BroadcastReceiver in NetworkState.kt continue to fire
         * even when this Activity has stopped.
         */
//        lifecycleScope.launchWhenStarted {
//            networkState.changes()
//                .onEach { isConnected -> updateUi(isConnected) }
//                .launchIn(this)
//        }

        lifecycle.launchRestartable {
            networkState.changes()
                .onEach { isConnected -> updateUi(isConnected) }
                .launchIn(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun updateUi(isConnected: Boolean) {
        val backgroundColor = if (isConnected) {
            Color.GREEN
        } else {
            Color.RED
        }
        container.setBackgroundColor(backgroundColor)
        connectedState.text = "connected? $isConnected"
    }
}
