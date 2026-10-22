package com.example.cunyclassroomfinder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    primaryKeys = ["days", "timeStart", "timeEnd", "room"],
    indices = [Index(value = ["school"])],
    foreignKeys = [ForeignKey(
        entity = CunyRoom::class,
        parentColumns = ["room"],
        childColumns = ["room"]
    )]
)
data class Course(
    val school: String,
    val classId: Int,
    val days: Int,
    val timeStart: Int,
    val timeEnd: Int,
    val room: String,
    val courseTopic: String
)


@Entity(
    tableName = "rooms",
    indices = [Index(value = ["school"])],
)
data class CunyRoom(
    @PrimaryKey
    val room: String,
    val school: String
)