package com.example.cunyclassroomfinder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    primaryKeys = ["days", "timeStart", "timeEnd", "room"],
    indices = [Index(value = ["room"])],
    foreignKeys = [ForeignKey(
        entity = CunyRoom::class,
        parentColumns = ["room"],
        childColumns = ["room"]
    )]
)
data class Course(
    val classId: Int,
    val days: Int,
    val timeStart: Int,
    val timeEnd: Int,
    val room: String,
    val courseTopic: String
)


@Entity(
    tableName = "rooms"
)
data class CunyRoom(
    @PrimaryKey
    val room: String
)