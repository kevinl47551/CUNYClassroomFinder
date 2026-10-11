package com.example.cunyclassroomfinder.data

interface DatabaseRepository {
    suspend fun insertCourse(course: Course)

    suspend fun insertRoom(room: CunyRoom)

    suspend fun getAvailableRooms(day: Int, timeStart: Int, timeEnd: Int): List<CunyRoom>

}