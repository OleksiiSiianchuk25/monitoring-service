package com.ajlekc.monitoringservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String internalId;

    @JsonProperty("id")
    private Integer externalId;

    private String name;
    private String username;
    private String email;

    private Address address;

    private String phone;
    private String website;

    private Company company;
}
