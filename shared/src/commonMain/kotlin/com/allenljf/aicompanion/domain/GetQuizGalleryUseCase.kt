package com.allenljf.aicompanion.domain

import com.allenljf.aicompanion.data.CompanionRepository
import com.allenljf.aicompanion.model.QuizGalleryResult

class GetQuizGalleryUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<QuizGalleryResult> = repository.getQuizGallery()
}
