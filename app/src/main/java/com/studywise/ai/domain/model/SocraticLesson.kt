package com.studywise.ai.domain.model

data class SocraticLesson(
    val id: String,
    val objectName: String,
    val grade: Int,
    val duration: Int, // minutes
    val learningObjectives: List<String>,
    val initialQuestion: String,
    val guidingQuestions: List<String>,
    val vocabularyWords: List<VocabularyWord>,
    val funFacts: List<String>
)

data class VocabularyWord(
    val word: String,
    val definition: String,
    val exampleSentence: String,
    val gradeLevel: Int
)