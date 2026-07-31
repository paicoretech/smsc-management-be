package com.smsc.management.app.user.model.entity;

import com.smsc.management.app.provider.model.entity.ServiceProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_service_provider", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "service_provider_id" })
})
public class UserServiceProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    public UserServiceProvider(Users user, ServiceProvider serviceProvider) {
        this.user = user;
        this.serviceProvider = serviceProvider;
    }
}
