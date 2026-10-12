package com.example.cunyclassroomfinder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRoom(cunyRoom: CunyRoom)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRooms(cunyRooms: List<CunyRoom>)

    @Transaction
    suspend fun insertCoursesWRooms(courses: List<Course>) {
        val rooms = courses
            .map { CunyRoom(room = it.room) }
            .distinctBy { it.room }
        insertRooms(rooms)
        insertCourses(courses)
    }

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

    @Query("""
        DELETE 
        FROM rooms
    """)
    suspend fun clearRooms()

    @Query("""
        DELETE 
        FROM courses
    """)
    suspend fun clearCourses()
}