package com.example.cunyclassroomfinder.data

class DatabaseRepoRoom(private val courseDao: CourseDao) : DatabaseRepository {
    override suspend fun insertCourse(course: Course) = courseDao.insertCourse(course)

    override suspend fun insertRoom(room: CunyRoom) = courseDao.insertRoom(room)

    override suspend fun getAvailableRooms(day: Int, timeStart: Int, timeEnd: Int): List<CunyRoom> =
        courseDao.getAvailableRooms(day, timeStart, timeEnd)
}