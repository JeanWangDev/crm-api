package com.brother.crm.travel;
import com.brother.crm.auth.AuthSupport;
import com.brother.crm.auth.LoginUser;
import com.brother.crm.common.BizException;
import com.brother.crm.entity.PickupStatus;
import com.brother.crm.entity.Student;
import com.brother.crm.entity.TrafficType;
import com.brother.crm.entity.TravelInfo;
import com.brother.crm.repository.StudentRepository;
import com.brother.crm.repository.TravelInfoRepository;
import com.brother.crm.student.StudentService;
import com.brother.crm.user.NameLookup;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TravelService {

  private final TravelInfoRepository travelInfoRepository;
  private final StudentRepository studentRepository;
  private final StudentService studentService;
  private final NameLookup nameLookup;

  public TravelInfo ofStudent(Long studentId) {
    return travelInfoRepository.findByStudentId(studentId).orElse(null);
  }

  public Map<Long, TravelInfo> ofStudents(java.util.Collection<Long> studentIds) {
    if (studentIds == null || studentIds.isEmpty()) {
      return Map.of();
    }
    return travelInfoRepository.findByStudentIdIn(studentIds).stream()
        .collect(java.util.stream.Collectors.toMap(TravelInfo::getStudentId, t -> t, (a, b) -> a));
  }

  @Transactional
  public TravelInfo save(Long studentId, SaveRequest request) {
    Student student = studentService.require(studentId);
    TravelInfo travel =
        travelInfoRepository.findByStudentId(studentId).orElseGet(TravelInfo::new);
    travel.setStudentId(student.getId());
    travel.setArriveDate(request.getArriveDate());
    travel.setArriveTime(request.getArriveTime());
    travel.setTrafficType(request.getTrafficType());
    travel.setNeedPickup(Boolean.TRUE.equals(request.getNeedPickup()));
    travel.setStation(blankToNull(request.getStation()));
    travel.setPickupUserId(request.getPickupUserId());
    travel.setRemark(blankToNull(request.getRemark()));
    if (travel.getNeedPickup()) {
      if (travel.getPickupStatus() == null) {
        travel.setPickupStatus(PickupStatus.WAITING);
      }
    } else {
      travel.setPickupStatus(null);
      travel.setPickupUserId(null);
    }
    travel.setUpdatedAt(Instant.now());
    student.setUpdatedAt(Instant.now());
    studentRepository.save(student);
    return travelInfoRepository.save(travel);
  }

  public List<PickupView> board(LocalDate from, LocalDate to, String station) {
    LoginUser user = AuthSupport.currentUser();
    Map<Long, String> names = nameLookup.names();
    List<PickupView> rows = new ArrayList<>();
    for (TravelInfo travel : travelInfoRepository.findByNeedPickupTrueOrderByArriveDateAscArriveTimeAsc()) {
      if (travel.getArriveDate() != null) {
        if (from != null && travel.getArriveDate().isBefore(from)) continue;
        if (to != null && travel.getArriveDate().isAfter(to)) continue;
      }
      if (station != null && !station.isBlank() && !station.equals(travel.getStation())) {
        continue;
      }
      Student student = studentRepository.findById(travel.getStudentId()).orElse(null);
      if (student == null) continue;
      if (!canSee(user, student, travel)) continue;
      PickupView view = new PickupView();
      view.setId(travel.getId());
      view.setStudentId(student.getId());
      view.setStudentName(student.getName());
      view.setPhone(student.getPhone());
      view.setArriveDate(travel.getArriveDate());
      view.setArriveTime(travel.getArriveTime());
      view.setTrafficType(travel.getTrafficType() == null ? null : travel.getTrafficType().name());
      view.setStation(travel.getStation());
      view.setPickupUserId(travel.getPickupUserId());
      view.setPickupUserName(names.get(travel.getPickupUserId()));
      view.setPickupStatus(travel.getPickupStatus() == null ? null : travel.getPickupStatus().name());
      view.setRemark(travel.getRemark());
      rows.add(view);
    }
    return rows;
  }

  @Transactional
  public TravelInfo mark(Long studentId, PickupStatus status) {
    LoginUser user = AuthSupport.currentUser();
    Student student =
        studentRepository.findById(studentId).orElseThrow(() -> new BizException("学员不存在"));
    TravelInfo travel =
        travelInfoRepository
            .findByStudentId(studentId)
            .orElseThrow(() -> new BizException("还没有行程"));
    if (!canSee(user, student, travel)) {
      throw new BizException("无权操作该接站记录");
    }
    if (!Boolean.TRUE.equals(travel.getNeedPickup())) {
      throw new BizException("该学员不需要接站");
    }
    travel.setPickupStatus(status);
    travel.setUpdatedAt(Instant.now());
    return travelInfoRepository.save(travel);
  }

  private boolean canSee(LoginUser user, Student student, TravelInfo travel) {
    return user.isAdmin()
        || student.getOwnerId().equals(user.getId())
        || user.getId().equals(travel.getPickupUserId());
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }

  @Data
  public static class SaveRequest {
    private LocalDate arriveDate;
    private LocalTime arriveTime;
    private TrafficType trafficType;

    @NotNull(message = "请选择是否接站")
    private Boolean needPickup;

    private String station;
    private Long pickupUserId;
    private String remark;
  }

  @Data
  public static class PickupView {
    private Long id;
    private Long studentId;
    private String studentName;
    private String phone;
    private LocalDate arriveDate;
    private LocalTime arriveTime;
    private String trafficType;
    private String station;
    private Long pickupUserId;
    private String pickupUserName;
    private String pickupStatus;
    private String remark;
  }
}
