package com.spectrum.workfolio.domain.dto

import com.spectrum.workfolio.domain.entity.Staff
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomStaffDetails(
    private val staff: Staff,
) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_STAFF"))
    }

    override fun getPassword(): String {
        return ""
    }

    override fun getUsername(): String {
        return staff.id
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return staff.isActive
    }
}

