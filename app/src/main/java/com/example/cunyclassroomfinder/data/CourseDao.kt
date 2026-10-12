package com.example.cunyclassroomfinder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoom(cunyRoom: CunyRoom)

    @Query("""
        SELECT r.*
        FROM rooms r
        WHERE NOT EXISTS (
            SELECT 1
            FROM courses c
            WHERE c.room = r.room
            AND c.days & :day > 0
            AND c.timeStart < :timeEnd
            AND c.timeEnd > :timeStart
        )
    """)
    suspend fun getAvailableRooms(
        day: Int,
        timeStart: Int,
        timeEnd: Int
    ): List<CunyRoom>
}