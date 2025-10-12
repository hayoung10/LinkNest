package com.linknest.backend.user;

import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UserUpdateReq updateReq, @MappingTarget User user);

    // Entity -> Res
    UserRes toRes(User user);
}
