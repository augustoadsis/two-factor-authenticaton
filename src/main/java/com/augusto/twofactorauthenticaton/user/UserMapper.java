package com.augusto.twofactorauthenticaton.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserDTO user);
    UserDTO toDTO(User user);

}
