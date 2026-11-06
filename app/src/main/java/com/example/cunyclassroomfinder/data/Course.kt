package com.example.cunyclassroomfinder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "courses",
    primaryKeys = ["school", "room", "days", "timeStart", "timeEnd"],
    indices = [Index(value = ["school"]), Index(value = ["school", "room"])],
    foreignKeys = [ForeignKey(
        entity = CunyRoom::class,
        parentColumns = ["school", "room"],
        childColumns = ["school", "room"]
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
    primaryKeys = ["school", "room"],
    indices = [Index(value = ["school"])],
)
data class CunyRoom(
    val room: String,
    val school: String
)