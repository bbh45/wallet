package com.example.wallet.wallet;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String country;
    private String dob;
    @NotNull
    private UserIdentifier userIdentifier;
    @NotBlank  //value!=null and value.length>0 : check for - ''(empty string)
    private String identifierValue;

    public User to(){
        return User.builder()
                .name(this.name)
                .phoneNumber(this.phoneNumber)
                .password(this.password)
                .email(this.email)
                .dob(this.dob)
                .country(this.country)
                .userIdentifier(this.userIdentifier)
                .identifierValue(this.identifierValue)
                .build();
    }

}
