package com.brother.crm.lead;

import com.brother.crm.common.ApiResponse;
import com.brother.crm.entity.Lead;
import com.brother.crm.entity.LeadFollowup;
import com.brother.crm.entity.Student;
import com.brother.crm.student.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

  private final LeadService leadService;

  @GetMapping
  public ApiResponse<List<Lead>> list() {
    return ApiResponse.ok(leadService.list());
  }

  @PostMapping
  public ApiResponse<Lead> create(@Valid @RequestBody LeadService.CreateLeadRequest request) {
    return ApiResponse.ok(leadService.create(request));
  }

  @PostMapping("/import")
  public ApiResponse<LeadService.ImportResult> importRows(
      @RequestBody List<LeadService.ImportLeadRow> rows) {
    return ApiResponse.ok(leadService.importRows(rows));
  }

  @PutMapping("/{id}")
  public ApiResponse<Lead> update(
      @PathVariable Long id, @Valid @RequestBody LeadService.UpdateLeadRequest request) {
    return ApiResponse.ok(leadService.update(id, request));
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    return ApiResponse.ok(leadService.detail(id));
  }

  @PostMapping("/{id}/followups")
  public ApiResponse<LeadFollowup> addFollowup(
      @PathVariable Long id, @Valid @RequestBody LeadService.FollowupRequest request) {
    return ApiResponse.ok(leadService.addFollowup(id, request));
  }

  @PatchMapping("/{id}/status")
  public ApiResponse<Lead> changeStatus(
      @PathVariable Long id, @Valid @RequestBody LeadService.StatusRequest request) {
    return ApiResponse.ok(leadService.changeStatus(id, request));
  }

  @PostMapping("/{id}/convert")
  public ApiResponse<Student> convert(
      @PathVariable Long id, @Valid @RequestBody StudentService.ConvertRequest request) {
    return ApiResponse.ok(leadService.convert(id, request));
  }
}
