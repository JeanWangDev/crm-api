package com.brother.crm.student;
import com.brother.crm.common.ApiResponse;
import com.brother.crm.dorm.DormService;
import com.brother.crm.entity.Student;
import com.brother.crm.entity.StudentFollowup;
import com.brother.crm.entity.TravelInfo;
import com.brother.crm.travel.TravelService;
import com.brother.crm.user.NameLookup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

  private final StudentService studentService;
  private final TravelService travelService;
  private final DormService dormService;
  private final NameLookup nameLookup;

  @GetMapping
  public ApiResponse<List<Student>> list() {
    List<Student> students = studentService.visible();
    Map<Long, String> names = nameLookup.names();
    students.forEach(student -> student.setOwnerName(names.get(student.getOwnerId())));
    return ApiResponse.ok(students);
  }

  @GetMapping("/{id}")
  public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
    Student student = studentService.require(id);
    student.setOwnerName(nameLookup.names().get(student.getOwnerId()));
    TravelInfo travel = travelService.ofStudent(id);
    DormService.BedPlace bed = dormService.ofStudent(id);
    return ApiResponse.ok(
        Map.of(
            "student", student,
            "followups", studentService.followups(id),
            "travel", travel == null ? Map.of() : travel,
            "bed", bed == null ? Map.of() : bed));
  }

  @PostMapping("/{id}/checkin")
  public ApiResponse<Student> checkIn(@PathVariable Long id) {
    return ApiResponse.ok(studentService.checkIn(id));
  }

  @PostMapping("/{id}/checkin/cancel")
  public ApiResponse<Student> cancelCheckIn(@PathVariable Long id) {
    return ApiResponse.ok(studentService.cancelCheckIn(id));
  }

  @PutMapping("/{id}")
  public ApiResponse<Student> update(
      @PathVariable Long id, @Valid @RequestBody StudentService.UpdateRequest request) {
    return ApiResponse.ok(studentService.update(id, request));
  }

  @PostMapping("/{id}/followups")
  public ApiResponse<StudentFollowup> followup(
      @PathVariable Long id, @Valid @RequestBody FollowupRequest request) {
    return ApiResponse.ok(studentService.addFollowup(id, request.getContent()));
  }

  @PutMapping("/{id}/travel")
  public ApiResponse<TravelInfo> saveTravel(
      @PathVariable Long id, @Valid @RequestBody TravelService.SaveRequest request) {
    return ApiResponse.ok(travelService.save(id, request));
  }

  @Data
  public static class FollowupRequest {
    @NotBlank(message = "请填写跟进内容")
    private String content;
  }
}
