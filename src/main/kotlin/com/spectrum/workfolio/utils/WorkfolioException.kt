package com.spectrum.workfolio.utils

import com.spectrum.workfolio.domain.model.ErrorCode

class WorkfolioException(message: String, val errorCode: ErrorCode? = null) : RuntimeException(message)
