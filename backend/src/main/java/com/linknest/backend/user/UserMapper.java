package com.linknest.backend.user;

import com.linknest.backend.user.dto.UserRes;
import com.linknest.backend.user.dto.UserUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(UserUpdateReq updateReq, @MappingTarget User user);

    // Entity -> Res
    @Mapping(target = "profileImageUrl", expression = "java(user.getResolvedProfileImageUrl())")
    @Mapping(target = "hasCustomProfileImage", expression = "java(user.hasCustomProfileImage())")
    UserRes toRes(User user);
}
