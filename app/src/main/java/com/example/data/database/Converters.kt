package com.example.data.database

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromIntList(list: List<Int>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun toIntList(data: String): List<Int> {
        if (data.trim().isEmpty()) return emptyList()
        return data.split(",").mapNotNull { it.toIntOrNull() }
    }

    @TypeConverter
    fun fromMap(map: Map<Int, Int>): String {
        return map.entries.joinToString("|") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toMap(data: String): Map<Int, Int> {
        if (data.trim().isEmpty()) return emptyMap()
        return data.split("|").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                val key = parts[0].toIntOrNull()
                val value = parts[1].toIntOrNull()
                if (key != null && value != null) {
                    key to value
                } else null
            } else null
        }.toMap()
    }
}
