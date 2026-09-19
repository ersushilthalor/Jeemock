package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Difficulty
import com.example.data.model.ExamPattern
import com.example.data.model.QuestionType
import com.example.data.model.Subject
import com.example.data.model.TestSource

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return ""
        return value.joinToString("||_DELIM_||")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("||_DELIM_||")
    }

    @TypeConverter
    fun fromSubject(subject: Subject): String = subject.name

    @TypeConverter
    fun toSubject(value: String): Subject = try {
        Subject.valueOf(value)
    } catch (e: Exception) {
        Subject.PHYSICS
    }

    @TypeConverter
    fun fromQuestionType(type: QuestionType): String = type.name

    @TypeConverter
    fun toQuestionType(value: String): QuestionType = try {
        QuestionType.valueOf(value)
    } catch (e: Exception) {
        QuestionType.MCQ
    }

    @TypeConverter
    fun fromExamPattern(pattern: ExamPattern): String = pattern.name

    @TypeConverter
    fun toExamPattern(value: String): ExamPattern = try {
        ExamPattern.valueOf(value)
    } catch (e: Exception) {
        ExamPattern.JEE_MAIN
    }

    @TypeConverter
    fun fromDifficulty(diff: Difficulty): String = diff.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = try {
        Difficulty.valueOf(value)
    } catch (e: Exception) {
        Difficulty.MEDIUM
    }

    @TypeConverter
    fun fromTestSource(source: TestSource): String = source.name

    @TypeConverter
    fun toTestSource(value: String): TestSource = try {
        TestSource.valueOf(value)
    } catch (e: Exception) {
        TestSource.GENUINE_PYQ
    }
}
