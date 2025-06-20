package com.challenge.nauta_challenge.infrastructure.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(
    val id: Long, val userName: String, val pass: String,
    val auths: Collection<GrantedAuthority>
) : UserDetails {

    override fun getUsername(): String {
        return userName
    }

    override fun getPassword(): String {
        return pass
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return auths
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }
}