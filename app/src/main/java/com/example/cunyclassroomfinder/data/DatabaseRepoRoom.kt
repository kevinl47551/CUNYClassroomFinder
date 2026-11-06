package com.example.cunyclassroomfinder.data

class DatabaseRepoRoom(private val courseDao: CourseDao) : DatabaseRepository {
    override suspend fun insertCourse(course: Course) = courseDao.insertCourse(course)

    override suspend fun insertCourses(courses: List<Course>) = courseDao.insertCourses(courses)

    override suspend fun insertRoom(room: CunyRoom) = courseDao.insertRoom(room)

    override suspend fun insertRooms(rooms: List<CunyRoom>) = courseDao.insertRooms(rooms)

    override suspend fun insertCoursesWRooms(courses: List<Course>) = courseDao.insertCoursesWRooms(courses)

    override suspend fun getAvailableRooms(school: String, day: Int, timeStart: Int, timeEnd: Int): List<CunyRoom> =
        courseDao.getAvailableRooms(school, day, timeStart, timeEnd)

    override suspend fun getSchools(): List<String> = courseDao.getSchools()

    override suspend fun clearRooms() = courseDao.clearRooms()

    override suspend fun clearCourses(school: String) = courseDao.clearCourses(school)
}