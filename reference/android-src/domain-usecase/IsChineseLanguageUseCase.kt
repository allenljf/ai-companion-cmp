package com.kkday.domain.companion

import com.kkday.library.common.repository.CompanionRepository
import org.koin.core.annotation.Factory

@Factory
class IsChineseLanguageUseCase(
    private val repository: CompanionRepository
) {
    operator fun invoke(): Boolean = repository.isChineseLanguage()
}
