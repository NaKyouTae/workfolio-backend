package com.spectrum.workfolio.redis.repository

import com.spectrum.workfolio.redis.model.RedisRefreshToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisRefreshTokenRepository : CrudRepository<RedisRefreshToken, String>
