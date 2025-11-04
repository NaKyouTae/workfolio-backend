package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.TurnOverRetrospective
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TurnOverRetrospectiveRepository : JpaRepository<TurnOverRetrospective, String>
