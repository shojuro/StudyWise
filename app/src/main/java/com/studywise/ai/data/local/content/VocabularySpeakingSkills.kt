package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.content.EducationalContentData.SkillData

/**
 * Vocabulary & Speaking skills data (Skills 76-80 of 80)
 */
object VocabularySpeakingSkills {
    
    val skills = listOf(
        SkillData(
            skillId = "context_clues",
            name = "Context Clues",
            category = EducationalContentData.VOCABULARY_SPEAKING,
            description = "Using surrounding text to determine word meanings",
            prompts = mapOf(
                2 to "Find a new word. What do the other words around it tell you it might mean?",
                3 to "Use clues in sentences to figure out unknown words. What kind of clues help most?",
                4 to "Find an unfamiliar word in your text. What clues in the sentences around it help you guess its meaning?",
                5 to "Use context clues to determine word meanings. What types of clues (examples, definitions, contrasts) does the author provide?",
                6 to "Analyze multiple context clues to infer precise meanings. How do surrounding words narrow possible definitions?",
                7 to "Examine how context shapes connotation and denotation. When might the same word mean different things?",
                8 to "Master contextual analysis for sophisticated vocabulary. How do you determine meaning when clues are subtle or contradictory?",
                9 to "Decode specialized vocabulary through disciplinary context. How do fields create meaning systems?",
                10 to "Analyze how context shapes connotation beyond denotation. When does context override dictionary meaning?",
                11 to "Examine context as intertextual web. How do literary allusions create meaning through context?",
                12 to "Theorize context as meaning determinant. Can words have meaning outside contexts?"
            )
        ),
        
        SkillData(
            skillId = "word_relationships",
            name = "Word Relationships",
            category = EducationalContentData.VOCABULARY_SPEAKING,
            description = "Understanding synonyms, antonyms, and word families",
            prompts = mapOf(
                2 to "Find words that mean the same thing or opposite things. How are 'big' and 'large' alike?",
                3 to "Match synonyms and antonyms. How are 'happy/glad' the same but 'happy/sad' different?",
                4 to "Find words that mean the same (synonyms) or opposite (antonyms). How do these relationships help you understand?",
                5 to "Explore word relationships including synonyms, antonyms, and homonyms. How do slight differences in synonyms matter?",
                6 to "Analyze semantic relationships and word families. How do analogies help explain word relationships?",
                7 to "Investigate connotative differences among synonyms. How do word choices reveal bias or attitude?",
                8 to "Master nuanced word relationships and semantic fields. How do words cluster around concepts?",
                9 to "Map semantic fields and conceptual relationships. How do words cluster around ideas?",
                10 to "Analyze how word relationships shift across contexts. When do antonyms become synonyms?",
                11 to "Examine etymology and semantic change. How do word relationships evolve historically?",
                12 to "Theorize meaning as relational system. How do words define each other?"
            )
        ),
        
        SkillData(
            skillId = "academic_vocabulary",
            name = "Academic Vocabulary",
            category = EducationalContentData.VOCABULARY_SPEAKING,
            description = "Learning and using subject-specific vocabulary",
            prompts = mapOf(
                2 to "Learn school words like 'compare,' 'describe,' and 'explain.' Use one in a sentence.",
                3 to "Practice important school words like 'analyze' and 'summarize.' What does each mean?",
                4 to "Identify important words that appear across subjects (analyze, compare, describe). How do these words help in learning?",
                5 to "Use academic vocabulary to discuss texts. What formal words can replace casual ones in school discussions?",
                6 to "Apply discipline-specific academic language. How does vocabulary signal scholarly thinking?",
                7 to "Integrate sophisticated academic vocabulary naturally. When does precise terminology enhance versus obscure?",
                8 to "Master academic register across disciplines. How do you code-switch between academic and everyday language?",
                9 to "Deploy academic vocabulary with precision across disciplines. How do terms shift meaning between fields?",
                10 to "Analyze academic vocabulary as gatekeeping mechanism. How does specialized language include/exclude?",
                11 to "Navigate code-switching between academic and vernacular registers. When does each serve best?",
                12 to "Theorize academic discourse as culture. What worldviews do academic vocabularies encode?"
            )
        ),
        
        SkillData(
            skillId = "speaking_presentation",
            name = "Speaking and Presentation",
            category = EducationalContentData.VOCABULARY_SPEAKING,
            description = "Communicating ideas orally with clarity and confidence",
            prompts = mapOf(
                2 to "Tell someone about what you read. Speak clearly and look at them.",
                3 to "Present what you learned to others. Use a strong voice and explain clearly.",
                4 to "Practice explaining what you've read to someone else. What are the most important points to share?",
                5 to "Present information clearly using appropriate vocabulary and organization. How do you adjust for your audience?",
                6 to "Develop presentations that engage listeners. How do verbal and nonverbal elements work together?",
                7 to "Craft compelling oral arguments or explanations. How do you balance preparation with natural delivery?",
                8 to "Master various presentation styles and purposes. How do you maintain authority while inviting dialogue?",
                9 to "Deliver presentations that balance authority with accessibility. How do you establish credibility while remaining approachable?",
                10 to "Adapt presentation style to diverse audiences and purposes. How do you modulate formality and engagement?",
                11 to "Master multimedia presentation integrating verbal and visual rhetoric. How do modes interact?",
                12 to "Theorize presentation as performance. How do speaking situations construct speaker and audience?"
            )
        ),
        
        SkillData(
            skillId = "discussion_skills",
            name = "Discussion Skills",
            category = EducationalContentData.VOCABULARY_SPEAKING,
            description = "Participating effectively in academic discussions",
            prompts = mapOf(
                2 to "Talk with a partner about your reading. Listen to their ideas too!",
                3 to "Share ideas in a group. Ask questions and build on what others say.",
                4 to "Share your ideas about the text in a group. How do you build on what others say?",
                5 to "Participate in discussions by asking questions and responding to others. What makes a discussion productive?",
                6 to "Lead and contribute to collaborative discussions. How do you disagree respectfully and find common ground?",
                7 to "Facilitate rich discussions that deepen understanding. How do you draw out quiet voices and manage dominant ones?",
                8 to "Master sophisticated discussion techniques. How do you synthesize multiple viewpoints into new insights?",
                9 to "Facilitate discussions that generate new thinking. How do you move beyond debate to dialogue?",
                10 to "Navigate disagreement productively. How do you challenge ideas while respecting participants?",
                11 to "Lead seminar-style discussions that build collective understanding. How do you synthesize without dominating?",
                12 to "Theorize discussion as collaborative knowledge construction. How does dialogue create understanding?"
            )
        )
    )
}