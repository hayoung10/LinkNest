package com.linknest.backend.userpreferences;

import com.linknest.backend.userpreferences.dto.UserPreferencesRes;
import com.linknest.backend.userpreferences.dto.UserPreferencesUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPreferencesMapper {
    // UpdateReq -> Entity (null 값은 무시)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "defaultBookmarkSort",
            expression = "java(req.defaultBookmarkSort() != null ? com.linknest.backend.userpreferences.domain.DefaultBookmarkSort.valueOf(req.defaultBookmarkSort()) : null)")
    @Mapping(target = "defaultLayout",
            expression = "java(req.defaultLayout() != null ? com.linknest.backend.userpreferences.domain.DefaultLayout.valueOf(req.defaultLayout()) : null)")
    void updateFromDto(UserPreferencesUpdateReq req, @MappingTarget UserPreferences userPreferences);

    // Entity -> Res
    @Mapping(target = "defaultBookmarkSort", expression = "java(userPreferences.getDefaultBookmarkSort().name())")
    @Mapping(target = "defaultLayout", expression = "java(userPreferences.getDefaultLayout().name())")
    UserPreferencesRes toRes(UserPreferences userPreferences);
}
