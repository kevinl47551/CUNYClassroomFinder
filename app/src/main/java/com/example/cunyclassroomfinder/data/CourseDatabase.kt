package com.example.cunyclassroomfinder.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Course::class, CunyRoom::class], version = 2, exportSchema = false)
abstract class CourseDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    companion object {
        @Volatile
        private var Instance: CourseDatabase? = null

        fun getDatabase(context: Context): CourseDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CourseDatabase::class.java, "course_database")
                    // Data is re-scrapable, so drop and recreate tables on schema changes
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { Instance = it }
            }
        }
    }
}