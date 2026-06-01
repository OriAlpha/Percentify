package com.example

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM trackers ORDER BY timestamp DESC")
    fun getAllTrackers(): Flow<List<Tracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracker(tracker: Tracker)

    @Update
    suspend fun updateTracker(tracker: Tracker)

    @Delete
    suspend fun deleteTracker(tracker: Tracker)

    @Query("SELECT * FROM trackers WHERE id = :id")
    suspend fun getTrackerById(id: Int): Tracker?
}
