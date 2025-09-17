package com.spectrum.workfolio.redis.repository

import com.spectrum.workfolio.redis.model.RedisBlackAccessToken
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RedisBlackAccessTokenRepository : CrudRepository<RedisBlackAccessToken, String>
