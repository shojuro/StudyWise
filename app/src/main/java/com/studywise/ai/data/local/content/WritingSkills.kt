package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.content.EducationalContentData.SkillData

/**
 * Writing skills data (Skills 41-60 of 80)
 */
object WritingSkills {
    
    val skills = listOf(
        SkillData(
            skillId = "narrative_writing",
            name = "Narrative Writing",
            category = EducationalContentData.WRITING,
            description = "Creating stories with characters, setting, and plot",
            prompts = mapOf(
                2 to "Write about something that happened to you. Tell what happened first, next, and last.",
                3 to "Tell a story with a beginning, middle, and end. Add details so readers can picture it.",
                4 to "Write a story about a time something surprising happened. Include what happened first, next, and last. How did you feel?",
                5 to "Create a narrative with a clear beginning, middle, and end. Use descriptive details to help readers picture the events.",
                6 to "Develop a narrative using techniques like dialogue, pacing, and description. How can you make readers feel present in the moment?",
                7 to "Craft a narrative that uses multiple techniques to engage readers. How can you control pacing and build toward a meaningful conclusion?",
                8 to "Construct a sophisticated narrative that weaves together plot, character development, and theme. How do stylistic choices enhance meaning?",
                9 to "Craft narratives that experiment with time, perspective, and reliability. How can form enhance meaning?",
                10 to "Develop narratives that interrogate the construction of truth and memory. What stories resist simple telling?",
                11 to "Create narratives that engage with literary traditions while finding original voice. How do you honor and innovate?",
                12 to "Compose narratives that function as philosophical or political arguments. How can story create theory?"
            )
        ),
        
        SkillData(
            skillId = "opinion_writing",
            name = "Opinion/Argumentative Writing",
            category = EducationalContentData.WRITING,
            description = "Expressing and supporting opinions with evidence",
            prompts = mapOf(
                2 to "What do you think about something? Give two reasons why you think this.",
                3 to "Share your opinion and give three reasons. Use 'because' to explain why.",
                4 to "What's your opinion about something important to you? Give three reasons why you feel this way.",
                5 to "State your opinion clearly and support it with facts and examples. How can you organize reasons from most to least important?",
                6 to "Develop an argument with clear claims and relevant evidence. How do you address what others might say against your opinion?",
                7 to "Construct a logical argument with claims, counterclaims, and evidence. How do transitions clarify relationships between ideas?",
                8 to "Build a nuanced argument that acknowledges complexity. How do you establish credibility while addressing multiple perspectives?",
                9 to "Construct nuanced arguments that acknowledge complexity and counter-arguments. How do you maintain position while showing sophistication?",
                10 to "Develop arguments that examine underlying assumptions and values. What beliefs enable your claims?",
                11 to "Create arguments that engage with scholarly discourse. How do you enter academic conversations?",
                12 to "Compose arguments that theorize rather than merely persuade. How can argument generate new understanding?"
            )
        ),
        
        SkillData(
            skillId = "informative_writing",
            name = "Informative Writing",
            category = EducationalContentData.WRITING,
            description = "Explaining topics clearly with facts and details",
            prompts = mapOf(
                2 to "Teach someone about something you know. What do they need to learn?",
                3 to "Explain a topic you know well. Include facts and examples to help readers understand.",
                4 to "Explain how to do something you know well. What steps should someone follow? What do they need to know?",
                5 to "Write to teach others about a topic. How can you organize information clearly with main ideas and supporting details?",
                6 to "Develop an informative piece using multiple organizational strategies. How do formatting and graphics enhance understanding?",
                7 to "Create comprehensive informative text that anticipates reader needs. How do you maintain objectivity while engaging interest?",
                8 to "Produce sophisticated expository writing that synthesizes complex information. How do you balance depth with accessibility?",
                9 to "Produce informative texts that acknowledge the construction of knowledge. How do you inform while showing perspective?",
                10 to "Develop expositions that handle complex, contested information. How do you represent disagreement fairly?",
                11 to "Create informative writing that synthesizes multiple disciplinary perspectives. How do fields see differently?",
                12 to "Compose informative texts that question the possibility of neutral information. How does all writing argue?"
            )
        ),
        
        SkillData(
            skillId = "descriptive_writing",
            name = "Descriptive Writing",
            category = EducationalContentData.WRITING,
            description = "Using sensory details to paint pictures with words",
            prompts = mapOf(
                2 to "Describe something using words about how it looks, sounds, feels, smells, or tastes.",
                3 to "Paint a picture with words. Use describing words to help readers see what you see.",
                4 to "Describe a place or object using all five senses. What words help readers see, hear, smell, taste, or feel it?",
                5 to "Create vivid descriptions using specific details and figurative language. How can you show instead of just telling?",
                6 to "Develop rich descriptions that serve a purpose in your writing. When do descriptions slow pace versus enhance meaning?",
                7 to "Craft descriptions that reveal character, advance plot, or establish mood. How do word choices create specific effects?",
                8 to "Master descriptive techniques that work symbolically and literally. How can description carry thematic weight?",
                9 to "Craft descriptions that function symbolically and literally. How can concrete details carry abstract meaning?",
                10 to "Develop descriptive writing that challenges perception. How do you defamiliarize the familiar?",
                11 to "Create descriptions that engage with cultural ways of seeing. How does identity shape observation?",
                12 to "Compose descriptions that theorize the act of looking. What are the ethics of description?"
            )
        ),
        
        SkillData(
            skillId = "paragraph_structure",
            name = "Paragraph Structure",
            category = EducationalContentData.WRITING,
            description = "Organizing ideas into coherent paragraphs",
            prompts = mapOf(
                2 to "Write three sentences about one idea. Make them go together.",
                3 to "Write a paragraph with a main idea sentence and three detail sentences.",
                4 to "Write a paragraph with a topic sentence, three detail sentences, and a closing sentence. How do all parts work together?",
                5 to "Construct paragraphs where each sentence connects to the main idea. How do transition words link thoughts?",
                6 to "Develop cohesive paragraphs with varied sentence structures. How does sentence variety improve flow?",
                7 to "Create sophisticated paragraphs that build complex ideas. How do you balance unity with development?",
                8 to "Master paragraph construction for different purposes. How do paragraph patterns change across genres?",
                9 to "Construct paragraphs that balance cohesion with complexity. How do you maintain focus while developing nuance?",
                10 to "Develop paragraphs that use structure rhetorically. How can paragraph shape create emphasis?",
                11 to "Create paragraphs that reflect disciplinary conventions while maintaining voice. When do you conform or resist?",
                12 to "Compose paragraphs that challenge traditional unity. When might fragmentation serve your purpose?"
            )
        ),
        
        SkillData(
            skillId = "introduction_writing",
            name = "Introduction Writing",
            category = EducationalContentData.WRITING,
            description = "Creating engaging openings that preview content",
            prompts = mapOf(
                2 to "Start your writing with something interesting. Make readers want more!",
                3 to "Write an opening sentence that grabs attention. What will make readers curious?",
                4 to "Write an opening that makes readers want to know more. What interesting fact or question can you start with?",
                5 to "Create introductions that preview your main ideas. How can you hook readers while setting up your topic?",
                6 to "Develop engaging introductions suited to purpose and audience. What strategies work for different types of writing?",
                7 to "Craft introductions that establish context and significance. How do you balance intrigue with clarity?",
                8 to "Master various introduction techniques for impact. How do opening choices frame reader expectations?",
                9 to "Craft introductions that establish stakes and complexity. How do you show why your topic matters?",
                10 to "Develop introductions that position your work within larger conversations. What contexts frame your writing?",
                11 to "Create introductions that establish theoretical or methodological frameworks. How do you signal approach?",
                12 to "Compose introductions that problematize rather than simplify. How can openings unsettle assumptions?"
            )
        ),
        
        SkillData(
            skillId = "conclusion_writing",
            name = "Conclusion Writing",
            category = EducationalContentData.WRITING,
            description = "Creating satisfying endings that reinforce main ideas",
            prompts = mapOf(
                2 to "End your writing with a good last sentence. Wrap it up!",
                3 to "Write an ending that feels finished. Remind readers of your main point.",
                4 to "End your writing in a satisfying way. How can you remind readers of your main point without just repeating?",
                5 to "Write conclusions that give readers something to think about. What final thought or call to action fits your purpose?",
                6 to "Create conclusions that synthesize rather than summarize. How can you leave lasting impressions?",
                7 to "Develop conclusions that extend thinking beyond the text. What implications or connections enhance closure?",
                8 to "Craft sophisticated conclusions that resonate thematically. How do circular or evolved endings affect meaning?",
                9 to "Create conclusions that open outward rather than close down. What new questions emerge from your work?",
                10 to "Develop conclusions that acknowledge limitations while asserting significance. How do you balance humility with confidence?",
                11 to "Craft conclusions that gesture toward implications and applications. Where does your thinking lead?",
                12 to "Compose conclusions that resist closure. How can endings maintain productive ambiguity?"
            )
        ),
        
        SkillData(
            skillId = "revision_strategies",
            name = "Revision Strategies",
            category = EducationalContentData.WRITING,
            description = "Improving writing through systematic revision",
            prompts = mapOf(
                2 to "Read your writing out loud. What parts need more words? What's confusing?",
                3 to "Make your writing better. Add details, fix confusing parts, and check order.",
                4 to "Read your writing aloud. What parts sound confusing? Where could you add more details?",
                5 to "Review your draft for clarity and completeness. Which sentences could be combined or split for better flow?",
                6 to "Revise for specific elements: idea development, organization, word choice. What patterns of weakness emerge?",
                7 to "Apply systematic revision for style and substance. How do changes at sentence level affect overall coherence?",
                8 to "Execute sophisticated revision considering multiple dimensions. How do micro and macro changes interact?",
                9 to "Revise for conceptual coherence beyond surface clarity. How do you strengthen the deep logic of your work?",
                10 to "Approach revision as reimagining rather than correcting. What new possibilities emerge in rethinking?",
                11 to "Revise with attention to disciplinary expectations and personal voice. How do you negotiate these tensions?",
                12 to "Theorize revision as endless process. When is a text 'finished' and what does that mean?"
            )
        ),
        
        SkillData(
            skillId = "editing_skills",
            name = "Editing Skills",
            category = EducationalContentData.WRITING,
            description = "Correcting errors in grammar, spelling, and punctuation",
            prompts = mapOf(
                2 to "Check for capitals at the start and periods at the end. Circle words to check spelling.",
                3 to "Fix mistakes in capitals, periods, and spelling. Read each sentence carefully.",
                4 to "Check your writing for capitals, periods, and spelling. What tools can help you find and fix mistakes?",
                5 to "Edit for grammar, punctuation, and spelling patterns. Which errors do you make most often?",
                6 to "Apply editing strategies systematically. How do different types of errors affect reader comprehension?",
                7 to "Edit for consistency in style and conventions. When might breaking rules serve your purpose?",
                8 to "Master editing for publication-ready work. How do you maintain voice while meeting standard conventions?",
                9 to "Edit for rhetorical effect beyond correctness. How do mechanical choices create meaning?",
                10 to "Approach editing as fine-tuning voice and rhythm. What patterns need adjustment?",
                11 to "Edit with awareness of convention as choice. When might 'errors' serve purposes?",
                12 to "Consider editing as political act. How do standard conventions privilege certain voices?"
            )
        ),
        
        SkillData(
            skillId = "audience_awareness",
            name = "Audience Awareness",
            category = EducationalContentData.WRITING,
            description = "Writing appropriately for different readers",
            prompts = mapOf(
                2 to "Who will read this? What do they need to know?",
                3 to "Think about your reader. Are you writing for kids or adults? Change your words to fit.",
                4 to "Who will read your writing? What do they need to know? What words will they understand?",
                5 to "Adjust your writing for different audiences. How does writing for peers differ from writing for adults?",
                6 to "Analyze audience needs and expectations. What background knowledge can you assume versus must explain?",
                7 to "Adapt sophisticated strategies for diverse audiences. How do tone, evidence, and structure shift with purpose?",
                8 to "Master audience analysis for complex rhetorical situations. How do you address multiple audiences simultaneously?",
                9 to "Write for multiple, complex audiences simultaneously. How do you layer accessibility with sophistication?",
                10 to "Analyze how imagined audiences shape possibilities. What can't be said to whom?",
                11 to "Navigate academic and public audiences. How do you translate without condescending?",
                12 to "Theorize audience as co-creator of meaning. How do readers complete texts?"
            )
        ),
        
        SkillData(
            skillId = "voice_development",
            name = "Voice Development",
            category = EducationalContentData.WRITING,
            description = "Creating a distinctive writing style",
            prompts = mapOf(
                2 to "Make your writing sound like you! What words do you like to use?",
                3 to "Let your personality show. Write like you're talking to a friend.",
                4 to "Let your personality show in your writing. What words or phrases sound like you?",
                5 to "Develop a consistent voice that fits your purpose. How can you sound confident and interesting?",
                6 to "Cultivate distinct voices for different genres. What makes academic voice different from creative voice?",
                7 to "Refine voice for authenticity and impact. How do syntax and diction choices create voice?",
                8 to "Master voice modulation across contexts. How do you maintain authenticity while meeting genre expectations?",
                9 to "Cultivate a voice that balances authority with openness. How do you sound knowledgeable but not dogmatic?",
                10 to "Develop voices appropriate to different contexts. How do you maintain authenticity across registers?",
                11 to "Create voice that engages with academic discourse while remaining distinctive. How do you join without disappearing?",
                12 to "Theorize voice as performance and construct. What does 'authentic voice' mean?"
            )
        ),
        
        SkillData(
            skillId = "transition_usage",
            name = "Transition Usage",
            category = EducationalContentData.WRITING,
            description = "Connecting ideas smoothly within and between paragraphs",
            prompts = mapOf(
                2 to "Use words like 'first,' 'next,' and 'last' to connect your ideas.",
                3 to "Connect your sentences with words like 'also,' 'but,' and 'because.'",
                4 to "Use words like 'first,' 'next,' 'then,' and 'finally' to connect ideas. Where does your writing need these bridges?",
                5 to "Connect paragraphs and ideas with varied transitions. What relationships between ideas need clarifying?",
                6 to "Employ transitions that show complex relationships. How do transitions signal comparison, cause, or contradiction?",
                7 to "Use sophisticated transitions for coherence. How can transitions do more than just connect?",
                8 to "Master subtle transitions and flow. When are explicit transitions necessary versus intrusive?",
                9 to "Deploy transitions that show complex relationships beyond sequence. How do you signal nuanced connections?",
                10 to "Use transitions to control pacing and emphasis. When do you want smooth flow versus productive friction?",
                11 to "Employ transitions that reflect logical relationships precisely. How do you show causation versus correlation?",
                12 to "Consider when to refuse transitions. How might gaps and jumps serve your purpose?"
            )
        ),
        
        SkillData(
            skillId = "evidence_integration",
            name = "Evidence Integration",
            category = EducationalContentData.WRITING,
            description = "Supporting ideas with examples and evidence",
            prompts = mapOf(
                2 to "Give an example to show what you mean. Tell why it's important.",
                3 to "Add facts or examples to support your ideas. Explain how they help.",
                4 to "Add examples to support your ideas. How can you explain why each example matters?",
                5 to "Include facts, examples, or experiences as evidence. How do you introduce and explain evidence clearly?",
                6 to "Integrate evidence smoothly into your arguments. What's the balance between evidence and analysis?",
                7 to "Synthesize multiple sources of evidence effectively. How do you maintain your voice while incorporating others'?",
                8 to "Master sophisticated evidence integration. How do you evaluate and present conflicting evidence?",
                9 to "Integrate evidence that complicates rather than simply supports. How do you handle contradictory data?",
                10 to "Synthesize multiple types of evidence seamlessly. How do you balance personal, textual, and empirical support?",
                11 to "Position evidence within scholarly conversations. How do you show awareness of how others use similar evidence?",
                12 to "Theorize the nature of evidence itself. What counts as evidence and who decides?"
            )
        ),
        
        SkillData(
            skillId = "sentence_variety",
            name = "Sentence Variety",
            category = EducationalContentData.WRITING,
            description = "Using different sentence structures for rhythm and emphasis",
            prompts = mapOf(
                2 to "Make some sentences short. Make some sentences longer. Mix them up!",
                3 to "Use different kinds of sentences. Some short, some long, some questions!",
                4 to "Mix short and long sentences in your writing. How does this make your writing more interesting to read?",
                5 to "Vary sentence beginnings and structures. What different ways can you start sentences?",
                6 to "Create rhythm through deliberate sentence variety. How do sentence patterns affect pacing and emphasis?",
                7 to "Craft sentence variety for specific effects. When do parallel structures versus varied structures work best?",
                8 to "Master syntactic variety as a stylistic tool. How do sentence choices reflect and enhance meaning?",
                9 to "Vary sentences for rhetorical purpose beyond style. How does syntax create emphasis and meaning?",
                10 to "Use sentence patterns to control reading experience. When do you want acceleration versus contemplation?",
                11 to "Deploy sentences that reflect content complexity. How does form mirror conceptual sophistication?",
                12 to "Experiment with sentences as philosophical statements. How can syntax embody ideas?"
            )
        ),
        
        SkillData(
            skillId = "word_choice",
            name = "Word Choice",
            category = EducationalContentData.WRITING,
            description = "Selecting precise and effective vocabulary",
            prompts = mapOf(
                2 to "Change boring words like 'good' and 'said' to more interesting ones.",
                3 to "Pick the best words for your writing. Use strong verbs and colorful adjectives.",
                4 to "Replace boring words with more interesting ones. What specific words paint clearer pictures?",
                5 to "Choose precise words that match your meaning. How do word connotations affect your message?",
                6 to "Select words strategically for audience and purpose. When are simple words better than complex ones?",
                7 to "Employ diction that enhances tone and meaning. How do word origins and associations add layers?",
                8 to "Master nuanced word choice for subtle effects. How do individual word decisions accumulate into style?",
                9 to "Select words for precision and connotation simultaneously. How do you balance accuracy with effect?",
                10 to "Choose words that acknowledge their histories and associations. What cultural work do words perform?",
                11 to "Navigate technical and accessible vocabulary. When does precision require specialized terms?",
                12 to "Consider word choice as ethical decision. How do naming and describing create realities?"
            )
        ),
        
        SkillData(
            skillId = "dialogue_writing",
            name = "Dialogue Writing",
            category = EducationalContentData.WRITING,
            description = "Creating realistic conversations in writing",
            prompts = mapOf(
                2 to "Make characters talk. Use quotation marks to show their words.",
                3 to "Write what characters say. Show who's talking without always using 'said.'",
                4 to "Write a conversation between two characters. How can you show who's talking without always saying 'he said'?",
                5 to "Create realistic dialogue that reveals character. What makes written conversation sound natural?",
                6 to "Develop dialogue that advances plot and reveals personality. How do speech patterns differ between characters?",
                7 to "Craft dialogue with subtext and authenticity. What remains unspoken and why?",
                8 to "Master dialogue as a multifunctional tool. How does dialogue carry theme while maintaining realism?",
                9 to "Create dialogue that reveals character through indirection. What emerges in what's not said?",
                10 to "Develop dialogue that captures power dynamics and social positions. How does speech encode status?",
                11 to "Write dialogue that balances authenticity with artfulness. When do you sacrifice realism for effect?",
                12 to "Theorize dialogue as site of identity construction. How do characters create themselves through speech?"
            )
        ),
        
        SkillData(
            skillId = "research_writing",
            name = "Research Writing",
            category = EducationalContentData.WRITING,
            description = "Incorporating information from sources",
            prompts = mapOf(
                2 to "Write about something you learned from a book. Tell where you learned it.",
                3 to "Share facts you found in books or online. Tell readers where the facts came from.",
                4 to "Write about something you learned from books or websites. How do you tell readers where information came from?",
                5 to "Combine information from multiple sources in your writing. How do you put ideas in your own words?",
                6 to "Synthesize research into cohesive writing. How do you balance source material with your own analysis?",
                7 to "Integrate research seamlessly while maintaining academic integrity. What citation style fits your purpose?",
                8 to "Produce sophisticated research-based writing. How do you enter scholarly conversations through writing?",
                9 to "Produce research writing that synthesizes while maintaining source integrity. How do you create new knowledge?",
                10 to "Develop research writing that acknowledges its own limitations and biases. What can't your research capture?",
                11 to "Create research writing that engages with methodological questions. How does method shape findings?",
                12 to "Compose research writing that questions research itself. What are the politics of knowledge production?"
            )
        ),
        
        SkillData(
            skillId = "persuasive_techniques",
            name = "Persuasive Techniques",
            category = EducationalContentData.WRITING,
            description = "Using strategies to convince readers",
            prompts = mapOf(
                2 to "Try to get someone to agree with you. What will make them say yes?",
                3 to "Convince someone of your idea. Use strong reasons and examples.",
                4 to "Convince someone to agree with you. What reasons and examples will change their mind?",
                5 to "Use facts and feelings to persuade readers. How do different types of appeals work together?",
                6 to "Apply logical, emotional, and ethical appeals strategically. Which techniques fit your audience?",
                7 to "Develop sophisticated persuasive strategies. How do you build credibility while acknowledging complexity?",
                8 to "Master rhetorical techniques for maximum impact. How do you persuade without manipulating?",
                9 to "Deploy persuasion that acknowledges manipulation risks. How do you influence ethically?",
                10 to "Develop persuasive strategies for resistant audiences. How do you open closed minds?",
                11 to "Create persuasion that works through logic, emotion, and ethics simultaneously. How do appeals interact?",
                12 to "Theorize persuasion as collaborative meaning-making. How is all communication persuasive?"
            )
        ),
        
        SkillData(
            skillId = "creative_techniques",
            name = "Creative Techniques",
            category = EducationalContentData.WRITING,
            description = "Using literary devices to enhance writing",
            prompts = mapOf(
                2 to "Make your writing fun! Add sound words or comparing words.",
                3 to "Use creative touches like rhyme, repetition, or comparisons to make writing sparkle.",
                4 to "Add creative touches like comparisons or sound words to your writing. What makes writing fun to read?",
                5 to "Use literary devices like similes, metaphors, or repetition. How do these techniques affect readers?",
                6 to "Employ creative techniques purposefully. When do literary devices enhance versus distract?",
                7 to "Integrate sophisticated creative elements. How do technique and meaning interconnect?",
                8 to "Master creative techniques across genres. How do you innovate within conventions?",
                9 to "Employ creative techniques that serve conceptual purposes. When does innovation clarify ideas?",
                10 to "Develop creative approaches that challenge genre boundaries. How do hybrid forms create new possibilities?",
                11 to "Use creative techniques that engage with literary traditions. How do you signal awareness while innovating?",
                12 to "Theorize creativity as cultural practice. What enables and constrains innovation?"
            )
        ),
        
        SkillData(
            skillId = "reflection_writing",
            name = "Reflection Writing",
            category = EducationalContentData.WRITING,
            description = "Writing about personal growth and learning",
            prompts = mapOf(
                2 to "Write about something you learned. How did you feel about it?",
                3 to "Reflect on an experience. What did you learn? How did you change?",
                4 to "Write about what you learned or how you changed. What examples show your growth?",
                5 to "Reflect on experiences or learning with specific details. How do you connect events to insights?",
                6 to "Develop reflective writing that explores significance. What deeper meanings emerge from experience?",
                7 to "Create sophisticated reflections that analyze growth. How do you balance narrative with analysis?",
                8 to "Master reflective writing that synthesizes experience and knowledge. How does reflection lead to new understanding?",
                9 to "Create reflections that analyze process beyond narrating it. What patterns emerge in your thinking?",
                10 to "Develop reflective writing that connects personal experience to larger contexts. How does individual illuminate systemic?",
                11 to "Produce reflections that theorize learning itself. What does it mean to understand?",
                12 to "Compose reflections that question the possibility of self-knowledge. How do we know what we know?"
            )
        )
    )
}