package com.spectrum.workfolio.utils

import com.spectrum.workfolio.domain.enums.ErrorCode

class WorkfolioException(message: String, val errorCode: ErrorCode? = null) : RuntimeException(message)
