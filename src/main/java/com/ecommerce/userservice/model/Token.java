package com.ecommerce.userservice.model;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@AllArgsConstructor
@Entity
@NoArgsConstructor

public class Token extends Base{

    private String tokenValue;
    private Date expiryAt;

    @ManyToOne
    private User user;
}
