package com.linknest.backend.trash;

import com.linknest.backend.common.dto.SliceResponse;
import com.linknest.backend.common.response.ApiResponse;
import com.linknest.backend.trash.domain.TrashType;
import com.linknest.backend.trash.dto.TrashBulkReq;
import com.linknest.backend.trash.dto.TrashItemRes;
import com.linknest.backend.trash.dto.TrashMixedBulkReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.linknest.backend.common.constants.AppConstants.API_PREFIX;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_PREFIX + "/trash")
public class TrashController {
    private final TrashService service;

    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<TrashItemRes>>> list(@AuthenticationPrincipal(expression = "id") Long userId,
                                                                        @RequestParam(required = false) TrashType type,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size) {
        SliceResponse<TrashItemRes> data = service.list(userId, type, page, size);
        return ResponseEntity.ok(ApiResponse.ok("휴지통 목록 조회", data));
    }

    @DeleteMapping("/empty")
    public ResponseEntity<ApiResponse<Void>> empty(@AuthenticationPrincipal(expression = "id") Long userId,
                                                   @RequestParam(required = false) TrashType type) {
        service.empty(userId, type);
        String message = (type == null) ? "휴지통 비우기 완료" : "휴지통 비우기 완료(" + type + ")";
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }

    @PostMapping("/{type}/{id}/restore")
    public ResponseEntity<ApiResponse<Void>> restore(@AuthenticationPrincipal(expression = "id") Long userId,
                                                 @PathVariable TrashType type,
                                                 @PathVariable @Min(1) Long id) {
        service.restore(userId, type, id);
        return ResponseEntity.ok(ApiResponse.ok("복구 완료", null));
    }

    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@AuthenticationPrincipal(expression = "id") Long userId,
                                                 @PathVariable TrashType type,
                                                 @PathVariable @Min(1) Long id) {
        service.delete(userId, type, id);
        return ResponseEntity.ok(ApiResponse.ok("영구 삭제 완료", null));
    }

    // =============================
    // Bulk
    // =============================
    @PostMapping("/restore")
    public ResponseEntity<ApiResponse<Void>> restoreMixedBulk(@AuthenticationPrincipal(expression = "id") Long userId,
                                                              @RequestBody @Valid TrashMixedBulkReq req) {
        service.restoreMixedBulk(userId, req.items());
        return ResponseEntity.ok(ApiResponse.ok("선택 항목 복구 완료", null));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteMixedBulk(@AuthenticationPrincipal(expression = "id") Long userId,
                                                             @RequestBody @Valid TrashMixedBulkReq req) {
        service.deleteMixedBulk(userId, req.items());
        return ResponseEntity.ok(ApiResponse.ok("선택 항목 영구 삭제 완료", null));
    }

    @PostMapping("/{type}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreBulk(@AuthenticationPrincipal(expression = "id") Long userId,
                                                     @PathVariable TrashType type,
                                                     @RequestBody @Valid TrashBulkReq req) {
        service.restoreBulk(userId, type, req.ids());
        return ResponseEntity.ok(ApiResponse.ok("선택 항목 복구 완료", null));
    }

    @PostMapping("/{type}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteBulk(@AuthenticationPrincipal(expression = "id") Long userId,
                                                    @PathVariable TrashType type,
                                                    @RequestBody @Valid TrashBulkReq req) {
        service.deleteBulk(userId, type, req.ids());
        return ResponseEntity.ok(ApiResponse.ok("선택 항복 영구 삭제 완료", null));
    }
}
