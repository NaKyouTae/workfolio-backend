package com.spectrum.workfolio.domain.dto

import com.spectrum.workfolio.domain.entity.resume.Career
import com.spectrum.workfolio.domain.entity.resume.Certifications
import com.spectrum.workfolio.domain.entity.resume.Degrees
import com.spectrum.workfolio.domain.entity.resume.Education

data class ExistingData(
    val companies: List<Career>,
    val certifications: List<Certifications>,
    val degrees: List<Degrees>,
    val educations: List<Education>,
)
