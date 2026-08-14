package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import com.kkday.model.companion.QuizGalleryResult
import org.koin.core.annotation.Factory

@Factory
class GetQuizGalleryUseCase(
    private val repository: CompanionRepository
) {
    suspend operator fun invoke(): Result<QuizGalleryResult> = repository.getQuizGallery()
}
