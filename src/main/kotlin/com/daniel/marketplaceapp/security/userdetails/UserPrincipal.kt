package com.daniel.marketplaceapp.security.userdetails

import com.daniel.marketplaceapp.user.domain.User
import java.util.Collections
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val user: User,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return Collections.singletonList(SimpleGrantedAuthority("USER"))
    }

    override fun getPassword(): String? {
        return user.passwordHash
    }

    override fun getUsername(): String {
        return user.username
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
        return true
    }
}
