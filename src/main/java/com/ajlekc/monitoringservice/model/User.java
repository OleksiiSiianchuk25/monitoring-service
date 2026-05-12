package com.ajlekc.monitoringservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @Id
    private String internalId;

    @JsonProperty("id")
    private Integer externalId;

    @EqualsAndHashCode.Include
    private String name;

    @EqualsAndHashCode.Include
    private String username;

    @EqualsAndHashCode.Include
    private String email;

    @EqualsAndHashCode.Include
    private Address address;

    @EqualsAndHashCode.Include
    private String phone;

    @EqualsAndHashCode.Include
    private String website;

    @EqualsAndHashCode.Include
    private Company company;

}
