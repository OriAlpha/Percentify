package com.example

import kotlinx.coroutines.flow.Flow

class TrackerRepository(private val trackerDao: TrackerDao) {
    val allTrackers: Flow<List<Tracker>> = trackerDao.getAllTrackers()

    suspend fun insert(tracker: Tracker) {
        trackerDao.insertTracker(tracker)
    }

    suspend fun update(tracker: Tracker) {
        trackerDao.updateTracker(tracker)
    }

    suspend fun delete(tracker: Tracker) {
        trackerDao.deleteTracker(tracker)
    }

    suspend fun getById(id: Int): Tracker? {
        return trackerDao.getTrackerById(id)
    }
}
