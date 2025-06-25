package com.studywise.ai.domain.usecase

import com.studywise.ai.data.local.dao.QuestionDao
import com.studywise.ai.data.local.dao.SkillDao
import com.studywise.ai.data.local.entity.QuestionEntity
import com.studywise.ai.data.local.entity.SkillCategory
import com.studywise.ai.data.local.entity.SkillEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import com.google.common.truth.Truth.assertThat

class GetQuestionsUseCaseTest {

    @Mock
    private lateinit var questionDao: QuestionDao

    @Mock
    private lateinit var skillDao: SkillDao

    private lateinit var getQuestionsUseCase: GetQuestionsUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getQuestionsUseCase = GetQuestionsUseCase(questionDao, skillDao)
    }

    @Test
    fun `getQuestionsForSkill returns questions with skill name`() = runBlocking {
        // Given
        val userId = "user123"
        val skillId = "lit_character_traits"
        val gradeLevel = 4
        val skill = SkillEntity(
            id = skillId,
            category = SkillCategory.READING_LITERATURE,
            name = "Character Traits",
            description = "Test description",
            orderIndex = 0
        )
        val questions = listOf(
            QuestionEntity(
                id = "q1",
                skillId = skillId,
                gradeLevel = gradeLevel,
                prompt = "Test question 1",
                hints = "[\"Hint 1\", \"Hint 2\"]",
                followUpQuestions = null,
                skillSubCategory = null
            )
        )

        whenever(skillDao.getSkillById(skillId)).thenReturn(skill)
        whenever(questionDao.getUnaskedQuestions(userId, skillId, gradeLevel, 5))
            .thenReturn(questions)

        // When
        val result = getQuestionsUseCase.getQuestionsForSkill(userId, skillId, gradeLevel, 5)

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].skillName).isEqualTo("Character Traits")
        assertThat(result[0].hints).hasSize(2)
        assertThat(result[0].hints[0]).isEqualTo("Hint 1")
    }
}