package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.primary.Education
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EducationRepository: JpaRepository<Education, String> {
}
