/*
 * MIT License
 *
 * Copyright (c) 2020 Rosetta Roberts
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.thenumberone.discord.rolekickerbot.scheduler

import kotlinx.coroutines.*
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.ReplayProcessor
import java.util.concurrent.atomic.AtomicReference

sealed class JobResult<out T> {
    class Successful<T>(val result: T) : JobResult<T>()
    class Failed(val error: Throwable) : JobResult<Nothing>()
    object Cancelled : JobResult<Nothing>()
}

/**
 * Always runs exactly one job (and its children). If any other jobs are launched, cancels the other jobs. Emits a value
 * every time a job completes through the passed sink.
 */
class SingleJobScheduler<T>(
    dispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val currentJob = AtomicReference<Job>()
    private val scope = CoroutineScope(dispatcher)
    private val sink: FluxSink<JobResult<T>>
    val jobResults: Flux<JobResult<T>>

    init {
        jobResults = ReplayProcessor.cacheLast<JobResult<T>>().also {
            sink = it.sink()
        }
    }

    fun launch(f: suspend () -> T) {
        val newJob = scope.launch(start = CoroutineStart.LAZY) {
            sink.next(
                try {
                    JobResult.Successful(f())
                } catch (e: Throwable) {
                    if (e is CancellationException) {
                        JobResult.Cancelled
                    } else {
                        JobResult.Failed(e)
                    }
                }
            )
        }
        val oldJob = currentJob.getAndSet(newJob)
        oldJob?.cancel()
        newJob.start()
    }
}