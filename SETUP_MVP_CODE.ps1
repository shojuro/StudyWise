# StudyWise MVP Code Generator
# This script creates the initial code files for MVP development

param(
    [string]$ProjectPath = "C:\DEV\StudyWise-Fresh"
)

Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "StudyWise MVP Code Generator" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $ProjectPath

# Base package path
$basePackage = "app\src\main\java\com\studywise\ai"

# Create StudyWiseApplication
$appContent = @'
package com.studywise.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class StudyWiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
'@
$appContent | Out-File -FilePath "$basePackage\StudyWiseApplication.kt" -Encoding UTF8

# Create User domain model
$userModelContent = @'
package com.studywise.ai.domain.model

import java.util.Date

data class User(
    val id: String,
    val name: String,
    val email: String,
    val grade: Int,
    val createdAt: Date
)
'@
$userModelContent | Out-File -FilePath "$basePackage\domain\model\User.kt" -Encoding UTF8

# Create Lesson domain model
$lessonModelContent = @'
package com.studywise.ai.domain.model

data class Lesson(
    val id: String,
    val title: String,
    val subject: String,
    val gradeLevel: Int,
    val content: String,
    val questions: List<Question>,
    val duration: Int,
    val points: Int
)

data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)
'@
$lessonModelContent | Out-File -FilePath "$basePackage\domain\model\Lesson.kt" -Encoding UTF8

# Create Subject model
$subjectModelContent = @'
package com.studywise.ai.domain.model

data class Subject(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val lessonCount: Int
)
'@
$subjectModelContent | Out-File -FilePath "$basePackage\domain\model\Subject.kt" -Encoding UTF8

# Create UserEntity
$userEntityContent = @'
package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val grade: Int,
    val createdAt: Date
)
'@
$userEntityContent | Out-File -FilePath "$basePackage\data\local\entity\UserEntity.kt" -Encoding UTF8

# Create LessonEntity
$lessonEntityContent = @'
package com.studywise.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val gradeLevel: Int,
    val content: String,
    val questionsJson: String,
    val duration: Int,
    val points: Int
)
'@
$lessonEntityContent | Out-File -FilePath "$basePackage\data\local\entity\LessonEntity.kt" -Encoding UTF8

# Create UserDao
$userDaoContent = @'
package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?
    
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)
    
    @Delete
    suspend fun deleteUser(user: UserEntity)
}
'@
$userDaoContent | Out-File -FilePath "$basePackage\data\local\dao\UserDao.kt" -Encoding UTF8

# Create LessonDao
$lessonDaoContent = @'
package com.studywise.ai.data.local.dao

import androidx.room.*
import com.studywise.ai.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons")
    fun getAllLessons(): Flow<List<LessonEntity>>
    
    @Query("SELECT * FROM lessons WHERE subject = :subject")
    fun getLessonsBySubject(subject: String): Flow<List<LessonEntity>>
    
    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: String): LessonEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)
}
'@
$lessonDaoContent | Out-File -FilePath "$basePackage\data\local\dao\LessonDao.kt" -Encoding UTF8

# Create Database
$databaseContent = @'
package com.studywise.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.studywise.ai.data.local.converter.Converters
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.dao.LessonDao
import com.studywise.ai.data.local.entity.UserEntity
import com.studywise.ai.data.local.entity.LessonEntity

@Database(
    entities = [UserEntity::class, LessonEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StudyWiseDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
}
'@
$databaseContent | Out-File -FilePath "$basePackage\data\local\database\StudyWiseDatabase.kt" -Encoding UTF8

# Create Converters
$convertersContent = @'
package com.studywise.ai.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
'@
New-Item -ItemType Directory -Path "$basePackage\data\local\converter" -Force | Out-Null
$convertersContent | Out-File -FilePath "$basePackage\data\local\converter\Converters.kt" -Encoding UTF8

# Create DatabaseModule
$dbModuleContent = @'
package com.studywise.ai.di

import android.content.Context
import androidx.room.Room
import com.studywise.ai.data.local.database.StudyWiseDatabase
import com.studywise.ai.data.local.dao.UserDao
import com.studywise.ai.data.local.dao.LessonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StudyWiseDatabase {
        return Room.databaseBuilder(
            context,
            StudyWiseDatabase::class.java,
            "studywise_database"
        ).fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideUserDao(database: StudyWiseDatabase): UserDao = database.userDao()
    
    @Provides
    fun provideLessonDao(database: StudyWiseDatabase): LessonDao = database.lessonDao()
}
'@
$dbModuleContent | Out-File -FilePath "$basePackage\di\DatabaseModule.kt" -Encoding UTF8

# Create MainActivity
$mainActivityContent = @'
package com.studywise.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.studywise.ai.presentation.navigation.StudyWiseNavigation
import com.studywise.ai.presentation.theme.StudyWiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyWiseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudyWiseNavigation()
                }
            }
        }
    }
}
'@
$mainActivityContent | Out-File -FilePath "$basePackage\MainActivity.kt" -Encoding UTF8

# Create Navigation
$navigationContent = @'
package com.studywise.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun StudyWiseNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            // TODO: Add LoginScreen
        }
        
        composable("home") {
            // TODO: Add HomeScreen
        }
        
        composable("lesson/{lessonId}") {
            // TODO: Add LessonScreen
        }
    }
}
'@
$navigationContent | Out-File -FilePath "$basePackage\presentation\navigation\StudyWiseNavigation.kt" -Encoding UTF8

# Create Theme
$themeContent = @'
package com.studywise.ai.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF1976D2),
    secondary = androidx.compose.ui.graphics.Color(0xFF4CAF50),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFF9800)
)

@Composable
fun StudyWiseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
'@
$themeContent | Out-File -FilePath "$basePackage\presentation\theme\Theme.kt" -Encoding UTF8

Write-Host "✅ MVP code structure created!" -ForegroundColor Green
Write-Host ""
Write-Host "Created files:" -ForegroundColor Yellow
Write-Host "- StudyWiseApplication.kt" -ForegroundColor Gray
Write-Host "- Domain models (User, Lesson, Subject)" -ForegroundColor Gray
Write-Host "- Entities (UserEntity, LessonEntity)" -ForegroundColor Gray
Write-Host "- DAOs (UserDao, LessonDao)" -ForegroundColor Gray
Write-Host "- StudyWiseDatabase.kt" -ForegroundColor Gray
Write-Host "- DatabaseModule.kt" -ForegroundColor Gray
Write-Host "- MainActivity.kt" -ForegroundColor Gray
Write-Host "- Navigation setup" -ForegroundColor Gray
Write-Host "- Theme setup" -ForegroundColor Gray
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Open in Android Studio" -ForegroundColor White
Write-Host "2. Sync Gradle" -ForegroundColor White
Write-Host "3. Start implementing screens" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")