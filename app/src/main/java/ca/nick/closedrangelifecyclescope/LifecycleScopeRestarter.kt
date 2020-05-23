package ca.nick.closedrangelifecyclescope

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Lifecycle.restartingLaunch(
    jobsLifetime: ClosedRange<Lifecycle.Event> = Lifecycle.Event.ON_START..Lifecycle.Event.ON_STOP,
    unsubscribeOn: Lifecycle.Event? = Lifecycle.Event.ON_DESTROY,
    block: suspend CoroutineScope.() -> Unit
) {
    LifecycleScopeRestarter(this, jobsLifetime, unsubscribeOn, block)
}

private class LifecycleScopeRestarter(
    private val lifecycle: Lifecycle,
    private val jobsLifetime: ClosedRange<Lifecycle.Event>,
    private val unsubscribeOn: Lifecycle.Event?,
    private val block: suspend CoroutineScope.() -> Unit
) : DefaultLifecycleObserver {

    private val jobs = mutableListOf<Job>()

    init {
        lifecycle.addObserver(this)
    }

    private fun tryStarting(event: Lifecycle.Event) {
        if (jobsLifetime.start == event) {
            jobs += lifecycle.coroutineScope.launch { block() }
        }
    }

    private fun tryEnding(event: Lifecycle.Event) {
        if (jobsLifetime.endInclusive == event) {
            jobs.forEach { it.cancel() }
            jobs.clear()
        }

        if (unsubscribeOn == event) {
            lifecycle.removeObserver(this)
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        tryStarting(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart(owner: LifecycleOwner) {
        tryStarting(Lifecycle.Event.ON_START)
    }

    override fun onResume(owner: LifecycleOwner) {
        tryStarting(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause(owner: LifecycleOwner) {
        tryEnding(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop(owner: LifecycleOwner) {
        tryEnding(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        tryEnding(Lifecycle.Event.ON_DESTROY)
    }
}