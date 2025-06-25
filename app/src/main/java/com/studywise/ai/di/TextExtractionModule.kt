package com.studywise.ai.di

import android.content.ContentResolver
import android.content.Context
import com.studywise.ai.util.textextraction.DocumentTextExtractor
import com.studywise.ai.util.textextraction.PhotoTextExtractor
import com.studywise.ai.util.textextraction.VoiceTextCapture
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object TextExtractionModule {
    
    @Provides
    @ViewModelScoped
    fun providePhotoTextExtractor(
        @ApplicationContext context: Context
    ): PhotoTextExtractor {
        return PhotoTextExtractor(context)
    }
    
    @Provides
    @ViewModelScoped
    fun provideDocumentTextExtractor(
        @ApplicationContext context: Context,
        photoTextExtractor: PhotoTextExtractor
    ): DocumentTextExtractor {
        val contentResolver = context.contentResolver
        return DocumentTextExtractor(contentResolver, photoTextExtractor)
    }
    
    @Provides
    @ViewModelScoped
    fun provideVoiceTextCapture(
        @ApplicationContext context: Context
    ): VoiceTextCapture {
        return VoiceTextCapture(context)
    }
}