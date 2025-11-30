package com.linknest.backend.userpreferences;

import com.linknest.backend.userpreferences.domain.DefaultBookmarkSort;
import com.linknest.backend.userpreferences.domain.DefaultLayout;
import com.linknest.backend.userpreferences.dto.UserPreferencesRes;
import com.linknest.backend.userpreferences.dto.UserPreferencesUpdateReq;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPreferencesMapper {
    // UpdateReq -> Entity (null 값은 무시)
    default void updateFromDto(UserPreferencesUpdateReq req, @MappingTarget UserPreferences userPreferences) {
        if(req.defaultBookmarkSort() != null) {
            userPreferences.setDefaultBookmarkSort(
                    DefaultBookmarkSort.valueOf(req.defaultBookmarkSort()));
        }

        if(req.defaultLayout() != null) {
            userPreferences.setDefaultLayout(
                    DefaultLayout.valueOf(req.defaultLayout()));
        }

        if(req.openInNewTab() != null) {
            userPreferences.setOpenInNewTab(req.openInNewTab());
        }

        if(req.keepSignedIn() != null) {
            userPreferences.setKeepSignedIn(req.keepSignedIn());
        }
    }

    // Entity -> Res
    default UserPreferencesRes toRes(UserPreferences userPreferences) {
        return new UserPreferencesRes(
                userPreferences.getDefaultBookmarkSort().name(),
                userPreferences.getDefaultLayout().name(),
                userPreferences.isOpenInNewTab(),
                userPreferences.isOpenInNewTab()
        );
    }
}
