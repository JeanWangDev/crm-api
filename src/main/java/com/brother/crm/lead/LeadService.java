package com.brother.crm.lead;

import com.brother.crm.auth.AuthSupport;
import com.brother.crm.auth.LoginUser;
import com.brother.crm.common.BizException;
import com.brother.crm.entity.Lead;
import com.brother.crm.entity.LeadFollowup;
import com.brother.crm.entity.LeadStatus;
import com.brother.crm.entity.Student;
import com.brother.crm.repository.LeadFollowupRepository;
import com.brother.crm.repository.LeadRepository;
import com.brother.crm.student.StudentService;
import com.brother.crm.user.NameLookup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeadService {

  private final LeadRepository leadRepository;
  private final LeadFollowupRepository followupRepository;
  private final StudentService studentService;
  private final NameLookup nameLookup;

  public List<Lead> list() {
    LoginUser user = AuthSupport.currentUser();
    // 顾问只能看自己的；管理员看全部
    List<Lead> leads =
        user.isAdmin()
            ? leadRepository.findAllByOrderByUpdatedAtDesc()
            : leadRepository.findByOwnerIdOrderByUpdatedAtDesc(user.getId());
    fillNames(leads);
    return leads;
  }

  @Transactional
  public Lead create(CreateLeadRequest request) {
    LoginUser user = AuthSupport.currentUser();
    Instant now = Instant.now();
    Lead lead = new Lead();
    lead.setOwnerId(user.getId());
    lead.setStudentName(request.getStudentName().trim());
    lead.setStudentPhone(request.getStudentPhone().trim());
    lead.setWechat(blankToNull(request.getWechat()));
    lead.setSource(request.getSource() == null ? "运营分配" : request.getSource().trim());
    lead.setRemark(blankToNull(request.getRemark()));
    lead.setStatus(LeadStatus.PENDING);
    lead.setCreatedById(user.getId());
    lead.setUpdatedById(user.getId());
    lead.setCreatedAt(now);
    lead.setUpdatedAt(now);
    Lead saved = leadRepository.save(lead);
    fillNames(List.of(saved));
    return saved;
  }

  /** 批量导入旧线索：姓名+电话必填；状态仅待联系/跟进中/无意向；管理员可按归属顾问姓名指定 */
  @Transactional
  public ImportResult importRows(List<ImportLeadRow> rows) {
    LoginUser user = AuthSupport.currentUser();
    if (rows == null || rows.isEmpty()) {
      throw new BizException("没有可导入的数据");
    }
    if (rows.size() > 2000) {
      throw new BizException("单次最多导入 2000 条");
    }
    Map<String, Long> ownerByName =
        nameLookup.names().entrySet().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    e -> e.getValue().trim(), java.util.Map.Entry::getKey, (a, b) -> a));
    ImportResult result = new ImportResult();
    int rowNo = 1;
    for (ImportLeadRow row : rows) {
      rowNo += 1;
      try {
        String name = row.getStudentName() == null ? "" : row.getStudentName().trim();
        String phone = row.getStudentPhone() == null ? "" : row.getStudentPhone().trim();
        if (name.isEmpty() || phone.isEmpty()) {
          throw new BizException("姓名和电话必填");
        }
        LeadStatus status = parseImportStatus(row.getStatus());
        Long ownerId = user.getId();
        if (user.isAdmin() && row.getOwnerName() != null && !row.getOwnerName().isBlank()) {
          Long matched = ownerByName.get(row.getOwnerName().trim());
          if (matched == null) {
            throw new BizException("找不到归属顾问「" + row.getOwnerName().trim() + "」");
          }
          ownerId = matched;
        }
        Instant now = Instant.now();
        Lead lead = new Lead();
        lead.setOwnerId(ownerId);
        lead.setStudentName(name);
        lead.setStudentPhone(phone);
        lead.setWechat(blankToNull(row.getWechat()));
        lead.setSource(
            row.getSource() == null || row.getSource().isBlank()
                ? "运营分配"
                : row.getSource().trim());
        lead.setRemark(blankToNull(row.getRemark()));
        lead.setStatus(status);
        lead.setCreatedById(user.getId());
        lead.setUpdatedById(user.getId());
        lead.setCreatedAt(now);
        lead.setUpdatedAt(now);
        leadRepository.save(lead);
        result.setSuccess(result.getSuccess() + 1);
      } catch (Exception ex) {
        result.setFailed(result.getFailed() + 1);
        String msg = ex instanceof BizException ? ex.getMessage() : "保存失败";
        result.getErrors().add("第" + rowNo + "行：" + msg);
      }
    }
    return result;
  }

  private LeadStatus parseImportStatus(String raw) {
    if (raw == null || raw.isBlank()) {
      return LeadStatus.PENDING;
    }
    String text = raw.trim();
    return switch (text) {
      case "待联系", "PENDING" -> LeadStatus.PENDING;
      case "跟进中", "FOLLOWING" -> LeadStatus.FOLLOWING;
      case "无意向", "DROPPED" -> LeadStatus.DROPPED;
      case "已成交", "CONVERTED" ->
          throw new BizException("已成交请走「交钱转学员」，导入仅支持待联系/跟进中/无意向");
      default -> throw new BizException("状态只能填：待联系 / 跟进中 / 无意向");
    };
  }

  @Transactional
  public Lead update(Long id, UpdateLeadRequest request) {
    Lead lead = requireLead(id);
    if (lead.getStatus() == LeadStatus.CONVERTED) {
      throw new BizException("已成交线索不能再编辑报名信息");
    }
    LoginUser user = AuthSupport.currentUser();
    lead.setStudentName(request.getStudentName().trim());
    lead.setStudentPhone(request.getStudentPhone().trim());
    lead.setWechat(blankToNull(request.getWechat()));
    if (request.getSource() != null && !request.getSource().isBlank()) {
      lead.setSource(request.getSource().trim());
    }
    lead.setRemark(blankToNull(request.getRemark()));
    lead.setUpdatedById(user.getId());
    lead.setUpdatedAt(Instant.now());
    Lead saved = leadRepository.save(lead);
    fillNames(List.of(saved));
    return saved;
  }

  public Map<String, Object> detail(Long id) {
    Lead lead = requireLead(id);
    fillNames(List.of(lead));
    return Map.of("lead", lead, "followups", followupRepository.findByLeadIdOrderByCreatedAtDesc(id));
  }

  @Transactional
  public LeadFollowup addFollowup(Long id, FollowupRequest request) {
    Lead lead = requireLead(id);
    LoginUser user = AuthSupport.currentUser();
    LeadFollowup followup = new LeadFollowup();
    followup.setLeadId(lead.getId());
    followup.setContent(request.getContent().trim());
    followup.setOperatorId(user.getId());
    followup.setCreatedAt(Instant.now());
    followupRepository.save(followup);
    if (lead.getStatus() == LeadStatus.PENDING) {
      lead.setStatus(LeadStatus.FOLLOWING);
    }
    touch(lead, user.getId());
    return followup;
  }

  @Transactional
  public Lead changeStatus(Long id, StatusRequest request) {
    Lead lead = requireLead(id);
    if (lead.getStatus() == LeadStatus.CONVERTED) {
      throw new BizException("已成交线索不能改状态");
    }
    LoginUser user = AuthSupport.currentUser();
    lead.setStatus(request.getStatus());
    touch(lead, user.getId());
    fillNames(List.of(lead));
    return lead;
  }

  @Transactional
  public Student convert(Long id, StudentService.ConvertRequest request) {
    Lead lead = requireLead(id);
    Student student = studentService.convert(lead, request);
    LoginUser user = AuthSupport.currentUser();
    lead.setUpdatedById(user.getId());
    lead.setUpdatedAt(Instant.now());
    leadRepository.save(lead);
    return student;
  }

  private void touch(Lead lead, Long userId) {
    lead.setUpdatedById(userId);
    lead.setUpdatedAt(Instant.now());
    leadRepository.save(lead);
  }

  private void fillNames(List<Lead> leads) {
    Map<Long, String> names = nameLookup.names();
    for (Lead lead : leads) {
      lead.setOwnerName(names.get(lead.getOwnerId()));
      Long createdBy = lead.getCreatedById() != null ? lead.getCreatedById() : lead.getOwnerId();
      Long updatedBy = lead.getUpdatedById() != null ? lead.getUpdatedById() : lead.getOwnerId();
      lead.setCreatedByName(names.get(createdBy));
      lead.setUpdatedByName(names.get(updatedBy));
    }
  }

  private Lead requireLead(Long id) {
    LoginUser user = AuthSupport.currentUser();
    Lead lead = leadRepository.findById(id).orElseThrow(() -> new BizException("线索不存在"));
    if (!user.isAdmin() && !lead.getOwnerId().equals(user.getId())) {
      throw new BizException("无权查看该线索");
    }
    return lead;
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }

  @Data
  public static class CreateLeadRequest {
    @NotBlank(message = "请填写姓名")
    private String studentName;

    @NotBlank(message = "请填写电话")
    private String studentPhone;

    private String wechat;
    private String source;
    private String remark;
  }

  @Data
  public static class UpdateLeadRequest {
    @NotBlank(message = "请填写姓名")
    private String studentName;

    @NotBlank(message = "请填写电话")
    private String studentPhone;

    private String wechat;
    private String source;
    private String remark;
  }

  @Data
  public static class FollowupRequest {
    @NotBlank(message = "请填写跟进内容")
    private String content;
  }

  @Data
  public static class StatusRequest {
    @NotNull(message = "请选择状态")
    private LeadStatus status;
  }

  @Data
  public static class ImportLeadRow {
    private String studentName;
    private String studentPhone;
    private String wechat;
    private String source;
    private String status;
    private String ownerName;
    private String remark;
  }

  @Data
  public static class ImportResult {
    private int success;
    private int failed;
    private List<String> errors = new java.util.ArrayList<>();
  }
}
