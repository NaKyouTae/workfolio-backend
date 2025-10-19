package com.spectrum.workfolio.domain.dto

import com.spectrum.workfolio.domain.entity.resume.Company
import com.spectrum.workfolio.domain.entity.resume.Certifications
import com.spectrum.workfolio.domain.entity.resume.Degrees
import com.spectrum.workfolio.domain.entity.resume.Education

data class ExistingData(
    val companies: List<Company>,
    val certifications: List<Certifications>,
    val degrees: List<Degrees>,
    val educations: List<Education>,
)
