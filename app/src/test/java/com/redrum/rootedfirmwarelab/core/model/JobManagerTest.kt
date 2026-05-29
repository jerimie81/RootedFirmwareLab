package com.redrum.rootedfirmwarelab.core.model

import com.redrum.rootedfirmwarelab.core.service.JobManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JobManagerTest {
    @Test
    fun lifecycleTransitionsUpdateTrackedJob() {
        val manager = JobManager()
        val jobId = manager.registerJob("flash")

        assertEquals(JobStatus.PENDING, manager.getJobState(jobId)?.status)

        manager.updateProgress(jobId, JobProgress(50, "Halfway"))
        assertEquals(50, manager.getJobState(jobId)?.progress?.percentage)

        manager.updateStatus(jobId, JobStatus.SUCCESS)
        val state = manager.getJobState(jobId)

        assertEquals(JobStatus.SUCCESS, state?.status)
        assertNotNull(state?.endTimeMs)
        assertTrue((state?.endTimeMs ?: 0L) >= (state?.startTimeMs ?: 0L))
    }
}
