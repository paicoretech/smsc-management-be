package com.smsc.management.app.user.model.entity;

import com.smsc.management.app.user.model.converter.SenderIdConverter;
import com.smsc.management.utils.EntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.FetchType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.smsc.management.utils.Constants.ENABLED;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "user_code_id_seq", sequenceName = "user_code_id_seq", allocationSize = 1)
public class Users extends EntityBase implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_code_id_seq")
    private Integer id;
    private String name;
    private String lastName;
    @Column(unique = true, nullable = false)
    private String userName;
    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    private Short status;
    @Column(columnDefinition = "bool NOT NULL DEFAULT true")
    private boolean mustChangePassword = true;
    @Column(columnDefinition = "bool NOT NULL DEFAULT false")
    private boolean login = false;

    @Column(name = "sender_id", length = 500)
    @Convert(converter = SenderIdConverter.class)
    private List<String> senderIds = new ArrayList<>();

    @Column(columnDefinition = "bool NOT NULL DEFAULT false")
    private boolean allServiceProviders = false;

    @Column(columnDefinition = "int NOT NULL DEFAULT 0")
    private Integer failedLoginAttempts = 0;

    @Column(columnDefinition = "bool NOT NULL DEFAULT false")
    private boolean accountLocked = false;

    private LocalDateTime lockTime;

    private LocalDateTime lastFailedLoginTime;

    private String jwt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == ENABLED;
    }
}
