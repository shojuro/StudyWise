package com.studywise.ai.data.offline

import com.studywise.ai.domain.model.IdentifiedObject
import com.studywise.ai.domain.model.SocraticLesson
import com.studywise.ai.domain.model.VocabularyWord
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFallbackProvider @Inject constructor() {
    
    fun getOfflineObjectIdentification(objectName: String, confidence: Float): IdentifiedObject {
        return IdentifiedObject(
            name = objectName,
            description = "This is a $objectName. When you're online, I can tell you much more about it!",
            category = "General",
            confidence = confidence,
            educationalValue = "Every object around us has something interesting to teach us. Let's explore this $objectName together!"
        )
    }
    
    fun getOfflineGradedSentences(word: String, minGrade: Int, maxGrade: Int): Map<Int, List<String>> {
        val sentences = mutableMapOf<Int, List<String>>()
        
        for (grade in minGrade..maxGrade) {
            sentences[grade] = when (grade) {
                2, 3 -> listOf(
                    "I see a $word.",
                    "The $word is here.",
                    "This is a $word."
                )
                4, 5 -> listOf(
                    "The $word is very interesting.",
                    "I found a $word today.",
                    "We can learn about the $word."
                )
                6, 7, 8 -> listOf(
                    "The $word has many interesting features.",
                    "Scientists study the $word to learn more.",
                    "Throughout history, the $word has been important."
                )
                else -> listOf(
                    "The $word represents an important concept in our understanding.",
                    "Analyzing the $word reveals complex relationships.",
                    "The significance of the $word extends beyond its immediate appearance."
                )
            }
        }
        
        return sentences
    }
    
    fun getOfflineSocraticLesson(objectName: String, grade: Int, duration: Int): SocraticLesson {
        val gradeAdjustedQuestions = when {
            grade <= 3 -> listOf(
                "What do you see when you look at this $objectName?",
                "What color is it?",
                "How does it feel when you touch it?",
                "What do you think it's used for?",
                "Can you find other things that are similar?"
            )
            grade <= 5 -> listOf(
                "What makes this $objectName special?",
                "How do you think it was made?",
                "Why do people use this?",
                "What would happen if we didn't have this?",
                "Can you think of ways to improve it?"
            )
            grade <= 8 -> listOf(
                "What properties does this $objectName have?",
                "How has this changed over time?",
                "What scientific principles does it demonstrate?",
                "How does this impact our daily lives?",
                "What connections can you make to other subjects?"
            )
            else -> listOf(
                "What underlying principles govern this $objectName?",
                "How does this relate to broader concepts?",
                "What innovations could transform this?",
                "What ethical considerations are involved?",
                "How might this evolve in the future?"
            )
        }
        
        return SocraticLesson(
            id = UUID.randomUUID().toString(),
            objectName = objectName,
            grade = grade,
            duration = duration,
            learningObjectives = listOf(
                "Observe and describe the $objectName",
                "Think critically about its purpose and function",
                "Make connections to your own experiences"
            ),
            initialQuestion = "Let's explore this $objectName together! What's the first thing you notice about it?",
            guidingQuestions = gradeAdjustedQuestions,
            vocabularyWords = listOf(
                VocabularyWord(
                    word = objectName,
                    definition = "The object we're learning about today",
                    exampleSentence = "This $objectName is interesting to study.",
                    gradeLevel = grade
                )
            ),
            funFacts = listOf(
                "Every $objectName has unique characteristics!",
                "People have been using things like this for many years.",
                "There's always more to discover when we look closely!"
            )
        )
    }
    
    fun getOfflineSocraticResponse(context: String, studentResponse: String, grade: Int): String {
        val encouragingPhrases = listOf(
            "That's a great observation!",
            "I like how you're thinking about this!",
            "You're really paying attention!",
            "Excellent point!",
            "That's very thoughtful!"
        )
        
        val followUpQuestions = when {
            grade <= 3 -> listOf(
                "What else do you notice?",
                "Can you tell me more about that?",
                "How does that make you feel?",
                "What do you think happens next?",
                "Why do you think that is?"
            )
            grade <= 8 -> listOf(
                "What makes you think that?",
                "Can you give me an example?",
                "How would you test that idea?",
                "What patterns do you see?",
                "How does this connect to what you already know?"
            )
            else -> listOf(
                "What evidence supports your thinking?",
                "How might others view this differently?",
                "What are the implications of that?",
                "Can you elaborate on that concept?",
                "What questions does this raise for you?"
            )
        }
        
        val encouragement = encouragingPhrases.random()
        val question = followUpQuestions.random()
        
        return "$encouragement $question"
    }
    
    fun getOfflineTranscription(): String {
        return "I'm having trouble hearing you right now. Could you type your answer instead?"
    }
    
    fun isOfflineMode(error: Throwable?): Boolean {
        return error?.message?.contains("network", ignoreCase = true) == true ||
               error?.message?.contains("internet", ignoreCase = true) == true ||
               error?.message?.contains("connection", ignoreCase = true) == true ||
               error?.message?.contains("timeout", ignoreCase = true) == true
    }
}