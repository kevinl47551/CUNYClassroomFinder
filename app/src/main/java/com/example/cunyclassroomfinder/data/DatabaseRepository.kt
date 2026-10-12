package com.example.cunyclassroomfinder.data

interface DatabaseRepository {
    suspend fun insertCourse(course: Course)

    suspend fun insertCourses(courses: List<Course>)

    suspend fun insertRoom(room: CunyRoom)

    suspend fun insertRooms(rooms: List<CunyRoom>)

    suspend fun insertCoursesWRooms(courses: List<Course>)

    suspend fun getAvailableRooms(day: Int, timeStart: Int, timeEnd: Int): List<CunyRoom>

    suspend fun clearRooms()

    suspend fun clearCourses()
}