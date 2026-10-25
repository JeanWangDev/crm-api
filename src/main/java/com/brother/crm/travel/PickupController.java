package com.brother.crm.travel;
import com.brother.crm.common.ApiResponse;
import com.brother.crm.entity.PickupStatus;
import com.brother.crm.entity.TravelInfo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pickups")
@RequiredArgsConstructor
public class PickupController {

  private final TravelService travelService;

  @GetMapping
  public ApiResponse<List<TravelService.PickupView>> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) String station) {
    return ApiResponse.ok(travelService.board(from, to, station));
  }

  @PostMapping("/{studentId}/status")
  public ApiResponse<TravelInfo> status(
      @PathVariable Long studentId, @Valid @RequestBody StatusRequest request) {
    return ApiResponse.ok(travelService.mark(studentId, request.getPickupStatus()));
  }

  @Data
  public static class StatusRequest {
    @NotNull(message = "请选择接站状态")
    private PickupStatus pickupStatus;
  }
}
