package com.studywise.ai.data.local.content

/**
 * Complete educational content data for StudyWise AI
 * Contains all 80 English Language Arts skills with grade-specific prompts for grades 2-12
 */
object EducationalContentData {
    
    data class SkillData(
        val skillId: String,
        val name: String,
        val category: String,
        val description: String,
        val prompts: Map<Int, String> // Grade level to prompt
    )
    
    // Categories
    const val READING_LITERATURE = "Reading Literature"
    const val READING_INFORMATIONAL = "Reading Informational"
    const val WRITING = "Writing"
    const val LANGUAGE_GRAMMAR = "Language & Grammar"
    const val VOCABULARY_SPEAKING = "Vocabulary & Speaking"
    
    val allSkills = listOf(
        // ===== READING LITERATURE SKILLS (20) =====
        SkillData(
            skillId = "character_traits",
            name = "Character Traits",
            category = READING_LITERATURE,
            description = "Analyzing and understanding character personalities and qualities",
            prompts = mapOf(
                2 to "Tell me about the main character. Are they nice, mean, brave, or scared? How do you know?",
                3 to "What kind of person is the main character? Find two things they do that show what they're like.",
                4 to "Think about the main character in your story. What three words would you use to describe them? Can you find a part in the story that shows one of these traits?",
                5 to "How would you describe the main character's personality? Find two examples from the story where their actions or words show us what they're really like.",
                6 to "What character traits define the protagonist? How do their actions, dialogue, and thoughts reveal these traits throughout the story?",
                7 to "Analyze how the author develops the main character's personality. What techniques (actions, dialogue, thoughts, others' reactions) reveal their complex traits?",
                8 to "Examine how the protagonist's character traits evolve throughout the narrative. How do internal and external forces shape their development?",
                9 to "Analyze how the author reveals character complexity through indirect characterization. What contradictions emerge in the protagonist's nature?",
                10 to "Examine how character traits evolve in response to conflict. How do internal and external forces shape identity?",
                11 to "Critique the author's characterization techniques within the literary tradition. How do archetypal patterns interact with individual complexity?",
                12 to "Synthesize multiple theoretical lenses to interpret character construction. How do psychological, social, and philosophical frameworks illuminate character depth?"
            )
        ),
        
        SkillData(
            skillId = "plot_sequence",
            name = "Plot Sequence",
            category = READING_LITERATURE,
            description = "Understanding the order and structure of story events",
            prompts = mapOf(
                2 to "What happened first in your story? What happened next? What happened at the end?",
                3 to "Tell the story in order. What are the three most important things that happened?",
                4 to "What happened at the beginning, middle, and end of your story? Can you tell me the most important event in each part?",
                5 to "List the five most important events in your story in order. How does each event lead to the next one?",
                6 to "Map out the plot structure of your story. How does the rising action build tension toward the climax?",
                7 to "Analyze the plot's progression from exposition through resolution. How does the author control pacing to build suspense or emphasis?",
                8 to "Evaluate how the plot structure enhances the story's themes. How do subplot elements interweave with the main narrative arc?",
                9 to "Analyze how plot structure creates meaning beyond chronology. How do flashbacks, parallel plots, or non-linear elements enhance themes?",
                10 to "Evaluate the relationship between plot structure and narrative tension. How does the author manipulate reader expectations through structural choices?",
                11 to "Critique plot conventions within the work's genre and period. How does the author honor or subvert traditional narrative patterns?",
                12 to "Theorize about the philosophical implications of the plot's structure. How does narrative form reflect worldview or challenge linear thinking?"
            )
        ),
        
        SkillData(
            skillId = "identifying_theme",
            name = "Identifying Theme",
            category = READING_LITERATURE,
            description = "Discovering the central message or lesson in literature",
            prompts = mapOf(
                2 to "What did the character learn in this story? Have you ever learned the same thing?",
                3 to "What is the big lesson or message in your story? How does it help you in real life?",
                4 to "What lesson do you think the main character learned in this story? How could this lesson help you in your own life?",
                5 to "What message is the author trying to share through this story? Find one part that really shows this message.",
                6 to "What universal theme emerges from this story? How do the character's experiences convey this theme?",
                7 to "Identify the central theme and explain how multiple story elements (plot, character, setting) work together to develop it.",
                8 to "Analyze how the author develops complex themes through literary techniques. How do conflicting ideas create thematic tension?",
                9 to "Examine how multiple themes interact and create tension. Which theme ultimately dominates and why?",
                10 to "Analyze how themes develop through imagery, symbolism, and motif patterns. How do recurring elements build thematic resonance?",
                11 to "Critique the work's thematic concerns within historical and cultural contexts. How do themes reflect or challenge contemporary values?",
                12 to "Synthesize thematic analysis with critical theory. How do feminist, postcolonial, or other theoretical approaches reveal hidden themes?"
            )
        ),
        
        SkillData(
            skillId = "setting_impact",
            name = "Setting Impact",
            category = READING_LITERATURE,
            description = "Understanding how time and place affect the story",
            prompts = mapOf(
                2 to "Where does your story happen? When does it happen? Draw a picture of this place.",
                3 to "Where and when does your story take place? How does this place make the story exciting or scary?",
                4 to "Where and when does your story take place? How does this place affect what happens in the story?",
                5 to "Describe the setting of your story. How would the story be different if it happened in a completely different place or time?",
                6 to "How does the setting influence the characters' actions and the plot? Consider both time period and location.",
                7 to "Analyze how the author uses setting to create mood, develop conflict, or reveal character. What symbolic significance might the setting have?",
                8 to "Evaluate how setting functions as more than backdrop. How does it reflect themes, create atmosphere, and drive character development?",
                9 to "Analyze setting as a dynamic force rather than static backdrop. How does place function almost as a character?",
                10 to "Evaluate how temporal and spatial settings create symbolic meaning. What metaphorical significance does the setting carry?",
                11 to "Critique the author's use of setting within literary movements. How does setting reflect naturalist, romantic, or modernist philosophies?",
                12 to "Theorize about setting as cultural construct. How do geographical and temporal choices encode power dynamics or ideological positions?"
            )
        ),
        
        SkillData(
            skillId = "point_of_view",
            name = "Point of View",
            category = READING_LITERATURE,
            description = "Identifying and analyzing narrative perspective",
            prompts = mapOf(
                2 to "Who is telling the story? Is it one of the characters or someone else?",
                3 to "Who tells this story? How would it be different if another character told it?",
                4 to "Who is telling this story? How do you know? What would be different if another character told it?",
                5 to "From whose point of view is this story told? How does this affect what information we get as readers?",
                6 to "Identify the narrative perspective. How does this point of view limit or expand our understanding of events and characters?",
                7 to "Analyze how the chosen point of view shapes the reader's experience. What biases or limitations does this perspective create?",
                8 to "Evaluate the author's choice of narrative perspective. How does it manipulate reader sympathy, create irony, or enhance themes?",
                9 to "Analyze how narrative perspective shapes truth and reliability. What remains unknowable due to perspectival limitations?",
                10 to "Evaluate the ethical implications of the chosen perspective. Whose voices are centered, marginalized, or silenced?",
                11 to "Critique point of view as an artistic and political choice. How does perspective reflect or challenge dominant narratives?",
                12 to "Synthesize narratological theory with close reading. How do concepts like focalization and narrative distance operate in the text?"
            )
        ),
        
        SkillData(
            skillId = "conflict_identification",
            name = "Conflict Identification",
            category = READING_LITERATURE,
            description = "Recognizing and analyzing story conflicts",
            prompts = mapOf(
                2 to "What is the big problem in your story? Who has this problem?",
                3 to "What problem does the main character need to solve? What makes it hard to solve?",
                4 to "What is the main problem in your story? Who or what is causing this problem for the main character?",
                5 to "What conflict does the main character face? Is it with another person, nature, society, or themselves?",
                6 to "Identify the primary conflict and any secondary conflicts. How do these different conflicts relate to each other?",
                7 to "Analyze the layers of conflict in the story. How do internal and external conflicts intertwine to create complexity?",
                8 to "Examine how multiple conflicts drive the narrative. How does the resolution (or lack thereof) of conflicts reflect the work's themes?",
                9 to "Examine how multiple conflict layers create thematic complexity. How do personal conflicts mirror societal tensions?",
                10 to "Analyze unresolved or ambiguous conflicts. What does the lack of clear resolution suggest about the human condition?",
                11 to "Critique conflict representation within genre expectations. How does the author reimagine traditional conflict patterns?",
                12 to "Theorize about conflict as philosophical inquiry. How do conflicts embody existential, ethical, or epistemological questions?"
            )
        ),
        
        SkillData(
            skillId = "making_inferences",
            name = "Making Inferences",
            category = READING_LITERATURE,
            description = "Drawing conclusions from textual clues",
            prompts = mapOf(
                2 to "How do you think the character feels? What clues help you know?",
                3 to "What can you figure out that the author doesn't tell you? What hints did you use?",
                4 to "Based on what the character did, how do you think they were feeling? What clues in the story helped you figure this out?",
                5 to "What can you figure out about the character that the author doesn't directly tell you? What evidence supports your thinking?",
                6 to "What inferences can you make about characters' motivations or future actions based on subtle clues in the text?",
                7 to "Read between the lines to infer unstated relationships, motivations, or outcomes. How does subtext enhance meaning?",
                8 to "Analyze what the author implies but doesn't state explicitly. How do these inferences deepen thematic understanding?",
                9 to "Analyze the gap between what's stated and implied. How do silences and omissions create meaning?",
                10 to "Evaluate competing inferences and their textual support. How does ambiguity serve the author's purpose?",
                11 to "Critique the cultural assumptions required for certain inferences. What knowledge does the text assume or exclude?",
                12 to "Synthesize inferential reading with hermeneutic theory. How do reader, text, and context interact to create meaning?"
            )
        ),
        
        SkillData(
            skillId = "character_motivation",
            name = "Character Motivation",
            category = READING_LITERATURE,
            description = "Understanding why characters act as they do",
            prompts = mapOf(
                2 to "Why did the character do that? What did they want?",
                3 to "Why does the main character make their big choice? What do they hope will happen?",
                4 to "Why did the main character make that important choice? What did they want to achieve?",
                5 to "What drives the main character's actions throughout the story? How do their wants and needs change?",
                6 to "Examine the protagonist's motivations. How do internal desires conflict with external pressures?",
                7 to "Analyze the complex motivations driving character decisions. How do competing motivations create internal conflict?",
                8 to "Evaluate how character motivations reflect broader themes. How do unconscious drives influence conscious choices?",
                9 to "Examine the interplay between conscious and unconscious motivations. How do characters deceive themselves?",
                10 to "Analyze how social forces shape individual motivation. Where do personal desires conflict with cultural expectations?",
                11 to "Critique motivational complexity using psychological frameworks. How do Freudian, Jungian, or other theories illuminate character drives?",
                12 to "Theorize about free will versus determinism in character motivation. How does the text position human agency?"
            )
        ),
        
        SkillData(
            skillId = "comparing_characters",
            name = "Comparing Characters",
            category = READING_LITERATURE,
            description = "Analyzing similarities and differences between characters",
            prompts = mapOf(
                2 to "Pick two characters. How are they the same? How are they different?",
                3 to "Compare two characters in your story. Make a list of what's alike and what's different.",
                4 to "How are two characters in your story alike? How are they different? Make a list for each.",
                5 to "Compare two characters' personalities and actions. What makes each character unique?",
                6 to "Compare and contrast two characters' roles in the story. How do their differences create tension or complement each other?",
                7 to "Analyze how character foils highlight the protagonist's qualities. What does each character represent thematically?",
                8 to "Examine how character relationships and contrasts illuminate central themes. How do dynamic character interactions drive meaning?",
                9 to "Analyze character foils and doubles. How do parallels and contrasts illuminate central themes?",
                10 to "Evaluate character relationships as power dynamics. How do social hierarchies shape character interactions?",
                11 to "Critique character types within literary traditions. How does the author use or subvert stock characters?",
                12 to "Synthesize character analysis with identity theory. How do characters embody or challenge constructions of race, gender, or class?"
            )
        ),
        
        SkillData(
            skillId = "figurative_language",
            name = "Figurative Language",
            category = READING_LITERATURE,
            description = "Understanding and analyzing metaphors, similes, and other devices",
            prompts = mapOf(
                2 to "Find a place where the author says something is like something else. What picture does it make in your mind?",
                3 to "Find words that paint pictures in your mind. Why did the author choose these special words?",
                4 to "Find an example where the author compares two things. What picture does this create in your mind?",
                5 to "Identify examples of similes, metaphors, or personification. How do they help you visualize the story?",
                6 to "How does the author use figurative language to enhance description or convey emotion? Find three different examples.",
                7 to "Analyze how figurative language contributes to mood, characterization, or theme. What patterns do you notice?",
                8 to "Evaluate the author's use of figurative language as a stylistic choice. How does it create layers of meaning?",
                9 to "Analyze extended metaphors and their thematic implications. How do figurative patterns create coherent meaning systems?",
                10 to "Evaluate how figurative language constructs reality rather than merely describing it. What worldview emerges through metaphor?",
                11 to "Critique figurative language within rhetorical traditions. How do classical tropes function in contemporary contexts?",
                12 to "Theorize about metaphor as cognitive framework. How does figurative language shape thought and perception?"
            )
        ),
        
        SkillData(
            skillId = "mood_and_tone",
            name = "Mood and Tone",
            category = READING_LITERATURE,
            description = "Identifying emotional atmosphere and author's attitude",
            prompts = mapOf(
                2 to "How does this part of the story make you feel? Happy? Sad? Scared? Why?",
                3 to "What feeling does the author create? Find words that make you feel this way.",
                4 to "How does this part of the story make you feel? What words does the author use to create that feeling?",
                5 to "What is the mood of this scene? Find specific words or descriptions that create this atmosphere.",
                6 to "Distinguish between mood and tone in a key passage. How does the author's word choice create each?",
                7 to "Analyze how the author establishes and shifts mood throughout the narrative. How does tone reveal attitude?",
                8 to "Examine how mood and tone work together to enhance meaning. How do tonal shifts signal thematic developments?",
                9 to "Analyze tonal shifts and their structural significance. How do mood changes signal thematic developments?",
                10 to "Evaluate the tension between surface tone and underlying mood. Where does ironic distance emerge?",
                11 to "Critique mood and tone as genre markers. How does the author fulfill or frustrate generic expectations?",
                12 to "Synthesize affective theory with textual analysis. How do mood and tone function as rhetorical strategies?"
            )
        ),
        
        SkillData(
            skillId = "symbolism",
            name = "Symbolism",
            category = READING_LITERATURE,
            description = "Recognizing objects or elements that represent deeper meanings",
            prompts = mapOf(
                2 to "Is there something special in the story that shows up more than once? Why is it important?",
                3 to "Find an object that seems really important in the story. What might it mean?",
                4 to "Is there an object in your story that seems really important? What might it represent beyond just being a thing?",
                5 to "Find an object, color, or place that appears multiple times. What bigger idea might it represent?",
                6 to "Identify potential symbols in the text. How do they connect to the story's themes or characters' journeys?",
                7 to "Analyze how symbols develop meaning throughout the text. How does context deepen symbolic significance?",
                8 to "Evaluate the symbolic network within the text. How do multiple symbols interact to create complex meaning?",
                9 to "Trace symbolic networks throughout the text. How do symbols accumulate meaning through repetition and variation?",
                10 to "Analyze competing symbolic interpretations. How does contextual knowledge alter symbolic meaning?",
                11 to "Critique symbolic systems within cultural frameworks. How do symbols encode or challenge ideological positions?",
                12 to "Theorize about the limits and possibilities of symbolic representation. What remains beyond symbolization?"
            )
        ),
        
        SkillData(
            skillId = "foreshadowing",
            name = "Foreshadowing",
            category = READING_LITERATURE,
            description = "Identifying hints about future events",
            prompts = mapOf(
                2 to "Did you guess what would happen before it did? What clues helped you?",
                3 to "Find a clue early in the story that hints at what happens later. How did it prepare you?",
                4 to "Were there any clues early in the story that hinted at what would happen later? What were they?",
                5 to "Find a moment where the author gives us a hint about future events. How did this prepare you for what happened?",
                6 to "Identify instances of foreshadowing. How does the author plant seeds that bloom later in the narrative?",
                7 to "Analyze how foreshadowing creates suspense and coherence. What techniques does the author use to hint without revealing?",
                8 to "Examine how foreshadowing operates on multiple levels. How does it create dramatic irony or thematic resonance?",
                9 to "Examine how foreshadowing creates deterministic versus open-ended narratives. What philosophy of time emerges?",
                10 to "Analyze false foreshadowing and misdirection. How does the author manipulate reader expectations?",
                11 to "Critique foreshadowing within narrative traditions. How does the technique reflect classical or modernist sensibilities?",
                12 to "Synthesize temporal theory with foreshadowing analysis. How does prolepsis challenge linear temporality?"
            )
        ),
        
        SkillData(
            skillId = "dialogue_analysis",
            name = "Dialogue Analysis",
            category = READING_LITERATURE,
            description = "Examining how characters speak and what it reveals",
            prompts = mapOf(
                2 to "Find people talking in your story. How can you tell who is speaking?",
                3 to "Look at what characters say. What do their words tell you about them?",
                4 to "Pick an important conversation in your story. What does the way characters talk tell us about them?",
                5 to "How do characters' words reveal their personalities or relationships? Find an example of revealing dialogue.",
                6 to "Analyze how dialogue advances the plot and reveals character. What do we learn beyond the literal words?",
                7 to "Examine how dialogue creates subtext. What remains unspoken, and why is that significant?",
                8 to "Evaluate dialogue as a vehicle for theme and characterization. How does conversational style reflect social dynamics?",
                9 to "Analyze dialogue as performance of identity. How do speech patterns reveal class, education, and cultural positioning?",
                10 to "Evaluate subtext and power dynamics in conversation. What remains unspoken and why?",
                11 to "Critique dialogue authenticity versus artistic stylization. How does the author balance realism with aesthetic purposes?",
                12 to "Theorize about dialogue as philosophical discourse. How do conversations embody ideological conflicts?"
            )
        ),
        
        SkillData(
            skillId = "story_structure",
            name = "Story Structure",
            category = READING_LITERATURE,
            description = "Understanding how stories are organized",
            prompts = mapOf(
                2 to "How does your story start? What makes you want to keep reading?",
                3 to "How does the author organize the story? Does it go in order or jump around?",
                4 to "How does your story begin? What makes you want to keep reading? How does it end?",
                5 to "Describe how the author organizes the story. Does it follow a typical pattern or surprise you?",
                6 to "Map the story's structure. How does the organization of events affect your understanding?",
                7 to "Analyze structural choices (flashbacks, parallel plots, circular narrative). How does structure enhance meaning?",
                8 to "Evaluate how structural innovation serves thematic purposes. How does form reflect or challenge content?",
                9 to "Analyze how structural innovations create meaning. What does experimental form communicate that traditional structure cannot?",
                10 to "Evaluate the politics of narrative structure. How do structural choices privilege certain perspectives?",
                11 to "Critique structure within literary movements. How does form reflect modernist fragmentation or postmodern play?",
                12 to "Synthesize structural analysis with narrative theory. How do concepts like story versus discourse illuminate the text?"
            )
        ),
        
        SkillData(
            skillId = "making_connections",
            name = "Making Connections",
            category = READING_LITERATURE,
            description = "Relating text to self, other texts, and the world",
            prompts = mapOf(
                2 to "Does this story remind you of something in your life? Tell about it.",
                3 to "How does this story connect to your life or another story you know?",
                4 to "Does anything in this story remind you of your own life or another story you know? Explain the connection.",
                5 to "Connect this story to your experiences, other texts, or the world. How do these connections deepen understanding?",
                6 to "Draw connections between this text and others you've read. What patterns or contrasts emerge?",
                7 to "Analyze intertextual connections or universal themes. How does this work participate in larger conversations?",
                8 to "Synthesize connections across multiple texts and contexts. How does this work reflect or challenge literary traditions?",
                9 to "Analyze intertextual connections and literary allusions. How does the text participate in literary conversations?",
                10 to "Evaluate connections between literature and historical contexts. How does the text reflect or resist its moment?",
                11 to "Critique the text's place in literary canon formation. What power structures determine literary value?",
                12 to "Synthesize interdisciplinary connections. How do philosophical, psychological, or sociological frameworks illuminate the text?"
            )
        ),
        
        SkillData(
            skillId = "character_relationships",
            name = "Character Relationships",
            category = READING_LITERATURE,
            description = "Analyzing how characters interact and influence each other",
            prompts = mapOf(
                2 to "Who are friends in your story? Who doesn't get along? How can you tell?",
                3 to "How do characters treat each other? Do their relationships change?",
                4 to "How do the main characters get along? Do their relationships change during the story?",
                5 to "Describe an important relationship in your story. How do these characters affect each other?",
                6 to "Examine how character relationships drive the plot. What dynamics create tension or support?",
                7 to "Analyze the complexity of character relationships. How do power dynamics, history, or secrets influence interactions?",
                8 to "Evaluate how relationship dynamics reflect broader themes. How do interpersonal conflicts mirror societal issues?",
                9 to "Examine relationships as microcosms of larger social structures. How do personal dynamics reflect systemic issues?",
                10 to "Analyze the construction of desire and intimacy. How does the text represent human connection?",
                11 to "Critique relationship representations within cultural norms. How does the text reinforce or challenge relationship scripts?",
                12 to "Theorize about intersubjectivity and recognition. How do characters constitute each other's identities?"
            )
        ),
        
        SkillData(
            skillId = "author_purpose",
            name = "Author's Purpose",
            category = READING_LITERATURE,
            description = "Understanding why the author wrote the story",
            prompts = mapOf(
                2 to "Why do you think someone wrote this story? To make you laugh? To teach you?",
                3 to "Why did the author write this story? What do they want you to think about?",
                4 to "Why do you think the author wrote this story? What did they want readers to think about?",
                5 to "What message or feeling does the author want to share? How can you tell from the story?",
                6 to "Infer the author's purpose beyond entertainment. What ideas or emotions do they want to convey?",
                7 to "Analyze how various elements serve the author's purpose. What social, emotional, or philosophical goals emerge?",
                8 to "Evaluate the author's multi-layered purposes. How do artistic, social, and personal motivations intersect?",
                9 to "Analyze multiple layers of authorial purpose beyond surface intention. What unconscious purposes might emerge?",
                10 to "Evaluate the tension between authorial intent and textual effect. How does meaning exceed intention?",
                11 to "Critique the concept of authorial purpose. How does 'death of the author' theory complicate intentionality?",
                12 to "Synthesize authorial purpose with reception theory. How do readers co-create textual meaning?"
            )
        ),
        
        SkillData(
            skillId = "resolution_analysis",
            name = "Resolution Analysis",
            category = READING_LITERATURE,
            description = "Examining how conflicts are resolved",
            prompts = mapOf(
                2 to "How does the problem get fixed? Are you happy with the ending?",
                3 to "How is the problem solved? Would you change the ending? Why?",
                4 to "How was the main problem solved? Were you satisfied with how the story ended? Why?",
                5 to "Examine how the conflict gets resolved. What questions are answered and what remains open?",
                6 to "Analyze the resolution's effectiveness. Does it satisfy the conflicts raised? What loose ends remain?",
                7 to "Evaluate whether the resolution fits the story's tone and themes. How does ambiguity or closure serve the narrative?",
                8 to "Critique the resolution's implications. How does the ending comment on the themes? What does it suggest about life?",
                9 to "Examine how resolution or its absence reflects worldview. What does closure or openness suggest about meaning?",
                10 to "Analyze the ideological implications of resolution. What values does the ending affirm or challenge?",
                11 to "Critique resolution within genre conventions. How does the ending satisfy or frustrate reader expectations?",
                12 to "Theorize about narrative closure and its impossibility. How do endings gesture beyond themselves?"
            )
        ),
        
        SkillData(
            skillId = "literary_devices",
            name = "Literary Devices",
            category = READING_LITERATURE,
            description = "Identifying and analyzing various literary techniques",
            prompts = mapOf(
                2 to "Find words that sound fun to say. Why did the author pick these words?",
                3 to "Find places where the author repeats words or sounds. What does this do?",
                4 to "Find a place where the author repeats words or sounds. Why do you think they did this?",
                5 to "Identify examples of repetition, alliteration, or rhyme. How do these techniques affect your reading?",
                6 to "Locate various literary devices (irony, allusion, imagery). How do they enhance the story's impact?",
                7 to "Analyze how multiple literary devices work together. What effects do they create for readers?",
                8 to "Evaluate the author's deployment of literary devices. How do technique and meaning interconnect?",
                9 to "Analyze how multiple devices create layered effects. How do techniques interact to produce complex meanings?",
                10 to "Evaluate device usage as stylistic signature. What aesthetic philosophy guides technical choices?",
                11 to "Critique devices within literary periods and movements. How do techniques mark historical moments?",
                12 to "Synthesize formal analysis with aesthetic theory. How do devices embody theories of beauty or sublimity?"
            )
        ),
        
        // ===== READING INFORMATIONAL SKILLS (20) =====
        SkillData(
            skillId = "main_idea_informational",
            name = "Main Idea (Informational)",
            category = READING_INFORMATIONAL,
            description = "Identifying central ideas in nonfiction texts",
            prompts = mapOf(
                2 to "What is this text mostly about? Tell me in one sentence.",
                3 to "What is the main idea? Find two facts that tell more about this idea.",
                4 to "What is the most important idea in this section? Can you find two details that support this main idea?",
                5 to "Identify the central idea of this text. Which key details help develop this idea throughout the passage?",
                6 to "Determine the central idea and explain how it's developed through specific details. How do examples support the main point?",
                7 to "Analyze how the author develops the central idea through carefully selected details. Which evidence is most compelling and why?",
                8 to "Evaluate how effectively the central idea emerges and develops. How do supporting details build a comprehensive argument?",
                9 to "Analyze how the central idea emerges through accumulation and emphasis. What rhetorical strategies focus attention?",
                10 to "Evaluate competing ideas within the text. How does the author subordinate some concepts while privileging others?",
                11 to "Critique the construction of 'main ideas' as rhetorical choice. What ideologies shape information hierarchy?",
                12 to "Theorize about the politics of centrality. How do main ideas marginalize alternative perspectives?"
            )
        ),
        
        // Continue with all remaining skills...
        // [Note: Due to length constraints, I'll include a few more examples and then provide the structure]
        
        SkillData(
            skillId = "text_structure_informational",
            name = "Text Structure (Informational)",
            category = READING_INFORMATIONAL,
            description = "Understanding how informational texts are organized",
            prompts = mapOf(
                2 to "Does this text tell steps in order or describe something? How do you know?",
                3 to "How is this text organized? Does it compare things, show steps, or explain why something happens?",
                4 to "How is this text organized? Does it describe steps in order, compare things, or explain cause and effect?",
                5 to "What text structure does the author use (chronological, compare/contrast, problem/solution)? How does this help you understand the information?",
                6 to "Analyze the text's organizational structure. How does this structure support the author's purpose and help convey information?",
                7 to "Examine how the author's structural choices enhance meaning. How would a different structure change the impact?",
                8 to "Evaluate the effectiveness of the text's structure. How do organizational patterns at paragraph and whole-text levels work together?",
                9 to "Examine how structure shapes argument beyond organization. How does form become persuasive force?",
                10 to "Analyze structural choices as epistemological claims. What ways of knowing does the structure privilege?",
                11 to "Critique structure within disciplinary conventions. How do academic fields shape organizational patterns?",
                12 to "Synthesize structural analysis with rhetoric theory. How does arrangement function as invention?"
            )
        ),
        
    ) + 
    ReadingInformationalSkills.skills +
    WritingSkills.skills +
    LanguageGrammarSkills.skills +
    VocabularySpeakingSkills.skills
    
