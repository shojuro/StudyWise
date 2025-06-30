package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.content.EducationalContentData.SkillData

/**
 * Language & Grammar skills data (Skills 61-75 of 80)
 */
object LanguageGrammarSkills {
    
    val skills = listOf(
        SkillData(
            skillId = "parts_of_speech",
            name = "Parts of Speech",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Identifying and using nouns, verbs, adjectives, and other parts of speech",
            prompts = mapOf(
                2 to "Find the action words (verbs) and naming words (nouns) in a sentence.",
                3 to "Identify nouns, verbs, and adjectives. How does each type of word do a different job?",
                4 to "Find all the nouns and verbs in this paragraph. How do these words work together to create meaning?",
                5 to "Identify nouns, verbs, adjectives, and adverbs in your text. How does each part of speech contribute to the sentence?",
                6 to "Analyze how different parts of speech function in complex sentences. When can words serve as multiple parts of speech?",
                7 to "Examine how parts of speech create style and emphasis. How do verb-heavy versus noun-heavy sentences differ in effect?",
                8 to "Evaluate how sophisticated use of parts of speech enhances writing. How do gerunds, participles, and infinitives add complexity?",
                9 to "Analyze how parts of speech create stylistic effects. How do noun-heavy versus verb-heavy passages differ rhetorically?",
                10 to "Examine how parts of speech blur in actual usage. When do words transcend categories?",
                11 to "Critique traditional part-of-speech categories. How do other languages organize words differently?",
                12 to "Theorize parts of speech as cultural constructs. How do grammatical categories shape thought?"
            )
        ),
        
        SkillData(
            skillId = "sentence_types",
            name = "Sentence Types",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Understanding and using different sentence structures",
            prompts = mapOf(
                2 to "Is this sentence telling, asking, or showing excitement? How do you know?",
                3 to "Find statements, questions, commands, and exclamations. What punctuation does each use?",
                4 to "Find examples of statements, questions, commands, and exclamations. Why did the author choose each type?",
                5 to "Identify simple, compound, and complex sentences. How does sentence type affect the rhythm of the writing?",
                6 to "Analyze how authors mix sentence types for effect. When are fragments or run-ons used purposefully?",
                7 to "Examine how sentence types create tone and pace. How do rhetorical questions function differently than regular questions?",
                8 to "Evaluate syntactic choices for rhetorical impact. How do periodic versus loose sentences affect emphasis?",
                9 to "Deploy sentence types strategically for rhetorical effect. When do questions function as assertions?",
                10 to "Analyze how sentence types create tone and authority. What does imperative mood accomplish?",
                11 to "Examine sentence types across genres and disciplines. How do conventions differ?",
                12 to "Theorize sentence types as speech acts. How do grammatical forms perform social functions?"
            )
        ),
        
        SkillData(
            skillId = "punctuation_purpose",
            name = "Punctuation Purpose",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Understanding how punctuation affects meaning",
            prompts = mapOf(
                2 to "Why do we need periods and question marks? What happens without them?",
                3 to "Look at periods, commas, question marks, and exclamation points. What job does each do?",
                4 to "Look at how periods, commas, and question marks are used. What would happen if you changed the punctuation?",
                5 to "Examine how punctuation guides reading. How do commas, semicolons, and dashes create different pauses?",
                6 to "Analyze punctuation choices for clarity and style. When do authors bend rules for effect?",
                7 to "Investigate how punctuation creates voice and meaning. How do colons, dashes, and ellipses shape interpretation?",
                8 to "Master punctuation as a rhetorical tool. How do unconventional choices serve artistic purposes?",
                9 to "Use punctuation as rhythmic and semantic tool. How do marks control reading experience?",
                10 to "Analyze punctuation as evolving system. How do new marks emerge for digital communication?",
                11 to "Compare punctuation across languages and time periods. What possibilities exist beyond English conventions?",
                12 to "Theorize punctuation as interpretation guide. How do marks constrain or enable meaning?"
            )
        ),
        
        SkillData(
            skillId = "subject_verb_agreement",
            name = "Subject-Verb Agreement",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Ensuring subjects and verbs match in number",
            prompts = mapOf(
                2 to "Does the action word match who's doing it? 'He run' or 'He runs'?",
                3 to "Make sure subjects and verbs agree. Why do we say 'The dog barks' not 'The dog bark'?",
                4 to "Check if the subjects and verbs match in number. Why is 'The dogs barks' incorrect?",
                5 to "Find tricky subject-verb agreements with collective nouns or compound subjects. What rules apply?",
                6 to "Analyze agreement in complex sentences with phrases between subjects and verbs. How do you identify the true subject?",
                7 to "Examine agreement with indefinite pronouns and inverted sentences. When do agreement rules seem counterintuitive?",
                8 to "Evaluate how agreement maintains clarity in sophisticated prose. How do writers handle agreement with abstract concepts?",
                9 to "Navigate complex agreement with collective nouns and indefinite pronouns. When do semantic and grammatical number conflict?",
                10 to "Analyze agreement in non-standard dialects. How do different Englishes handle agreement?",
                11 to "Examine agreement as arbitrary convention. Why do some languages not mark agreement?",
                12 to "Consider agreement in gender-neutral language evolution. How do pronouns challenge traditional rules?"
            )
        ),
        
        SkillData(
            skillId = "pronoun_usage",
            name = "Pronoun Usage",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Using pronouns correctly and clearly",
            prompts = mapOf(
                2 to "Find words like 'he,' 'she,' 'it,' and 'they.' Who do they mean?",
                3 to "Use pronouns to avoid repeating names. Make sure it's clear who you mean.",
                4 to "Find pronouns and identify what they replace. How do pronouns help avoid repetition?",
                5 to "Examine pronoun-antecedent agreement and clarity. When might pronouns confuse readers?",
                6 to "Analyze pronoun case (subject, object, possessive) in complex sentences. How does position determine form?",
                7 to "Investigate ambiguous pronoun references and how to fix them. How do writers maintain clarity with multiple antecedents?",
                8 to "Master sophisticated pronoun usage including indefinite and relative pronouns. How do pronoun choices affect tone and inclusivity?",
                9 to "Master pronoun usage for clarity in complex texts. How do you maintain reference across paragraphs?",
                10 to "Analyze pronouns as identity markers. How do pronoun choices reflect and construct identity?",
                11 to "Navigate evolving pronoun conventions. How do you respect individual preferences while maintaining clarity?",
                12 to "Theorize pronouns as political grammar. How do linguistic choices encode power relations?"
            )
        ),
        
        SkillData(
            skillId = "verb_tenses",
            name = "Verb Tenses",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Using appropriate verb tenses consistently",
            prompts = mapOf(
                2 to "Is this happening now, before, or later? How does the action word change?",
                3 to "Find past, present, and future tense verbs. How do they show when things happen?",
                4 to "Identify past, present, and future tense verbs. How does tense help you understand when things happen?",
                5 to "Examine how authors maintain or shift tense. What problems arise from unnecessary tense shifts?",
                6 to "Analyze perfect and progressive tenses. How do these forms show completed or ongoing actions?",
                7 to "Investigate tense in complex narratives with flashbacks. How do authors signal time shifts clearly?",
                8 to "Evaluate sophisticated tense usage in various genres. How does conditional or subjunctive mood affect meaning?",
                9 to "Deploy complex tenses for precise temporal relationships. How do perfect and progressive aspects interact?",
                10 to "Analyze literary tense usage for effect. Why might authors violate sequence of tenses?",
                11 to "Compare tense systems across languages. How do different cultures encode time grammatically?",
                12 to "Theorize tense as worldview. How do grammatical time systems shape temporal experience?"
            )
        ),
        
        SkillData(
            skillId = "modifiers",
            name = "Modifiers",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Using adjectives and adverbs effectively",
            prompts = mapOf(
                2 to "Find words that tell more about something. What do they describe?",
                3 to "Add describing words to make sentences more interesting. Where do they go?",
                4 to "Find words that describe or give more information. Where should these words be placed for clarity?",
                5 to "Identify misplaced or dangling modifiers. How can you fix confusing descriptions?",
                6 to "Analyze how modifiers create precise meaning. What's the difference between 'only ate pizza' and 'ate only pizza'?",
                7 to "Examine modifier placement for emphasis and clarity. How do participial phrases enhance description?",
                8 to "Master modifier usage for sophisticated effects. How do layers of modification create density or clarity?",
                9 to "Position modifiers for clarity and emphasis. How does placement affect meaning and focus?",
                10 to "Analyze ambiguous modification as stylistic choice. When might ambiguity serve purposes?",
                11 to "Compare modification patterns across languages. How do different systems create description?",
                12 to "Theorize modification as meaning negotiation. How do modifiers constrain or expand reference?"
            )
        ),
        
        SkillData(
            skillId = "parallel_structure",
            name = "Parallel Structure",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Creating balance in lists and comparisons",
            prompts = mapOf(
                2 to "When you list things, do they sound the same? 'I like running, jumping, and to play.'",
                3 to "Make lists follow the same pattern. Fix: 'She likes to read, writing, and draw.'",
                4 to "Find lists in your reading. Do all items follow the same pattern? Why does this matter?",
                5 to "Identify parallel structure in sentences with multiple actions or descriptions. How does parallelism improve flow?",
                6 to "Analyze parallelism in complex lists and comparisons. When should elements match in form?",
                7 to "Examine how parallelism creates rhythm and emphasis. How do writers use it in persuasive writing?",
                8 to "Evaluate sophisticated parallel structures across sentences and paragraphs. How does parallelism reinforce meaning?",
                9 to "Create complex parallel structures across sentence and paragraph levels. How does parallelism build arguments?",
                10 to "Analyze when to break parallelism for effect. How does variation within pattern create emphasis?",
                11 to "Examine parallelism in political and ceremonial language. Why do certain contexts demand parallel form?",
                12 to "Theorize parallelism as cognitive tool. How does structural repetition aid comprehension and memory?"
            )
        ),
        
        SkillData(
            skillId = "active_passive_voice",
            name = "Active and Passive Voice",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Understanding when subjects perform or receive action",
            prompts = mapOf(
                2 to "Who is doing the action? 'The cat chased the mouse' or 'The mouse was chased'?",
                3 to "Find sentences where someone does something versus something is done. Which sounds stronger?",
                4 to "Find sentences where the subject does the action versus receives it. Which sounds stronger?",
                5 to "Compare active and passive voice sentences. When might passive voice be the better choice?",
                6 to "Analyze voice choices in different text types. How does scientific writing use passive voice differently than narrative?",
                7 to "Examine how voice affects agency and emphasis. What gets hidden or highlighted in passive constructions?",
                8 to "Evaluate strategic voice choices across genres. How do writers manipulate voice for rhetorical purposes?",
                9 to "Choose voice strategically for agency and emphasis. When does passive voice serve ethical purposes?",
                10 to "Analyze voice in scientific and bureaucratic writing. How does passive voice obscure responsibility?",
                11 to "Compare voice systems across languages. How do ergative languages complicate active/passive distinctions?",
                12 to "Theorize voice as ideological grammar. How do voice choices reflect worldviews about action and causation?"
            )
        ),
        
        SkillData(
            skillId = "clause_types",
            name = "Clause Types",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Understanding independent and dependent clauses",
            prompts = mapOf(
                2 to "Find the main part of a sentence. What extra information is added?",
                3 to "Look for complete thoughts and incomplete thoughts in sentences. How do they work together?",
                4 to "Find the main part of a sentence that could stand alone. What extra information is added?",
                5 to "Identify independent and dependent clauses. How do connecting words show relationships?",
                6 to "Analyze how multiple clauses create complex meaning. What types of dependent clauses serve different purposes?",
                7 to "Examine clause relationships in sophisticated sentences. How do relative and subordinate clauses add layers?",
                8 to "Master clause manipulation for style. How do writers balance complexity with clarity?",
                9 to "Construct complex sentences balancing clarity with sophistication. How many clauses can readers process?",
                10 to "Analyze clause relationships for logical precision. How do different conjunctions create different relationships?",
                11 to "Compare clause structures across languages. How do different systems create complexity?",
                12 to "Theorize syntax as thought structure. How do available clause patterns shape expressible ideas?"
            )
        ),
        
        SkillData(
            skillId = "capitalization_rules",
            name = "Capitalization Rules",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Knowing when to use capital letters",
            prompts = mapOf(
                2 to "What words need capital letters? Check beginnings of sentences and names.",
                3 to "Find all capital letters. Why does each word need to be capitalized?",
                4 to "Find all capital letters in a paragraph. What rules explain why each word is capitalized?",
                5 to "Examine capitalization in titles, dialogue, and proper nouns. When do common words become capitalized?",
                6 to "Analyze capitalization in complex cases like titles within sentences. What style differences exist?",
                7 to "Investigate capitalization conventions across different text types. How do poetic or stylistic choices differ?",
                8 to "Evaluate capitalization as a stylistic and conventional choice. When do writers deliberately break rules?",
                9 to "Navigate capitalization in specialized contexts. How do different style guides handle edge cases?",
                10 to "Analyze capitalization as honor and hierarchy marker. What gets capitalized reveals what matters?",
                11 to "Examine capitalization history and evolution. How have conventions changed with technology?",
                12 to "Theorize capitalization as visual rhetoric. How do capital letters function beyond convention?"
            )
        ),
        
        SkillData(
            skillId = "word_formation",
            name = "Word Formation",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Understanding how words are built with prefixes and suffixes",
            prompts = mapOf(
                2 to "Add -ing, -ed, or -s to words. How do they change the meaning?",
                3 to "Find root words and endings. How do prefixes like 'un-' or 're-' change meanings?",
                4 to "Find words with prefixes or suffixes. How do these parts change the word's meaning?",
                5 to "Identify root words and affixes. How can understanding word parts help you figure out new words?",
                6 to "Analyze how affixes create different parts of speech. How does 'happy' become 'happiness' or 'happily'?",
                7 to "Examine complex word formations and etymology. How do Latin and Greek roots combine?",
                8 to "Master morphology for vocabulary expansion. How do you coin new words following English patterns?",
                9 to "Create and analyze neologisms using productive morphology. How do new words enter language?",
                10 to "Examine word formation across languages. How do different systems create new vocabulary?",
                11 to "Analyze technical and slang word formation. How do specialized communities create terminology?",
                12 to "Theorize morphology as cultural archive. How do word parts encode historical meanings?"
            )
        ),
        
        SkillData(
            skillId = "conventions_in_dialogue",
            name = "Conventions in Dialogue",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Punctuating and formatting dialogue correctly",
            prompts = mapOf(
                2 to "Where do the talking marks (quotation marks) go? Practice with 'said.'",
                3 to "Punctuate dialogue correctly. Where do commas and periods go with quotation marks?",
                4 to "Look at how dialogue is punctuated. Where do quotation marks, commas, and periods go?",
                5 to "Examine dialogue tags and punctuation patterns. How do you punctuate questions and exclamations in dialogue?",
                6 to "Analyze dialogue conventions including interrupted speech and paragraphing. When do you start new paragraphs?",
                7 to "Investigate sophisticated dialogue punctuation with actions and multiple speakers. How do you maintain clarity?",
                8 to "Master dialogue conventions across different styles. How do writers innovate within conventions?",
                9 to "Master dialogue conventions across different style guides. How do conventions vary by context?",
                10 to "Analyze unconventional dialogue punctuation in literature. What effects do innovations create?",
                11 to "Compare dialogue conventions across languages and scripts. How do different systems mark speech?",
                12 to "Theorize dialogue conventions as frame markers. How do quotation systems shape reported speech?"
            )
        ),
        
        SkillData(
            skillId = "comparative_forms",
            name = "Comparative Forms",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Forming comparisons correctly",
            prompts = mapOf(
                2 to "Make words that compare: big, bigger, biggest. What's the pattern?",
                3 to "Use -er and -est or 'more' and 'most' to compare. When do you use each?",
                4 to "Find words that compare things (bigger, biggest, more beautiful). What patterns do you notice?",
                5 to "Examine comparative and superlative forms. When do you use -er/-est versus more/most?",
                6 to "Analyze irregular comparisons and absolute adjectives. Why can't something be 'more perfect'?",
                7 to "Investigate nuanced comparisons and double comparatives. How do writers emphasize degree effectively?",
                8 to "Evaluate comparison usage for precision and style. How do comparisons function rhetorically?",
                9 to "Navigate complex comparisons and absolute adjectives. When can unique things be 'more unique'?",
                10 to "Analyze comparison as evaluation system. How do comparative structures encode values?",
                11 to "Examine comparison across languages. How do different systems create gradation?",
                12 to "Theorize comparison as fundamental cognition. How does language enable evaluative thinking?"
            )
        ),
        
        SkillData(
            skillId = "usage_conventions",
            name = "Usage Conventions",
            category = EducationalContentData.LANGUAGE_GRAMMAR,
            description = "Understanding common usage rules and exceptions",
            prompts = mapOf(
                2 to "Which is right: 'I goed' or 'I went'? Learn tricky words that don't follow rules.",
                3 to "Fix common mistakes like 'your/you're' and 'there/their/they're.' How do you remember?",
                4 to "Find commonly confused words (their/there/they're). How does context help you choose correctly?",
                5 to "Identify frequently misused words and phrases. What memory tricks help you remember correct usage?",
                6 to "Analyze formal versus informal usage conventions. How does audience affect word choice?",
                7 to "Examine evolving usage and dialectical variations. When is 'correct' usage actually flexible?",
                8 to "Master register and usage for different contexts. How do you navigate prescriptive versus descriptive grammar?",
                9 to "Master contested usage while understanding controversies. When do you follow disputed rules?",
                10 to "Analyze usage as social marker. How do usage choices signal group membership?",
                11 to "Examine prescriptivism versus descriptivism debates. Who determines 'correct' usage?",
                12 to "Theorize standard language as power structure. How do usage conventions maintain hierarchies?"
            )
        )
    )
}