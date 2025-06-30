package com.studywise.ai.data.local.content

import com.studywise.ai.data.local.content.EducationalContentData.SkillData

/**
 * Reading Informational skills data (Skills 21-40 of 80)
 */
object ReadingInformationalSkills {
    
    val skills = listOf(
        SkillData(
            skillId = "fact_vs_opinion",
            name = "Fact vs. Opinion",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Distinguishing between objective facts and subjective opinions",
            prompts = mapOf(
                2 to "Find something that is true for everyone. Find something that someone thinks.",
                3 to "What's a fact from your text? What's someone's opinion? How are they different?",
                4 to "Find one fact and one opinion in your text. How can you tell the difference between them?",
                5 to "Identify three facts and any opinions in this passage. What words signal that something is an opinion?",
                6 to "Distinguish between factual claims and opinions. How does the author support facts versus opinions?",
                7 to "Analyze how facts and opinions interact in the text. How does the author use facts to support opinions?",
                8 to "Evaluate the balance of fact and opinion. How does the author blur lines between objective and subjective claims?",
                9 to "Analyze how facts and opinions interweave to create persuasive effects. Where do boundaries blur?",
                10 to "Evaluate the construction of 'factuality.' How do rhetorical strategies make opinions appear factual?",
                11 to "Critique the fact/opinion binary. How do disciplinary contexts determine what counts as fact?",
                12 to "Theorize about epistemology and truth claims. How does the text navigate objective/subjective divides?"
            )
        ),
        
        SkillData(
            skillId = "cause_and_effect",
            name = "Cause and Effect",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Identifying causal relationships in texts",
            prompts = mapOf(
                2 to "Find something that happened because of something else. What made it happen?",
                3 to "What caused something to happen in your text? What was the effect?",
                4 to "Find something in your text that happened because of something else. What was the cause and what was the effect?",
                5 to "Identify cause-and-effect relationships in the text. How does the author show these connections?",
                6 to "Trace multiple cause-and-effect relationships. How do chain reactions or multiple causes create complex effects?",
                7 to "Analyze both explicit and implicit cause-effect relationships. What long-term effects does the author predict or imply?",
                8 to "Evaluate the validity of cause-effect claims. How does the author address alternative explanations or confounding factors?",
                9 to "Examine complex causation beyond simple relationships. How do multiple causes create overdetermined effects?",
                10 to "Analyze how causal arguments obscure agency or responsibility. What gets naturalized through causal logic?",
                11 to "Critique deterministic versus probabilistic causal claims. How does the text handle uncertainty?",
                12 to "Theorize about causation as narrative construction. How do causal stories shape understanding?"
            )
        ),
        
        SkillData(
            skillId = "compare_contrast_info",
            name = "Compare and Contrast (Informational)",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Analyzing similarities and differences in informational texts",
            prompts = mapOf(
                2 to "What two things does the text talk about? How are they the same or different?",
                3 to "Find two things the author compares. List what's alike and what's different.",
                4 to "What two things does the author compare in this text? List two ways they are alike and two ways they are different.",
                5 to "How does the author compare and contrast ideas or concepts? Create categories for similarities and differences.",
                6 to "Analyze comparison and contrast relationships. What criteria does the author use for comparison?",
                7 to "Examine how comparisons advance the author's argument. What subtle differences become significant through comparison?",
                8 to "Evaluate the effectiveness and fairness of comparisons. How might bias influence what's compared and how?",
                9 to "Analyze comparison as argumentative strategy. How do selected comparisons advance specific claims?",
                10 to "Evaluate the politics of comparison. What power dynamics emerge through comparative frameworks?",
                11 to "Critique comparison criteria and their assumptions. How do frameworks predetermine conclusions?",
                12 to "Theorize about comparison as knowledge production. How do analogies create or limit understanding?"
            )
        ),
        
        SkillData(
            skillId = "technical_vocabulary",
            name = "Technical Vocabulary",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Understanding domain-specific terminology",
            prompts = mapOf(
                2 to "Find a new word about the topic. What do the other words help you learn about it?",
                3 to "Find three special words about this topic. How does the text help you understand them?",
                4 to "Find three special words related to this topic. How does the text help you understand what they mean?",
                5 to "Identify domain-specific vocabulary. How does context or the text's features help you determine meanings?",
                6 to "Analyze how technical vocabulary is introduced and developed. Why are precise terms important for this topic?",
                7 to "Examine how specialized vocabulary shapes understanding. How do technical terms carry specific meanings different from everyday use?",
                8 to "Evaluate how technical language affects accessibility and precision. When does jargon clarify versus obscure meaning?",
                9 to "Examine how technical language establishes authority. When does precision clarify versus exclude?",
                10 to "Analyze jargon as community boundary-marking. How does specialized language create insider/outsider dynamics?",
                11 to "Critique technical vocabulary's ideological functions. How do terms encode disciplinary assumptions?",
                12 to "Theorize about language as constitutive of reality. How do technical terms create the phenomena they describe?"
            )
        ),
        
        SkillData(
            skillId = "text_features",
            name = "Text Features",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Using headings, graphics, and other features to understand text",
            prompts = mapOf(
                2 to "Look at the pictures, labels, or bold words. How do they help you understand?",
                3 to "What text features do you see (headings, captions, diagrams)? Pick one and tell how it helps.",
                4 to "What text features (headings, bold words, pictures, captions) help you understand this text? Pick one and explain how it helps.",
                5 to "How do text features guide your reading? Choose two features and explain what information they add.",
                6 to "Analyze how text features work together to convey information. Which features are most essential to understanding?",
                7 to "Examine how text features enhance or extend the main text. What would be lost without these elements?",
                8 to "Evaluate the strategic use of text features. How do they guide reader attention and interpretation?",
                9 to "Analyze how visual and textual features create hierarchies of attention. What gets emphasized or minimized?",
                10 to "Evaluate the rhetoric of document design. How do features guide interpretation beyond information delivery?",
                11 to "Critique text features as genre conventions. How do disciplinary expectations shape presentation?",
                12 to "Synthesize multimodal theory with feature analysis. How do different modes create meaning together?"
            )
        ),
        
        SkillData(
            skillId = "author_purpose_info",
            name = "Author's Purpose (Informational)",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Determining why the author wrote informational text",
            prompts = mapOf(
                2 to "Is the author teaching you something or trying to get you to do something?",
                3 to "Why did the author write this? To teach, to convince, or to explain?",
                4 to "Why did the author write this text? Do they want to teach you, convince you, or explain something?",
                5 to "What is the author's purpose? Find evidence that shows whether they aim to inform, persuade, or explain.",
                6 to "Determine the author's purpose and analyze how they achieve it. What techniques support their goal?",
                7 to "Analyze multiple purposes within the text. How does the author balance informing with persuading?",
                8 to "Evaluate how effectively the author achieves their stated and unstated purposes. What agendas might influence the text?",
                9 to "Analyze layered purposes beyond stated intentions. What institutional or ideological purposes operate?",
                10 to "Evaluate how purpose shapes evidence selection and presentation. What gets included or excluded?",
                11 to "Critique the rhetoric of objectivity. How do authors mask persuasive purposes as neutral information?",
                12 to "Theorize about discourse communities and purposes. How do disciplinary contexts shape communicative goals?"
            )
        ),
        
        SkillData(
            skillId = "summarizing_info",
            name = "Summarizing (Informational)",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Creating concise summaries of informational texts",
            prompts = mapOf(
                2 to "Tell someone the most important thing you learned in three sentences.",
                3 to "What are the three most important things from this text? Say them in your own words.",
                4 to "In your own words, what are the three most important things you learned from this text?",
                5 to "Summarize the main points of this passage in order. What information is essential versus interesting but less important?",
                6 to "Create a concise summary that captures central ideas and key supporting details. What can you leave out while maintaining accuracy?",
                7 to "Synthesize information into a summary that maintains the author's emphasis. How do you preserve meaning while condensing?",
                8 to "Produce an objective summary that distinguishes between essential and supplementary information. How do you avoid bias in summarizing?",
                9 to "Create summaries that preserve complexity while achieving concision. What tensions emerge in reduction?",
                10 to "Analyze how summary choices reflect interpretive frameworks. What gets prioritized and why?",
                11 to "Critique summary as transformation rather than condensation. How does summary create new texts?",
                12 to "Theorize about representation and loss. What necessarily disappears in any summary?"
            )
        ),
        
        SkillData(
            skillId = "evidence_evaluation",
            name = "Evidence Evaluation",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Assessing the quality and relevance of evidence",
            prompts = mapOf(
                2 to "What examples does the author give? Which one helps you understand best?",
                3 to "Find facts the author uses to explain the main idea. Which fact is most helpful?",
                4 to "What examples or facts does the author use to support their main point? Which one do you think is strongest?",
                5 to "Identify the evidence the author provides. How does each piece of evidence support the main idea?",
                6 to "Analyze the types and quality of evidence presented. What makes evidence strong or weak for this argument?",
                7 to "Evaluate the sufficiency and relevance of evidence. What additional evidence would strengthen the argument?",
                8 to "Critique the evidence's validity and reliability. How do source, method, and presentation affect credibility?",
                9 to "Analyze evidence quality using disciplinary standards. What constitutes strong evidence in this field?",
                10 to "Evaluate evidence selection bias. What evidence might contradict the argument but remains unmentioned?",
                11 to "Critique evidence hierarchies. Why does certain evidence carry more weight than other forms?",
                12 to "Theorize about evidence and proof. How do different epistemologies define sufficient evidence?"
            )
        ),
        
        SkillData(
            skillId = "making_inferences_info",
            name = "Making Inferences (Informational)",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Drawing conclusions from informational texts",
            prompts = mapOf(
                2 to "What else can you figure out from the facts? What do they make you think?",
                3 to "Based on the information, what else must be true? What clues helped you know?",
                4 to "Based on the facts given, what else can you figure out that the author doesn't directly say?",
                5 to "What conclusions can you draw from the information presented? What clues lead to these inferences?",
                6 to "Make inferences by connecting explicit information to implied meanings. What must be true based on the facts?",
                7 to "Analyze what the author implies through selection and presentation of information. What assumptions underlie the text?",
                8 to "Evaluate inferences for validity and examine what the text suggests versus proves. How do biases shape implications?",
                9 to "Draw inferences about unstated assumptions and values. What worldview enables the explicit claims?",
                10 to "Analyze inferential leaps and their validity. Where does the text rely on reader assumptions?",
                11 to "Critique the cultural knowledge required for inference. What communities can access implied meanings?",
                12 to "Theorize about implication and meaning. How do texts communicate beyond their literal content?"
            )
        ),
        
        SkillData(
            skillId = "visual_information",
            name = "Visual Information",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Interpreting charts, graphs, and images",
            prompts = mapOf(
                2 to "Look at the pictures or charts. What do they teach you that the words don't?",
                3 to "Study a diagram, chart, or picture. What extra information does it give you?",
                4 to "Look at the charts, graphs, or pictures in your text. What information do they show that the words don't tell you?",
                5 to "How do visual elements (graphs, diagrams, images) add to your understanding? Explain what you learn from one visual.",
                6 to "Analyze how visual elements complement or extend written information. What patterns or relationships become clearer through visuals?",
                7 to "Examine how visuals and text work together to build meaning. What might be misleading about the visual representations?",
                8 to "Evaluate the integration of visual and textual information. How do design choices influence interpretation of data?",
                9 to "Analyze how visuals argue rather than merely illustrate. What claims do images make independently?",
                10 to "Evaluate visual rhetoric and its persuasive strategies. How do design choices shape interpretation?",
                11 to "Critique visual representation ethics. What do visualization choices reveal or conceal?",
                12 to "Synthesize visual theory with critical analysis. How do images construct knowledge differently than text?"
            )
        ),
        
        SkillData(
            skillId = "argument_analysis",
            name = "Argument Analysis",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Examining claims, reasoning, and evidence",
            prompts = mapOf(
                2 to "What does the author want you to believe? How do they try to convince you?",
                3 to "What is the author's opinion? What reasons do they give to support it?",
                4 to "What is the author trying to convince you about? What reasons do they give to support their idea?",
                5 to "Identify the author's claim and supporting reasons. How does each reason connect to the main argument?",
                6 to "Trace the development of the author's argument. How do claims, reasons, and evidence build upon each other?",
                7 to "Analyze argument structure and logic. Where is the reasoning strong or weak? What counterarguments are addressed?",
                8 to "Evaluate argument effectiveness and identify logical fallacies. How do rhetorical strategies strengthen or weaken the case?",
                9 to "Map argument structure including warrants and backing. What unstated premises connect evidence to claims?",
                10 to "Analyze argumentative strategies beyond logic. How do emotional and ethical appeals function?",
                11 to "Critique argument within rhetorical traditions. How does the argument reflect classical or contemporary approaches?",
                12 to "Theorize about argumentation and truth. How do arguments create rather than discover knowledge?"
            )
        ),
        
        SkillData(
            skillId = "multiple_sources",
            name = "Multiple Sources",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Comparing and synthesizing information from multiple texts",
            prompts = mapOf(
                2 to "If you read two texts about the same thing, what new did you learn from each?",
                3 to "Compare two texts on the same topic. What does each one teach you?",
                4 to "If you've read two texts about the same topic, what new information did you learn from the second one?",
                5 to "Compare information from two sources on this topic. What do both sources agree on? What's different?",
                6 to "Integrate information from multiple sources. How do different perspectives contribute to complete understanding?",
                7 to "Synthesize information from multiple sources to build comprehensive knowledge. Where do sources conflict and why?",
                8 to "Evaluate how multiple sources complement or contradict each other. How do you reconcile conflicting information?",
                9 to "Synthesize sources while preserving their distinctiveness. How do different perspectives complicate understanding?",
                10 to "Analyze source relationships and power dynamics. Whose voices dominate synthesis and why?",
                11 to "Critique source selection and representation. What sources remain excluded from conversation?",
                12 to "Theorize about knowledge as conversation. How do sources constitute discourse communities?"
            )
        ),
        
        SkillData(
            skillId = "bias_detection",
            name = "Bias Detection",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Identifying author bias and perspective",
            prompts = mapOf(
                2 to "Does the author like or not like what they're writing about? How can you tell?",
                3 to "What does the author think about this topic? What words show their feelings?",
                4 to "Does the author seem to favor one side of the topic? What words or examples show their feelings?",
                5 to "Look for signs of the author's point of view. What language choices reveal their perspective on the topic?",
                6 to "Identify potential bias in the text. How might the author's background or purpose influence the information presented?",
                7 to "Analyze how bias manifests through word choice, evidence selection, and omissions. What perspectives are missing?",
                8 to "Evaluate subtle forms of bias and their impact on credibility. How do cultural, political, or economic factors influence perspective?",
                9 to "Examine subtle bias beyond obvious prejudice. How do word choices and structures encode perspectives?",
                10 to "Analyze systemic versus individual bias. How do institutional contexts shape seemingly neutral texts?",
                11 to "Critique the impossibility of unbiased text. How might acknowledging bias strengthen credibility?",
                12 to "Theorize about perspective and situated knowledge. How does standpoint epistemology reframe bias?"
            )
        ),
        
        SkillData(
            skillId = "research_connections",
            name = "Research Connections",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Connecting reading to further inquiry",
            prompts = mapOf(
                2 to "What else do you want to know about this topic? What questions do you have?",
                3 to "What questions does this text answer? What new questions do you have now?",
                4 to "What questions does this text answer for you? What new questions do you have after reading?",
                5 to "How does this text connect to what you already knew? What would you want to research next?",
                6 to "Generate research questions based on the text. What gaps in information would you need to fill for deeper understanding?",
                7 to "Analyze how this text contributes to broader knowledge. What research methods or sources would extend this learning?",
                8 to "Evaluate the text's contribution to the field. What further investigation would validate, challenge, or extend these ideas?",
                9 to "Generate research questions that challenge rather than extend the text. What critical inquiries emerge?",
                10 to "Analyze how texts position themselves within research conversations. What genealogies do they claim?",
                11 to "Critique research paradigms and their limitations. What questions cannot be asked within this framework?",
                12 to "Theorize about knowledge production and power. How do research agendas reflect institutional interests?"
            )
        ),
        
        SkillData(
            skillId = "credibility_assessment",
            name = "Credibility Assessment",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Evaluating source reliability and trustworthiness",
            prompts = mapOf(
                2 to "How do you know this information is true? Who wrote it?",
                3 to "Why should we trust this author? What makes them an expert?",
                4 to "How do you know if you can trust this information? What makes the author an expert on this topic?",
                5 to "What makes this source reliable? Look for clues about the author's expertise and the information's accuracy.",
                6 to "Assess source credibility using multiple criteria. What qualifications, citations, or methods establish trustworthiness?",
                7 to "Analyze credibility markers throughout the text. How do publication context and peer review affect reliability?",
                8 to "Evaluate nuanced credibility factors. How do funding sources, publication dates, and methodology impact trust?",
                9 to "Evaluate credibility using multiple criteria beyond traditional markers. What alternative authorities emerge?",
                10 to "Analyze how credibility is constructed rhetorically. What strategies establish author ethos?",
                11 to "Critique credibility hierarchies. How do credentialing systems privilege certain knowledge?",
                12 to "Theorize about expertise and authority. How do different communities define credible knowledge?"
            )
        ),
        
        SkillData(
            skillId = "problem_solution",
            name = "Problem/Solution",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Identifying problems and evaluating proposed solutions",
            prompts = mapOf(
                2 to "What problem does the text talk about? How can it be fixed?",
                3 to "Find the problem and solution in the text. Would the solution really work?",
                4 to "What problem does this text describe? What solutions does the author suggest?",
                5 to "Identify the problem and proposed solutions. How does the author show that these solutions could work?",
                6 to "Analyze the problem-solution relationship. What evidence supports the viability of proposed solutions?",
                7 to "Examine multiple dimensions of problems and solutions. What trade-offs or unintended consequences are considered?",
                8 to "Evaluate solution feasibility and completeness. How do proposed solutions address root causes versus symptoms?",
                9 to "Examine how problem definition shapes solution possibilities. What framings foreclose alternatives?",
                10 to "Analyze the politics of problem-solution frameworks. Who benefits from proposed solutions?",
                11 to "Critique solutionism as ideology. When do solutions obscure systemic issues?",
                12 to "Theorize about problems as constructions. How do texts create the problems they solve?"
            )
        ),
        
        SkillData(
            skillId = "sequential_procedures",
            name = "Sequential Procedures",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Understanding step-by-step processes",
            prompts = mapOf(
                2 to "If this tells how to do something, what comes first, next, and last?",
                3 to "List the steps in order. What would happen if you mixed them up?",
                4 to "If this text explains how to do something, what are the main steps? Why is the order important?",
                5 to "Trace the sequence of steps or events. What would happen if you changed the order?",
                6 to "Analyze procedural or chronological sequences. How do transitions and time markers guide understanding?",
                7 to "Examine how sequential information builds complexity. Where are critical decision points or dependencies?",
                8 to "Evaluate the clarity and completeness of sequential information. What expertise is assumed versus explicitly stated?",
                9 to "Analyze procedural texts as power structures. How do instructions encode authority relationships?",
                10 to "Evaluate the assumptions embedded in procedures. What knowledge or resources do they presume?",
                11 to "Critique standardization and its effects. How do procedures normalize particular approaches?",
                12 to "Theorize about procedure as control mechanism. How do sequential frameworks shape possibilities?"
            )
        ),
        
        SkillData(
            skillId = "synthesizing_info",
            name = "Synthesizing Information",
            category = EducationalContentData.READING_INFORMATIONAL,
            description = "Combining information to create new understanding",
            prompts = mapOf(
                2 to "Using everything you read, what's the most important thing to remember?",
                3 to "Put all the information together. What's the big idea you learned?",
                4 to "Using everything you learned from this text, what's the most important thing to remember?",
                5 to "Combine different pieces of information from the text to draw a conclusion. How do the parts create a whole?",
                6 to "Synthesize information across sections to form comprehensive understanding. What connections weren't explicitly stated?",
                7 to "Create new insights by synthesizing diverse information. How do patterns emerge from seemingly separate facts?",
                8 to "Generate original conclusions through synthesis. How does integrating this information change prior understanding?",
                9 to "Create synthesis that generates new insights rather than summarizing. What emerges through connection?",
                10 to "Analyze synthesis as creative act. How do you maintain source integrity while creating new meaning?",
                11 to "Critique synthesis as potential appropriation. When does synthesis erase original contexts?",
                12 to "Theorize about knowledge as emergent. How does synthesis reveal systemic patterns?"
            )
        )
    )
}