    /**
     * Get all skills for a specific category
     */
    fun getSkillsByCategory(category: String): List<SkillData> {
        return allSkills.filter { it.category == category }
    }
    
    /**
     * Get a specific skill by ID
     */
    fun getSkillById(skillId: String): SkillData? {
        return allSkills.find { it.skillId == skillId }
    }
    
    /**
     * Get all prompts for a specific grade level
     */
    fun getPromptsForGrade(gradeLevel: Int): Map<String, String> {
        return allSkills.associate { skill ->
            skill.skillId to (skill.prompts[gradeLevel] ?: "")
        }.filterValues { it.isNotEmpty() }
    }
    
    /**
     * Get skill progression recommendations based on mastery
     */
    fun getSkillProgressions(): Map<String, List<String>> {
        // This maps each skill to its prerequisites
        return mapOf(
            // Reading Literature progressions
            "character_traits" to emptyList(), // Foundation skill
            "character_motivation" to listOf("character_traits", "making_inferences"),
            "comparing_characters" to listOf("character_traits"),
            "character_relationships" to listOf("character_traits", "comparing_characters"),
            
            "plot_sequence" to emptyList(), // Foundation skill
            "conflict_identification" to listOf("plot_sequence"),
            "foreshadowing" to listOf("plot_sequence", "making_inferences"),
            "resolution_analysis" to listOf("plot_sequence", "conflict_identification"),
            
            "identifying_theme" to listOf("plot_sequence", "character_traits"),
            "symbolism" to listOf("identifying_theme", "making_inferences"),
            "mood_and_tone" to listOf("figurative_language"),
            
            // Reading Informational progressions
            "main_idea_informational" to emptyList(), // Foundation skill
            "text_structure_informational" to listOf("main_idea_informational"),
            "summarizing_info" to listOf("main_idea_informational", "text_structure_informational"),
            
            "fact_vs_opinion" to emptyList(), // Foundation skill
            "evidence_evaluation" to listOf("fact_vs_opinion"),
            "bias_detection" to listOf("fact_vs_opinion", "evidence_evaluation"),
            "credibility_assessment" to listOf("evidence_evaluation", "bias_detection"),
            
            // Writing progressions
            "paragraph_structure" to emptyList(), // Foundation skill
            "introduction_writing" to listOf("paragraph_structure"),
            "conclusion_writing" to listOf("paragraph_structure"),
            "transition_usage" to listOf("paragraph_structure"),
            
            "narrative_writing" to listOf("paragraph_structure", "sentence_variety"),
            "opinion_writing" to listOf("paragraph_structure", "evidence_integration"),
            "informative_writing" to listOf("paragraph_structure", "transition_usage"),
            
            // Language/Grammar progressions
            "parts_of_speech" to emptyList(), // Foundation skill
            "sentence_types" to listOf("parts_of_speech"),
            "subject_verb_agreement" to listOf("parts_of_speech"),
            "verb_tenses" to listOf("parts_of_speech"),
            
            // Vocabulary/Speaking progressions
            "context_clues" to emptyList(), // Foundation skill
            "word_relationships" to listOf("context_clues"),
            "academic_vocabulary" to listOf("context_clues", "word_relationships")
        )
    }
    
    /**
     * Get a specific skill prompt for a given grade
     */
    fun getSkillPromptForGrade(skillCode: String, gradeLevel: Int): String? {
        val skill = getSkillById(skillCode)
        return skill?.prompts?.get(gradeLevel)
    }
    
    /**
     * Get all skills for a specific category
     */
    fun getSkillsByCategory(category: String): List<SkillData> {
        return allSkills.filter { it.category == category }
    }
    
    /**
     * Check if a grade level has prompts
     */
    fun hasPromptsForGrade(gradeLevel: Int): Boolean {
        return gradeLevel in 2..12
    }
}