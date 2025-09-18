package com.spectrum.workfolio.domain.dto

import com.spectrum.workfolio.domain.entity.history.Company
import com.spectrum.workfolio.domain.entity.primary.Certifications
import com.spectrum.workfolio.domain.entity.primary.Degrees
import com.spectrum.workfolio.domain.entity.primary.Education

data class ExistingData(
    val companies: List<Company>,
    val certifications: List<Certifications>,
    val degrees: List<Degrees>,
    val educations: List<Education>
)
