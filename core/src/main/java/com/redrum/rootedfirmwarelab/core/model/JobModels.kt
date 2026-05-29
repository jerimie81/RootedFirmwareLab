package com.redrum.rootedfirmwarelab.core.model

import java.util.UUID

enum class JobStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED,
}

data class JobProgress(
    val percentage: Int,
    val message: String,
)

data class JobState(
    val id: String = UUID.randomUUID().toString(),
    val actionId: String,
    val status: JobStatus = JobStatus.PENDING,
    val progress: JobProgress = JobProgress(0, "Pending"),
    val error: String? = null,
    val startTimeMs: Long = System.currentTimeMillis(),
    val endTimeMs: Long? = null,
)

interface JobTracker {
    fun updateProgress(jobId: String, progress: JobProgress)
    fun updateStatus(jobId: String, status: JobStatus, error: String? = null)
    fun getJobState(jobId: String): JobState?
}
