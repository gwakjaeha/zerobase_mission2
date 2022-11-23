package com.example.account.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class AccountUser extends BaseEntity {
    private String name;
}
