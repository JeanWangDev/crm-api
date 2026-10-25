package com.brother.crm.student;
import com.brother.crm.auth.AuthSupport;
import com.brother.crm.auth.LoginUser;
import com.brother.crm.common.BizException;
import com.brother.crm.entity.Gender;
import com.brother.crm.entity.Lead;
import com.brother.crm.entity.LeadStatus;
import com.brother.crm.entity.Student;
import com.brother.crm.entity.StudentFollowup;
import com.brother.crm.entity.StudentStatus;
import com.brother.crm.repository.LeadRepository;
import com.brother.crm.repository.StudentFollowupRepository;
import com.brother.crm.repository.StudentRepository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final StudentRepository studentRepository;
  private final StudentFollowupRepository followupRepository;
  private final LeadRepository leadRepository;

  public List<Student> visible() {
    LoginUser user = AuthSupport.currentUser();
    if (user.isAdmin()) {
      return studentRepository.findAllByOrderByUpdatedAtDesc();
    }
    return studentRepository.findByOwnerIdOrderByUpdatedAtDesc(user.getId());
  }

  public Student find(Long id) {
    return studentRepository.findById(id).orElse(null);
  }

  public Map<Long, Student> mapByIds(Collection<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return Map.of();
    }
    return studentRepository.findAllById(ids).stream()
        .collect(Collectors.toMap(Student::getId, Function.identity(), (a, b) -> a));
  }

  public Student require(Long id) {
    LoginUser user = AuthSupport.currentUser();
    Student student =
        studentRepository.findById(id).orElseThrow(() -> new BizException("学员不存在"));
    if (!user.isAdmin() && !student.getOwnerId().equals(user.getId())) {
      throw new BizException("无权查看该学员");
    }
    return student;
  }

  public List<StudentFollowup> followups(Long studentId) {
    return followupRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
  }

  @Transactional
  public Student convert(Lead lead, ConvertRequest request) {
    if (lead.getStatus() == LeadStatus.CONVERTED) {
      throw new BizException("该线索已经转成学员");
    }
    if (lead.getStatus() == LeadStatus.DROPPED) {
      throw new BizException("已放弃的线索不能转学员，请先改回跟进中");
    }
    Instant now = Instant.now();
    Student student = new Student();
    student.setOwnerId(lead.getOwnerId());
    student.setFromLeadId(lead.getId());
    student.setName(text(request.getName(), lead.getStudentName()));
    student.setPhone(text(request.getPhone(), lead.getStudentPhone()));
    student.setGender(request.getGender());
    student.setHomeAddress(request.getHomeAddress().trim());
    student.setEmergencyName(request.getEmergencyName().trim());
    student.setEmergencyPhone(request.getEmergencyPhone().trim());
    student.setPlanJoinDate(request.getPlanJoinDate());
    student.setStudentStatus(StudentStatus.WAITING);
    student.setCreatedAt(now);
    student.setUpdatedAt(now);
    studentRepository.save(student);

    lead.setStatus(LeadStatus.CONVERTED);
    lead.setConvertedStudentId(student.getId());
    lead.setUpdatedAt(now);
    leadRepository.save(lead);
    return student;
  }

  @Transactional
  public Student update(Long id, UpdateRequest request) {
    Student student = require(id);
    student.setName(request.getName().trim());
    student.setPhone(request.getPhone().trim());
    if (request.getGender() != null) {
      student.setGender(request.getGender());
    }
    student.setHomeAddress(request.getHomeAddress().trim());
    student.setEmergencyName(request.getEmergencyName().trim());
    student.setEmergencyPhone(request.getEmergencyPhone().trim());
    student.setPlanJoinDate(request.getPlanJoinDate());
    if (request.getStudentStatus() != null) {
      student.setStudentStatus(request.getStudentStatus());
    }
    student.setUpdatedAt(Instant.now());
    return studentRepository.save(student);
  }

  @Transactional
  public StudentFollowup addFollowup(Long id, String content) {
    Student student = require(id);
    StudentFollowup followup = new StudentFollowup();
    followup.setStudentId(student.getId());
    followup.setContent(content.trim());
    followup.setOperatorId(AuthSupport.currentUser().getId());
    followup.setCreatedAt(Instant.now());
    student.setUpdatedAt(Instant.now());
    studentRepository.save(student);
    return followupRepository.save(followup);
  }

  @Transactional
  public Student checkIn(Long id) {
    Student student = require(id);
    student.setStudentStatus(StudentStatus.ENROLLED);
    student.setCheckedInAt(Instant.now());
    student.setUpdatedAt(Instant.now());
    return studentRepository.save(student);
  }

  @Transactional
  public Student cancelCheckIn(Long id) {
    Student student = require(id);
    student.setStudentStatus(StudentStatus.WAITING);
    student.setCheckedInAt(null);
    student.setUpdatedAt(Instant.now());
    return studentRepository.save(student);
  }

  private String text(String preferred, String fallback) {
    if (preferred == null || preferred.isBlank()) {
      return fallback;
    }
    return preferred.trim();
  }

  @Data
  public static class ConvertRequest {
    private String name;
    private String phone;

    @NotNull(message = "请选择性别")
    private Gender gender;

    @NotBlank(message = "请填写家庭住址")
    private String homeAddress;

    @NotBlank(message = "请填写紧急联系人")
    private String emergencyName;

    @NotBlank(message = "请填写紧急联系电话")
    private String emergencyPhone;

    @NotNull(message = "请填写计划入学日期")
    private LocalDate planJoinDate;
  }

  @Data
  public static class UpdateRequest {
    @NotBlank(message = "请填写姓名")
    private String name;

    @NotBlank(message = "请填写电话")
    private String phone;

    @NotNull(message = "请选择性别")
    private Gender gender;

    @NotBlank(message = "请填写家庭住址")
    private String homeAddress;

    @NotBlank(message = "请填写紧急联系人")
    private String emergencyName;

    @NotBlank(message = "请填写紧急联系电话")
    private String emergencyPhone;

    @NotNull(message = "请填写计划入学日期")
    private LocalDate planJoinDate;

    private StudentStatus studentStatus;
  }
}
