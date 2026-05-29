package com.redrum.rootedfirmwarelab.core.service

import com.redrum.rootedfirmwarelab.core.model.*
import java.util.concurrent.ConcurrentHashMap

class JobManager : JobTracker {
    private val jobs = ConcurrentHashMap<String, JobState>()

    override fun updateProgress(jobId: String, progress: JobProgress) {
        jobs[jobId]?.let {
            jobs[jobId] = it.copy(progress = progress)
        }
    }

    override fun updateStatus(jobId: String, status: JobStatus, error: String?) {
        jobs[jobId]?.let {
            jobs[jobId] = it.copy(
                status = status,
                error = error,
                endTimeMs = if (status == JobStatus.SUCCESS || status == JobStatus.FAILED || status == JobStatus.CANCELLED) 
                    System.currentTimeMillis() else null
            )
        }
    }

    override fun getJobState(jobId: String): JobState? = jobs[jobId]

    fun registerJob(actionId: String): String {
        val job = JobState(actionId = actionId)
        jobs[job.id] = job
        return job.id
    }
    
    // Future: Add executeAction(action, jobTracker) method to manage command execution
}
