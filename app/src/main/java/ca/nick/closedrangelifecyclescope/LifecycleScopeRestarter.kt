package ca.nick.closedrangelifecyclescope

import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Lifecycle.restartingLaunch(
    jobsLifetime: ClosedRange<Lifecycle.Event> = Lifecycle.Event.ON_START..Lifecycle.Event.ON_STOP,
    unsubscribeOn: Lifecycle.Event = Lifecycle.Event.ON_DESTROY,
    block: suspend CoroutineScope.() -> Unit
) {
    LifecycleScopeRestarter(this, jobsLifetime, unsubscribeOn, block)
}

private class LifecycleScopeRestarter(
    private val lifecycle: Lifecycle,
    private val jobsLifetime: ClosedRange<Lifecycle.Event>,
    private val unsubscribeOn: Lifecycle.Event,
    private val block: suspend CoroutineScope.() -> Unit
) : LifecycleEventObserver {

    private val jobs = mutableListOf<Job>()

    init {
        lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (jobsLifetime.start == event) {
            jobs += lifecycle.coroutineScope.launch { block() }
        } else if (jobsLifetime.endInclusive == event) {
            jobs.forEach { it.cancel() }
            jobs.clear()
        }
        if (unsubscribeOn == event) {
            lifecycle.removeObserver(this)
        }
    }
}