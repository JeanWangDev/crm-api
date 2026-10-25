package com.brother.crm.student;

import com.brother.crm.common.ApiResponse;
import com.brother.crm.dorm.DormService;
import com.brother.crm.entity.Student;
import com.brother.crm.entity.TravelInfo;
import com.brother.crm.travel.TravelService;
import com.brother.crm.user.NameLookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkins")
@RequiredArgsConstructor
public class CheckinController {

  private final StudentService studentService;
  private final TravelService travelService;
  private final DormService dormService;
  private final NameLookup nameLookup;

  @GetMapping
  public ApiResponse<List<CheckinRow>> list() {
    Map<Long, String> names = nameLookup.names();
    List<Student> students = studentService.visible();
    List<Long> studentIds = students.stream().map(Student::getId).toList();
    Map<Long, TravelInfo> travels = travelService.ofStudents(studentIds);
    Map<Long, DormService.BedPlace> beds = dormService.placesByStudentIds(studentIds);
    List<CheckinRow> rows = new ArrayList<>();
    for (Student student : students) {
      TravelInfo travel = travels.get(student.getId());
      DormService.BedPlace bed = beds.get(student.getId());
      CheckinRow row = new CheckinRow();
      row.setStudentId(student.getId());
      row.setName(student.getName());
      row.setPhone(student.getPhone());
      row.setGender(student.getGender() == null ? null : student.getGender().name());
      row.setPlanJoinDate(student.getPlanJoinDate() == null ? null : student.getPlanJoinDate().toString());
      row.setStudentStatus(student.getStudentStatus().name());
      row.setCheckedInAt(student.getCheckedInAt() == null ? null : student.getCheckedInAt().toString());
      row.setOwnerName(names.get(student.getOwnerId()));
      if (travel != null) {
        row.setArriveDate(travel.getArriveDate() == null ? null : travel.getArriveDate().toString());
        row.setArriveTime(travel.getArriveTime() == null ? null : travel.getArriveTime().toString());
        row.setTrafficType(travel.getTrafficType() == null ? null : travel.getTrafficType().name());
      }
      if (bed != null) {
        row.setBedLabel(bed.getLabel());
        row.setBedId(bed.getBedId());
      }
      rows.add(row);
    }
    return ApiResponse.ok(rows);
  }

  @Data
  public static class CheckinRow {
    private Long studentId;
    private String name;
    private String phone;
    private String gender;
    private String planJoinDate;
    private String studentStatus;
    private String checkedInAt;
    private String ownerName;
    private String arriveDate;
    private String arriveTime;
    private String trafficType;
    private String bedLabel;
    private Long bedId;
  }
}
