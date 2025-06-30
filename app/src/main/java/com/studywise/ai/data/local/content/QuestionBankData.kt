package com.studywise.ai.data.local.content

/**
 * Comprehensive question bank with multiple question types per skill per grade
 * Each skill has 5-10 questions per grade level (2-12)
 */
object QuestionBankData {
    
    data class QuestionTemplate(
        val skillId: String,
        val gradeLevel: Int,
        val questionType: String, // diagnostic, practice, challenge, assessment
        val prompt: String,
        val hints: List<String>,
        val followUpQuestions: List<String>,
        val scaffoldingPrompts: List<String>,
        val expectedResponseElements: List<String>,
        val difficulty: Float
    )
    
    // Character Traits Questions (Grades 2-12)
    val characterTraitsQuestions = listOf(
        // Grade 2
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 2,
            questionType = "practice",
            prompt = "Tell me about the main character. Are they nice, mean, brave, or scared? What did they do that shows this?",
            hints = listOf(
                "Look at what they say to others",
                "Think about their actions",
                "How do they treat friends?"
            ),
            followUpQuestions = listOf(
                "Can you find where this happens in your book?",
                "Would you want to be friends with them? Why?",
                "What else did they do?"
            ),
            scaffoldingPrompts = listOf(
                "Let's start with one thing the character did",
                "What happened at the beginning?",
                "How did the character feel?"
            ),
            expectedResponseElements = listOf("character name", "trait word", "example from text"),
            difficulty = 0.2f
        ),
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 2,
            questionType = "diagnostic",
            prompt = "Pick your favorite character. Tell me three things about them.",
            hints = listOf(
                "What do they look like?",
                "How do they act?",
                "What do they like to do?"
            ),
            followUpQuestions = listOf(
                "Which thing is most important?",
                "Show me in your book",
                "Why do you like them?"
            ),
            scaffoldingPrompts = listOf(
                "Let's think of one thing first",
                "What color is their hair?",
                "Are they big or small?"
            ),
            expectedResponseElements = listOf("character identification", "descriptive details"),
            difficulty = 0.15f
        ),
        
        // Grade 4
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 4,
            questionType = "practice",
            prompt = "Think about the main character in your story. What three words would you use to describe them? Can you find a part in the story that shows one of these traits?",
            hints = listOf(
                "Look for what the character says or does",
                "Think about how other characters react to them",
                "Check the beginning and see how they've changed"
            ),
            followUpQuestions = listOf(
                "What made you choose these words?",
                "Has this character changed during the story?",
                "Which trait is strongest?"
            ),
            scaffoldingPrompts = listOf(
                "Let's look at one important scene",
                "What did the character do when there was a problem?",
                "How do they treat others?"
            ),
            expectedResponseElements = listOf("three trait words", "text evidence", "explanation"),
            difficulty = 0.3f
        ),
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 4,
            questionType = "challenge",
            prompt = "Compare the main character at the beginning and end of your story. What traits stayed the same? What changed? Why do you think this happened?",
            hints = listOf(
                "Make a before/after list",
                "Look for the turning point",
                "Think about what they learned"
            ),
            followUpQuestions = listOf(
                "What caused the biggest change?",
                "Could this change happen in real life?",
                "What might happen next?"
            ),
            scaffoldingPrompts = listOf(
                "First, describe them at the beginning",
                "Now, how are they at the end?",
                "What big events happened?"
            ),
            expectedResponseElements = listOf("comparison", "evidence of change", "reasoning"),
            difficulty = 0.4f
        ),
        
        // Grade 6
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 6,
            questionType = "practice",
            prompt = "What character traits define the protagonist? How do their actions, dialogue, and thoughts reveal these traits throughout the story?",
            hints = listOf(
                "Create a list of traits with evidence",
                "Look for contradictions in their character",
                "Consider how traits affect the plot"
            ),
            followUpQuestions = listOf(
                "Which trait drives the story forward?",
                "How do other characters respond to these traits?",
                "Are any traits in conflict with each other?"
            ),
            scaffoldingPrompts = listOf(
                "Start with their most obvious trait",
                "Find three examples of this trait",
                "Now look for a contrasting trait"
            ),
            expectedResponseElements = listOf("multiple traits", "varied evidence types", "analysis of impact"),
            difficulty = 0.5f
        ),
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 6,
            questionType = "assessment",
            prompt = "Analyze how the author uses indirect characterization to reveal the protagonist's personality. Include examples of actions, dialogue, thoughts, and others' reactions.",
            hints = listOf(
                "Indirect means 'showing' not 'telling'",
                "Look for patterns across chapters",
                "Consider what's NOT said"
            ),
            followUpQuestions = listOf(
                "Which method is most effective? Why?",
                "What traits are hidden at first?",
                "How reliable is each source of information?"
            ),
            scaffoldingPrompts = listOf(
                "Find one example of each type",
                "Which reveals the most?",
                "Connect the examples"
            ),
            expectedResponseElements = listOf("understanding of indirect characterization", "multiple examples", "evaluation"),
            difficulty = 0.6f
        ),
        
        // Grade 8
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 8,
            questionType = "practice",
            prompt = "Examine how the protagonist's character traits evolve throughout the narrative. How do internal and external forces shape their development?",
            hints = listOf(
                "Map traits at beginning, middle, and end",
                "Identify catalysts for change",
                "Analyze the interplay of nature vs. nurture"
            ),
            followUpQuestions = listOf(
                "Which changes are permanent?",
                "What traits remain constant? Why?",
                "How does society influence the character?"
            ),
            scaffoldingPrompts = listOf(
                "Chart the character's journey",
                "Mark major turning points",
                "Connect changes to causes"
            ),
            expectedResponseElements = listOf("evolution tracking", "cause-effect analysis", "internal/external forces"),
            difficulty = 0.7f
        ),
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 8,
            questionType = "challenge",
            prompt = "How do the protagonist's contradictory traits create internal conflict and drive the narrative? Analyze the psychological complexity of the character.",
            hints = listOf(
                "Find opposing traits",
                "Look for moments of internal struggle",
                "Consider how this reflects real people"
            ),
            followUpQuestions = listOf(
                "Which contradiction is most significant?",
                "How does the author make this believable?",
                "What does this reveal about human nature?"
            ),
            scaffoldingPrompts = listOf(
                "Identify two opposing traits",
                "Find a scene showing internal conflict",
                "Explain the character's dilemma"
            ),
            expectedResponseElements = listOf("contradiction identification", "conflict analysis", "thematic connection"),
            difficulty = 0.8f
        ),
        
        // Grade 10
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 10,
            questionType = "practice",
            prompt = "Analyze how the protagonist's traits reflect broader social or philosophical themes. How does individual character illuminate universal human experiences?",
            hints = listOf(
                "Connect personal to universal",
                "Consider historical/social context",
                "Look for archetypal patterns"
            ),
            followUpQuestions = listOf(
                "What archetype does this character represent?",
                "How do their traits critique society?",
                "What philosophical questions arise?"
            ),
            scaffoldingPrompts = listOf(
                "Identify the character's defining trait",
                "What does this trait represent?",
                "Connect to a larger theme"
            ),
            expectedResponseElements = listOf("trait analysis", "thematic connection", "universal application"),
            difficulty = 0.8f
        ),
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 10,
            questionType = "assessment",
            prompt = "Evaluate the psychological realism of the protagonist's character development. How effectively does the author balance consistency with growth?",
            hints = listOf(
                "Consider psychological theories",
                "Analyze believability of changes",
                "Evaluate character consistency"
            ),
            followUpQuestions = listOf(
                "What aspects feel most/least authentic?",
                "How does this compare to real psychology?",
                "What choices strengthen believability?"
            ),
            scaffoldingPrompts = listOf(
                "Track one trait throughout",
                "Note consistent elements",
                "Evaluate major changes"
            ),
            expectedResponseElements = listOf("realism evaluation", "consistency analysis", "critical judgment"),
            difficulty = 0.85f
        ),
        
        // Grade 12
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 12,
            questionType = "practice",
            prompt = "Deconstruct the protagonist's character through multiple critical lenses (psychological, social, feminist, postcolonial, etc.). How do different perspectives reveal different aspects of their personality?",
            hints = listOf(
                "Apply at least three critical lenses",
                "Consider what each lens reveals/conceals",
                "Synthesize multiple viewpoints"
            ),
            followUpQuestions = listOf(
                "Which lens is most revealing? Why?",
                "What contradictions emerge?",
                "How does this enrich understanding?"
            ),
            scaffoldingPrompts = listOf(
                "Choose one critical lens",
                "Apply it systematically",
                "Add another perspective"
            ),
            expectedResponseElements = listOf("multiple perspectives", "critical analysis", "synthesis"),
            difficulty = 0.9f
        ),
        QuestionTemplate(
            skillId = "character_traits",
            gradeLevel = 12,
            questionType = "challenge",
            prompt = "Examine how the protagonist's character traits function as a commentary on contemporary human identity. What does their characterization suggest about selfhood in the modern/postmodern world?",
            hints = listOf(
                "Consider fragmentation vs. unity",
                "Analyze performance of identity",
                "Connect to contemporary theory"
            ),
            followUpQuestions = listOf(
                "How does this reflect current identity theory?",
                "What does this suggest about authenticity?",
                "How might future readers interpret this?"
            ),
            scaffoldingPrompts = listOf(
                "Define one aspect of modern identity",
                "Find examples in the character",
                "Draw broader conclusions"
            ),
            expectedResponseElements = listOf("theoretical understanding", "contemporary relevance", "philosophical depth"),
            difficulty = 0.95f
        )
    )
    
    // Theme Identification Questions (Grades 2-12)
    val themeQuestions = listOf(
        // Grade 2
        QuestionTemplate(
            skillId = "theme_identification",
            gradeLevel = 2,
            questionType = "practice",
            prompt = "What lesson did the character learn? What can we learn from this story?",
            hints = listOf(
                "Think about the ending",
                "What changed?",
                "What would you remember?"
            ),
            followUpQuestions = listOf(
                "Have you learned this too?",
                "When could you use this lesson?",
                "Tell me more about that"
            ),
            scaffoldingPrompts = listOf(
                "What was the problem?",
                "How was it solved?",
                "What's important here?"
            ),
            expectedResponseElements = listOf("simple lesson", "personal connection"),
            difficulty = 0.2f
        ),
        
        // Grade 4
        QuestionTemplate(
            skillId = "theme_identification",
            gradeLevel = 4,
            questionType = "practice",
            prompt = "What message is the author trying to share through this story? Find one part that really shows this message.",
            hints = listOf(
                "Themes are often about life lessons",
                "Look for repeated ideas or symbols",
                "Check character conversations about important topics"
            ),
            followUpQuestions = listOf(
                "Why is this message important?",
                "Have you seen this theme in other stories?",
                "How does the title connect?"
            ),
            scaffoldingPrompts = listOf(
                "What keeps happening in the story?",
                "What do characters learn?",
                "Find an important moment"
            ),
            expectedResponseElements = listOf("theme statement", "supporting evidence", "explanation"),
            difficulty = 0.3f
        ),
        
        // Grade 6
        QuestionTemplate(
            skillId = "theme_identification",
            gradeLevel = 6,
            questionType = "practice",
            prompt = "What universal theme emerges from this story? How do the character's experiences convey this theme?",
            hints = listOf(
                "Universal themes apply to many people",
                "Track how the theme develops",
                "Connect events to the bigger message"
            ),
            followUpQuestions = listOf(
                "How does this theme relate to real life?",
                "What symbols reinforce this theme?",
                "Are there competing themes?"
            ),
            scaffoldingPrompts = listOf(
                "List big ideas in the story",
                "Which appears most often?",
                "Connect to character growth"
            ),
            expectedResponseElements = listOf("universal theme", "development tracking", "character connection"),
            difficulty = 0.5f
        ),
        
        // Grade 8
        QuestionTemplate(
            skillId = "theme_identification",
            gradeLevel = 8,
            questionType = "practice",
            prompt = "Analyze how the author develops complex themes through literary techniques. How do conflicting ideas create thematic tension?",
            hints = listOf(
                "Identify primary and secondary themes",
                "Analyze thematic contradictions",
                "Examine how structure reinforces theme"
            ),
            followUpQuestions = listOf(
                "Which theme is most powerful? Why?",
                "How do themes interact?",
                "What techniques are most effective?"
            ),
            scaffoldingPrompts = listOf(
                "Find two opposing ideas",
                "Track each through the story",
                "Analyze their collision"
            ),
            expectedResponseElements = listOf("multiple themes", "technique analysis", "tension exploration"),
            difficulty = 0.7f
        ),
        
        // Grade 10
        QuestionTemplate(
            skillId = "theme_identification",
            gradeLevel = 10,
            questionType = "practice",
            prompt = "Examine how themes in this work reflect or challenge dominant cultural narratives. What commentary does the author offer on society?",
            hints = listOf(
                "Identify assumed cultural values",
                "Find moments of conformity/rebellion",
                "Consider historical context"
            ),
            followUpQuestions = listOf(
                "What cultural assumptions are questioned?",
                "How does this remain relevant today?",
                "What solutions are suggested?"
            ),
            scaffoldingPrompts = listOf(
                "What societal norm appears?",
                "How do characters respond?",
                "What's the author's stance?"
            ),
            expectedResponseElements = listOf("cultural analysis", "author commentary", "contemporary relevance"),
            difficulty = 0.85f
        ),
        
        // Grade 12
        QuestionTemplate(
            skillId = "theme_identification",
            gradeLevel = 12,
            questionType = "challenge",
            prompt = "Deconstruct the thematic architecture of this work. How do multiple themes create a complex philosophical argument about human existence?",
            hints = listOf(
                "Map thematic relationships",
                "Identify philosophical traditions",
                "Synthesize into coherent argument"
            ),
            followUpQuestions = listOf(
                "What philosophical school does this reflect?",
                "How do themes challenge each other?",
                "What questions remain unanswered?"
            ),
            scaffoldingPrompts = listOf(
                "Identify three major themes",
                "Find their intersections",
                "Build the overall argument"
            ),
            expectedResponseElements = listOf("thematic mapping", "philosophical depth", "synthetic argument"),
            difficulty = 0.95f
        )
    )
    
    // Main Idea Questions for Informational Text (Grades 2-12)
    val mainIdeaQuestions = listOf(
        // Grade 2
        QuestionTemplate(
            skillId = "main_idea_informational",
            gradeLevel = 2,
            questionType = "practice",
            prompt = "What is this text mostly about? Tell me the most important thing you learned.",
            hints = listOf(
                "Look at the title",
                "What comes up a lot?",
                "What's the big idea?"
            ),
            followUpQuestions = listOf(
                "What else did you learn?",
                "Why is this important?",
                "Can you show me where?"
            ),
            scaffoldingPrompts = listOf(
                "Read the first sentence",
                "What word appears most?",
                "Put it in your words"
            ),
            expectedResponseElements = listOf("topic identification", "importance statement"),
            difficulty = 0.2f
        ),
        
        // Grade 4
        QuestionTemplate(
            skillId = "main_idea_informational",
            gradeLevel = 4,
            questionType = "practice",
            prompt = "What's the main idea of this passage? Find two details that support this main idea.",
            hints = listOf(
                "The main idea is what it's all about",
                "Supporting details give examples or proof",
                "Check topic sentences"
            ),
            followUpQuestions = listOf(
                "Are there other supporting details?",
                "Which detail is strongest?",
                "How do they connect?"
            ),
            scaffoldingPrompts = listOf(
                "What subject keeps appearing?",
                "Find a fact about it",
                "Find another fact"
            ),
            expectedResponseElements = listOf("main idea statement", "two supporting details", "connection"),
            difficulty = 0.3f
        ),
        
        // Grade 6
        QuestionTemplate(
            skillId = "main_idea_informational",
            gradeLevel = 6,
            questionType = "practice",
            prompt = "Identify the central idea and explain how the author develops it throughout the text. Which details are most crucial?",
            hints = listOf(
                "Central idea may be implied",
                "Track development paragraph by paragraph",
                "Distinguish crucial from minor details"
            ),
            followUpQuestions = listOf(
                "How does structure support the central idea?",
                "What details could be removed?",
                "What's the author's purpose?"
            ),
            scaffoldingPrompts = listOf(
                "Summarize each paragraph",
                "Find the common thread",
                "Rank details by importance"
            ),
            expectedResponseElements = listOf("central idea", "development analysis", "detail evaluation"),
            difficulty = 0.5f
        ),
        
        // Grade 8
        QuestionTemplate(
            skillId = "main_idea_informational",
            gradeLevel = 8,
            questionType = "practice",
            prompt = "Analyze how the author establishes and refines the central idea through structure, evidence, and rhetorical devices.",
            hints = listOf(
                "Examine organizational patterns",
                "Evaluate evidence quality",
                "Identify persuasive techniques"
            ),
            followUpQuestions = listOf(
                "Which technique is most effective?",
                "How does structure enhance understanding?",
                "What counterarguments are addressed?"
            ),
            scaffoldingPrompts = listOf(
                "Outline the text structure",
                "Categorize types of evidence",
                "Find rhetorical devices"
            ),
            expectedResponseElements = listOf("structural analysis", "evidence evaluation", "rhetorical awareness"),
            difficulty = 0.7f
        ),
        
        // Grade 10
        QuestionTemplate(
            skillId = "main_idea_informational",
            gradeLevel = 10,
            questionType = "practice",
            prompt = "Evaluate how effectively the author's central claim is supported. What assumptions underlie the argument?",
            hints = listOf(
                "Identify explicit and implicit claims",
                "Examine logical connections",
                "Question unstated assumptions"
            ),
            followUpQuestions = listOf(
                "What evidence is missing?",
                "Are the assumptions valid?",
                "How might opponents respond?"
            ),
            scaffoldingPrompts = listOf(
                "State the main claim",
                "List supporting evidence",
                "Identify gaps or assumptions"
            ),
            expectedResponseElements = listOf("claim identification", "support evaluation", "assumption analysis"),
            difficulty = 0.85f
        ),
        
        // Grade 12
        QuestionTemplate(
            skillId = "main_idea_informational",
            gradeLevel = 12,
            questionType = "challenge",
            prompt = "Critique the text's central argument within its discourse community. How does it contribute to or challenge the ongoing scholarly conversation?",
            hints = listOf(
                "Situate within academic context",
                "Identify theoretical framework",
                "Assess contribution to field"
            ),
            followUpQuestions = listOf(
                "What paradigm does this represent?",
                "How does it advance the field?",
                "What further research is needed?"
            ),
            scaffoldingPrompts = listOf(
                "Identify the field/discipline",
                "Find references to other work",
                "Assess originality"
            ),
            expectedResponseElements = listOf("discourse awareness", "scholarly context", "critical evaluation"),
            difficulty = 0.95f
        )
    )
    
    // Writing Structure Questions (Grades 2-12)
    val writingStructureQuestions = listOf(
        // Grade 2
        QuestionTemplate(
            skillId = "paragraph_structure",
            gradeLevel = 2,
            questionType = "practice",
            prompt = "Let's write a paragraph about your favorite animal. Start with what animal it is, then tell me three things about it.",
            hints = listOf(
                "First sentence: name your animal",
                "Add three details",
                "End with why you like it"
            ),
            followUpQuestions = listOf(
                "Can you add more details?",
                "What else is special?",
                "Read it back to me"
            ),
            scaffoldingPrompts = listOf(
                "What animal do you pick?",
                "Tell me one thing about it",
                "What's another thing?"
            ),
            expectedResponseElements = listOf("topic sentence", "three details", "basic organization"),
            difficulty = 0.2f
        ),
        
        // Grade 4
        QuestionTemplate(
            skillId = "paragraph_structure",
            gradeLevel = 4,
            questionType = "practice",
            prompt = "Write a paragraph explaining your favorite hobby. Include a topic sentence, three supporting details, and a conclusion.",
            hints = listOf(
                "Topic sentence introduces your hobby",
                "Each detail should explain why you enjoy it",
                "Conclusion wraps up your thoughts"
            ),
            followUpQuestions = listOf(
                "How can you make details more specific?",
                "Does each sentence connect?",
                "Is your conclusion strong?"
            ),
            scaffoldingPrompts = listOf(
                "State your hobby clearly",
                "Give your best reason",
                "Add two more reasons"
            ),
            expectedResponseElements = listOf("clear topic sentence", "organized details", "concluding sentence"),
            difficulty = 0.3f
        ),
        
        // Grade 6
        QuestionTemplate(
            skillId = "paragraph_structure",
            gradeLevel = 6,
            questionType = "practice",
            prompt = "Construct a well-developed paragraph with a clear claim, relevant evidence, and thoughtful analysis. Use transitions to connect ideas.",
            hints = listOf(
                "Start with a debatable claim",
                "Provide specific evidence",
                "Explain how evidence supports claim"
            ),
            followUpQuestions = listOf(
                "Is your evidence convincing?",
                "How can you strengthen analysis?",
                "Are transitions smooth?"
            ),
            scaffoldingPrompts = listOf(
                "Make a clear claim",
                "Find strong evidence",
                "Connect evidence to claim"
            ),
            expectedResponseElements = listOf("claim", "evidence", "analysis", "transitions"),
            difficulty = 0.5f
        ),
        
        // Grade 8
        QuestionTemplate(
            skillId = "paragraph_structure",
            gradeLevel = 8,
            questionType = "practice",
            prompt = "Develop a sophisticated paragraph using the PEEL structure (Point, Evidence, Explanation, Link). Address potential counterarguments.",
            hints = listOf(
                "Make your point debatable",
                "Use credible evidence",
                "Acknowledge complexity"
            ),
            followUpQuestions = listOf(
                "Where might readers disagree?",
                "How can you refine explanation?",
                "Does your link advance the argument?"
            ),
            scaffoldingPrompts = listOf(
                "State your point clearly",
                "Support with best evidence",
                "Address the 'so what?'"
            ),
            expectedResponseElements = listOf("PEEL structure", "counterargument", "sophisticated analysis"),
            difficulty = 0.7f
        ),
        
        // Grade 10
        QuestionTemplate(
            skillId = "paragraph_structure",
            gradeLevel = 10,
            questionType = "practice",
            prompt = "Compose an analytical paragraph that synthesizes multiple sources. Demonstrate how different perspectives contribute to understanding.",
            hints = listOf(
                "Introduce competing viewpoints",
                "Find connections between sources",
                "Build toward insight"
            ),
            followUpQuestions = listOf(
                "How do sources dialogue?",
                "What synthesis emerges?",
                "Is your voice clear?"
            ),
            scaffoldingPrompts = listOf(
                "Introduce first source",
                "Add contrasting view",
                "Find the connection"
            ),
            expectedResponseElements = listOf("source synthesis", "analytical voice", "complex understanding"),
            difficulty = 0.85f
        ),
        
        // Grade 12
        QuestionTemplate(
            skillId = "paragraph_structure",
            gradeLevel = 12,
            questionType = "challenge",
            prompt = "Craft a paragraph that advances a nuanced argument while maintaining academic discourse conventions. Demonstrate mastery of rhetorical strategies.",
            hints = listOf(
                "Balance assertion with qualification",
                "Deploy rhetorical devices purposefully",
                "Maintain scholarly tone"
            ),
            followUpQuestions = listOf(
                "How does style support content?",
                "What rhetorical choices are most effective?",
                "Does this meet disciplinary expectations?"
            ),
            scaffoldingPrompts = listOf(
                "Establish scholarly ethos",
                "Layer your argument",
                "Refine academic voice"
            ),
            expectedResponseElements = listOf("nuanced argument", "rhetorical mastery", "academic discourse"),
            difficulty = 0.95f
        )
    )
    
    // Context Clues Questions (Grades 2-12)
    val contextCluesQuestions = listOf(
        // Grade 2
        QuestionTemplate(
            skillId = "context_clues",
            gradeLevel = 2,
            questionType = "practice",
            prompt = "Find a word you don't know in your reading. Can you guess what it means by looking at the words around it?",
            hints = listOf(
                "Read the whole sentence",
                "Look for clue words",
                "Think: what makes sense?"
            ),
            followUpQuestions = listOf(
                "What helped you guess?",
                "Could it mean something else?",
                "Use it in a new sentence"
            ),
            scaffoldingPrompts = listOf(
                "Read before the word",
                "Read after the word",
                "What's happening here?"
            ),
            expectedResponseElements = listOf("word identification", "meaning guess", "clue recognition"),
            difficulty = 0.2f
        ),
        
        // Grade 4
        QuestionTemplate(
            skillId = "context_clues",
            gradeLevel = 4,
            questionType = "practice",
            prompt = "When you find an unknown word, what clues in the sentence help you understand it? Show me how you figure out the meaning.",
            hints = listOf(
                "Look for definitions or examples",
                "Check for synonyms or antonyms",
                "Consider the overall topic"
            ),
            followUpQuestions = listOf(
                "What type of context clue is this?",
                "Can you find another example?",
                "How sure are you of the meaning?"
            ),
            scaffoldingPrompts = listOf(
                "Find the unknown word",
                "Circle helpful words nearby",
                "Make your best guess"
            ),
            expectedResponseElements = listOf("clue identification", "reasoning process", "meaning determination"),
            difficulty = 0.3f
        ),
        
        // Grade 6
        QuestionTemplate(
            skillId = "context_clues",
            gradeLevel = 6,
            questionType = "practice",
            prompt = "Identify different types of context clues (definition, synonym, antonym, example, inference) in your text. How does each type help determine meaning?",
            hints = listOf(
                "Categorize the clue types",
                "Some clues are subtle",
                "Multiple clues increase accuracy"
            ),
            followUpQuestions = listOf(
                "Which clue type is most helpful?",
                "How do clues work together?",
                "What if clues conflict?"
            ),
            scaffoldingPrompts = listOf(
                "Find a definition clue",
                "Find an example clue",
                "Compare their effectiveness"
            ),
            expectedResponseElements = listOf("clue categorization", "effectiveness analysis", "systematic approach"),
            difficulty = 0.5f
        ),
        
        // Grade 8
        QuestionTemplate(
            skillId = "context_clues",
            gradeLevel = 8,
            questionType = "practice",
            prompt = "Analyze how context clues reveal not just denotation but also connotation and tone. How do surrounding words shape meaning?",
            hints = listOf(
                "Consider emotional coloring",
                "Look for attitude markers",
                "Examine word associations"
            ),
            followUpQuestions = listOf(
                "What mood does this create?",
                "How would synonyms change tone?",
                "What does word choice reveal?"
            ),
            scaffoldingPrompts = listOf(
                "Find the literal meaning",
                "Identify emotional overtones",
                "Connect to author's purpose"
            ),
            expectedResponseElements = listOf("denotation/connotation", "tone analysis", "nuanced understanding"),
            difficulty = 0.7f
        ),
        
        // Grade 10
        QuestionTemplate(
            skillId = "context_clues",
            gradeLevel = 10,
            questionType = "practice",
            prompt = "Examine how technical or discipline-specific vocabulary gains meaning through context. How do authors introduce specialized terminology?",
            hints = listOf(
                "Look for embedded definitions",
                "Track conceptual building",
                "Notice scaffolding techniques"
            ),
            followUpQuestions = listOf(
                "How does this differ from general vocabulary?",
                "What prior knowledge is assumed?",
                "How effective is the introduction?"
            ),
            scaffoldingPrompts = listOf(
                "Identify technical terms",
                "Find introduction strategies",
                "Evaluate clarity"
            ),
            expectedResponseElements = listOf("technical vocabulary", "introduction strategies", "effectiveness evaluation"),
            difficulty = 0.85f
        ),
        
        // Grade 12
        QuestionTemplate(
            skillId = "context_clues",
            gradeLevel = 12,
            questionType = "challenge",
            prompt = "Analyze how context shapes meaning in ambiguous or polysemous language. How do authors exploit multiple meanings for effect?",
            hints = listOf(
                "Identify intentional ambiguity",
                "Trace multiple meaning paths",
                "Consider interpretive implications"
            ),
            followUpQuestions = listOf(
                "When is ambiguity productive?",
                "How do contexts compete?",
                "What interpretive work is required?"
            ),
            scaffoldingPrompts = listOf(
                "Find ambiguous language",
                "Map possible meanings",
                "Analyze author's purpose"
            ),
            expectedResponseElements = listOf("ambiguity recognition", "multiple interpretations", "sophisticated analysis"),
            difficulty = 0.95f
        )
    )
    
    // Compile all questions
    val allQuestions: List<QuestionTemplate> = 
        characterTraitsQuestions + 
        themeQuestions + 
        mainIdeaQuestions +
        writingStructureQuestions +
        contextCluesQuestions
    
    /**
     * Get questions for a specific skill and grade level
     */
    fun getQuestionsForSkill(skillId: String, gradeLevel: Int): List<QuestionTemplate> {
        return allQuestions.filter { 
            it.skillId == skillId && it.gradeLevel == gradeLevel 
        }
    }
    
    /**
     * Get questions by type
     */
    fun getQuestionsByType(questionType: String): List<QuestionTemplate> {
        return allQuestions.filter { it.questionType == questionType }
    }
    
    /**
     * Get adaptive questions based on student performance
     */
    fun getAdaptiveQuestions(
        skillId: String, 
        gradeLevel: Int, 
        currentMastery: Float
    ): List<QuestionTemplate> {
        val baseQuestions = getQuestionsForSkill(skillId, gradeLevel)
        
        // Filter by appropriate difficulty
        return baseQuestions.filter { question ->
            when {
                currentMastery < 0.3f -> question.difficulty <= 0.4f
                currentMastery < 0.6f -> question.difficulty in 0.3f..0.7f
                currentMastery < 0.8f -> question.difficulty in 0.5f..0.85f
                else -> question.difficulty >= 0.7f
            }
        }
    }
    
    /**
     * Generate question variations using templates
     */
    fun generateQuestionVariation(
        template: QuestionTemplate,
        textReference: String = "your text"
    ): QuestionTemplate {
        val variations = listOf(
            "Think about", "Consider", "Reflect on", "Examine", "Explore"
        )
        
        val newPrompt = template.prompt.replace(
            "your story", textReference
        ).replace(
            "this story", textReference
        ).replace(
            "the story", textReference
        )
        
        return template.copy(
            prompt = newPrompt,
            scaffoldingPrompts = template.scaffoldingPrompts.map { prompt ->
                prompt.replace("story", textReference.removePrefix("your "))
            }
        )
    }
}