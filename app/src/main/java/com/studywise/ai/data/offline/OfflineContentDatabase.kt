package com.studywise.ai.data.offline

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineContentDatabase @Inject constructor() {
    
    // Common objects with pre-generated educational content
    private val commonObjects = mapOf(
        // Household items
        "keys" to ObjectContent(
            name = "Keys",
            description = "Keys are metal tools that open locks on doors, cars, and other things.",
            category = "Everyday Objects",
            educationalValue = "Keys teach us about security, responsibility, and how simple machines work.",
            gradedSentences = mapOf(
                2 to listOf(
                    "I use keys to open my door.",
                    "My keys are on the table.",
                    "Keys help keep us safe."
                ),
                3 to listOf(
                    "Different keys open different locks.",
                    "I keep my keys in a special place.",
                    "Keys have unique shapes called teeth."
                ),
                4 to listOf(
                    "Keys and locks work together as a security system.",
                    "My family has several keys for our house and car.",
                    "Ancient Egyptians invented the first keys thousands of years ago."
                )
            ),
            socraticQuestions = listOf(
                "What do you notice about the shape of these keys?",
                "Why do you think keys have different patterns?",
                "How do keys help keep things safe?",
                "What would happen if all keys looked the same?",
                "Can you think of other ways we keep things secure?"
            )
        ),
        
        "book" to ObjectContent(
            name = "Book",
            description = "A book is a collection of pages with words and pictures that tell stories or share information.",
            category = "Learning Materials",
            educationalValue = "Books help us learn, imagine, and explore new worlds through reading.",
            gradedSentences = mapOf(
                2 to listOf(
                    "I love to read books.",
                    "This book has colorful pictures.",
                    "Books tell us stories."
                ),
                3 to listOf(
                    "Books can teach us about many different topics.",
                    "The library has thousands of books to explore.",
                    "I bookmark my favorite pages in books."
                ),
                4 to listOf(
                    "Books have been important for sharing knowledge throughout history.",
                    "Digital books and paper books each have unique advantages.",
                    "Authors spend months or years writing books for readers to enjoy."
                )
            ),
            socraticQuestions = listOf(
                "What makes books special to you?",
                "How are books different from watching videos?",
                "Why do you think people still read books in the digital age?",
                "What can you learn from the cover of a book?",
                "How do books help us use our imagination?"
            )
        ),
        
        "apple" to ObjectContent(
            name = "Apple",
            description = "An apple is a round, crunchy fruit that grows on trees and comes in many colors.",
            category = "Food",
            educationalValue = "Apples teach us about healthy eating, how plants grow, and the importance of fruits in our diet.",
            gradedSentences = mapOf(
                2 to listOf(
                    "I eat an apple for lunch.",
                    "Apples are red, green, or yellow.",
                    "Apples grow on trees."
                ),
                3 to listOf(
                    "Apples contain vitamins that keep us healthy.",
                    "Apple trees bloom with flowers in spring.",
                    "We can make juice and pie from apples."
                ),
                4 to listOf(
                    "Apples are one of the most popular fruits grown worldwide.",
                    "Different apple varieties have unique flavors and textures.",
                    "Apple orchards require careful planning and seasonal care."
                )
            ),
            socraticQuestions = listOf(
                "What do you notice about the apple's shape and color?",
                "Why do you think apples have seeds inside?",
                "How do apples change as they grow on the tree?",
                "What happens to an apple after you bite it?",
                "Why are apples considered healthy food?"
            )
        ),
        
        "pencil" to ObjectContent(
            name = "Pencil",
            description = "A pencil is a writing tool made of wood with graphite inside that leaves marks on paper.",
            category = "School Supplies",
            educationalValue = "Pencils help us learn about writing, drawing, and how simple tools can create amazing things.",
            gradedSentences = mapOf(
                2 to listOf(
                    "I write with my pencil.",
                    "Pencils can be sharp or dull.",
                    "We use pencils at school."
                ),
                3 to listOf(
                    "Pencils contain graphite, not lead, for writing.",
                    "Erasers help us fix mistakes made with pencils.",
                    "Artists use different pencils for drawing."
                ),
                4 to listOf(
                    "Pencils have been used for writing for over 400 years.",
                    "The hardness of pencil graphite affects how dark the marks appear.",
                    "Mechanical pencils use replaceable graphite sticks instead of wood."
                )
            ),
            socraticQuestions = listOf(
                "How is a pencil different from a pen?",
                "What makes pencil marks erasable?",
                "Why do you think pencils are made of wood?",
                "How do artists use pencils differently than writers?",
                "What would school be like without pencils?"
            )
        ),
        
        "water bottle" to ObjectContent(
            name = "Water Bottle",
            description = "A water bottle is a container that holds water or other drinks for us to carry and drink.",
            category = "Personal Items",
            educationalValue = "Water bottles teach us about staying hydrated, reducing waste, and taking care of our health.",
            gradedSentences = mapOf(
                2 to listOf(
                    "I drink water from my bottle.",
                    "My water bottle is blue.",
                    "Water bottles keep drinks cold."
                ),
                3 to listOf(
                    "Reusable water bottles help protect our environment.",
                    "I fill my water bottle before going outside.",
                    "Different materials keep water cold for different times."
                ),
                4 to listOf(
                    "Insulated water bottles use special technology to maintain temperature.",
                    "Staying hydrated with a water bottle improves our concentration.",
                    "Reusable bottles significantly reduce plastic waste in our oceans."
                )
            ),
            socraticQuestions = listOf(
                "Why is it important to carry water with us?",
                "How do water bottles help the environment?",
                "What makes some bottles keep water cold longer?",
                "When do you need to drink more water?",
                "How has the design of water bottles changed over time?"
            )
        )
    )
    
    fun getObjectContent(objectName: String): ObjectContent? {
        // Try exact match first
        commonObjects[objectName.lowercase()]?.let { return it }
        
        // Try partial match
        commonObjects.entries.firstOrNull { (key, _) ->
            objectName.lowercase().contains(key) || key.contains(objectName.lowercase())
        }?.value?.let { return it }
        
        // Generate generic content if not found
        return generateGenericContent(objectName)
    }
    
    private fun generateGenericContent(objectName: String): ObjectContent {
        return ObjectContent(
            name = objectName.capitalize(),
            description = "This is an interesting object that we can learn about together.",
            category = "Objects",
            educationalValue = "Learning about different objects helps us understand the world around us.",
            gradedSentences = mapOf(
                2 to listOf(
                    "I see a $objectName.",
                    "The $objectName is interesting.",
                    "Let's learn about the $objectName."
                ),
                3 to listOf(
                    "The $objectName has special features we can explore.",
                    "I wonder what the $objectName is used for.",
                    "We can discover new things about the $objectName."
                ),
                4 to listOf(
                    "The $objectName might have an interesting history.",
                    "Understanding the $objectName helps us learn about our world.",
                    "Every object, including the $objectName, has a purpose."
                )
            ),
            socraticQuestions = listOf(
                "What do you notice about this ${objectName}?",
                "How do you think people use this ${objectName}?",
                "What makes this $objectName special or unique?",
                "Can you describe the $objectName in your own words?",
                "What questions do you have about this ${objectName}?"
            )
        )
    }
    
    fun getAllObjects(): List<String> {
        return commonObjects.keys.toList()
    }
}

data class ObjectContent(
    val name: String,
    val description: String,
    val category: String,
    val educationalValue: String,
    val gradedSentences: Map<Int, List<String>>,
    val socraticQuestions: List<String>
)