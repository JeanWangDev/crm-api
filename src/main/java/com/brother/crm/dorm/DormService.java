package com.brother.crm.dorm;
import com.brother.crm.common.BizException;
import com.brother.crm.entity.Bed;
import com.brother.crm.entity.Building;
import com.brother.crm.entity.DormRoom;
import com.brother.crm.entity.DormStatus;
import com.brother.crm.entity.Floor;
import com.brother.crm.entity.Gender;
import com.brother.crm.entity.Place;
import com.brother.crm.entity.RoomGender;
import com.brother.crm.entity.Student;
import com.brother.crm.entity.StudentBed;
import com.brother.crm.repository.BedRepository;
import com.brother.crm.repository.BuildingRepository;
import com.brother.crm.repository.DormRoomRepository;
import com.brother.crm.repository.FloorRepository;
import com.brother.crm.repository.PlaceRepository;
import com.brother.crm.repository.StudentBedRepository;
import com.brother.crm.student.StudentService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
public class DormService {

  private final PlaceRepository placeRepository;
  private final BuildingRepository buildingRepository;
  private final FloorRepository floorRepository;
  private final DormRoomRepository roomRepository;
  private final BedRepository bedRepository;
  private final StudentBedRepository studentBedRepository;
  private final StudentService studentService;

  public List<BuildingNode> tree() {
    List<BuildingNode> buildings = new ArrayList<>();
    for (Building building : buildingRepository.findAllByOrderByCampusNameAscNameAsc()) {
      BuildingNode node = new BuildingNode();
      node.setId(building.getId());
      node.setCampusName(building.getCampusName());
      node.setName(building.getName());
      node.setRemark(building.getRemark());
      List<FloorNode> floors = new ArrayList<>();
      for (Floor floor : floorRepository.findByBuildingIdOrderByFloorNoAsc(building.getId())) {
        FloorNode floorNode = new FloorNode();
        floorNode.setId(floor.getId());
        floorNode.setFloorNo(floor.getFloorNo());
        List<RoomNode> rooms = new ArrayList<>();
        for (DormRoom room : roomRepository.findByFloorIdOrderByRoomNoAsc(floor.getId())) {
          RoomNode roomNode = new RoomNode();
          roomNode.setId(room.getId());
          roomNode.setRoomNo(room.getRoomNo());
          roomNode.setGenderLimit(room.getGenderLimit().name());
          roomNode.setRemark(room.getRemark());
          List<BedNode> beds = new ArrayList<>();
          for (Bed bed : bedRepository.findByRoomIdOrderByBedNoAsc(room.getId())) {
            BedNode bedNode = new BedNode();
            bedNode.setId(bed.getId());
            bedNode.setBedNo(bed.getBedNo());
            bedNode.setStatus(bed.getStatus().name());
            studentBedRepository
                .findByBedId(bed.getId())
                .ifPresent(
                    link -> {
                      Student student = studentService.find(link.getStudentId());
                      if (student != null) {
                        bedNode.setStudentId(student.getId());
                        bedNode.setStudentName(student.getName());
                        bedNode.setStudentPhone(student.getPhone());
                      }
                    });
            beds.add(bedNode);
          }
          roomNode.setBeds(beds);
          rooms.add(roomNode);
        }
        floorNode.setRooms(rooms);
        floors.add(floorNode);
      }
      node.setFloors(floors);
      buildings.add(node);
    }
    return buildings;
  }

  public BedPlace ofStudent(Long studentId) {
    return studentBedRepository
        .findByStudentId(studentId)
        .map(link -> place(link.getBedId(), link.getStudentId()))
        .orElse(null);
  }

  public List<PlaceRow> places() {
    List<Building> buildings = buildingRepository.findAll();
    List<PlaceRow> rows = new ArrayList<>();
    for (Place place : placeRepository.findAllByOrderByNameAsc()) {
      PlaceRow row = new PlaceRow();
      row.setId(place.getId());
      row.setName(place.getName());
      row.setAddress(place.getAddress());
      row.setRemark(place.getRemark());
      row.setBuildingCount((int) buildings.stream().filter(item -> place.getId().equals(item.getPlaceId())).count());
      rows.add(row);
    }
    return rows;
  }

  public List<BuildingRow> buildings(Long placeId) {
    Place place = placeRepository.findById(placeId).orElseThrow(() -> new BizException("地点不存在"));
    List<Floor> floors = floorRepository.findAllByOrderByFloorNoAsc();
    List<BuildingRow> rows = new ArrayList<>();
    for (Building building : buildingRepository.findByPlaceIdOrderByNameAsc(placeId)) {
      BuildingRow row = new BuildingRow();
      row.setId(building.getId());
      row.setPlaceId(place.getId());
      row.setPlaceName(place.getName());
      row.setName(building.getName());
      row.setRemark(building.getRemark());
      row.setFloorCount((int) floors.stream().filter(item -> building.getId().equals(item.getBuildingId())).count());
      rows.add(row);
    }
    return rows;
  }

  public List<FloorRow> floors(Long buildingId) {
    Building building = buildingRepository.findById(buildingId).orElseThrow(() -> new BizException("楼栋不存在"));
    List<DormRoom> rooms = roomRepository.findAllByOrderByRoomNoAsc();
    List<FloorRow> rows = new ArrayList<>();
    for (Floor floor : floorRepository.findByBuildingIdOrderByFloorNoAsc(buildingId)) {
      FloorRow row = new FloorRow();
      row.setId(floor.getId());
      row.setBuildingId(building.getId());
      row.setBuildingName(building.getName());
      row.setPlaceName(building.getCampusName());
      row.setFloorNo(floor.getFloorNo());
      row.setRemark(floor.getRemark());
      row.setRoomCount((int) rooms.stream().filter(item -> floor.getId().equals(item.getFloorId())).count());
      rows.add(row);
    }
    return rows;
  }

  public List<RoomRow> rooms(Long floorId) {
    Floor floor = floorRepository.findById(floorId).orElseThrow(() -> new BizException("楼层不存在"));
    Building building = buildingRepository.findById(floor.getBuildingId()).orElseThrow(() -> new BizException("楼栋不存在"));
    List<Bed> beds = bedRepository.findAllByOrderByBedNoAsc();
    List<RoomRow> rows = new ArrayList<>();
    for (DormRoom room : roomRepository.findByFloorIdOrderByRoomNoAsc(floorId)) {
      RoomRow row = new RoomRow();
      row.setId(room.getId());
      row.setFloorId(floor.getId());
      row.setFloorNo(floor.getFloorNo());
      row.setBuildingName(building.getName());
      row.setPlaceName(building.getCampusName());
      row.setRoomNo(room.getRoomNo());
      row.setGenderLimit(room.getGenderLimit().name());
      row.setRemark(room.getRemark());
      List<Bed> roomBeds = beds.stream().filter(item -> room.getId().equals(item.getRoomId())).toList();
      row.setBedCount(roomBeds.size());
      row.setFreeCount((int) roomBeds.stream().filter(item -> item.getStatus() == DormStatus.FREE).count());
      rows.add(row);
    }
    return rows;
  }

  public List<BedNode> beds(Long roomId) {
    roomRepository.findById(roomId).orElseThrow(() -> new BizException("房间不存在"));
    List<BedNode> rows = new ArrayList<>();
    for (Bed bed : bedRepository.findByRoomIdOrderByBedNoAsc(roomId)) {
      BedNode node = new BedNode();
      node.setId(bed.getId());
      node.setBedNo(bed.getBedNo());
      node.setStatus(bed.getStatus().name());
      studentBedRepository
          .findByBedId(bed.getId())
          .ifPresent(
              link -> {
                Student student = studentService.find(link.getStudentId());
                if (student != null) {
                  node.setStudentId(student.getId());
                  node.setStudentName(student.getName());
                  node.setStudentPhone(student.getPhone());
                }
              });
      rows.add(node);
    }
    return rows;
  }

  @Transactional
  public Place createPlace(PlaceRequest request) {
    Place place = new Place();
    place.setName(request.getName().trim());
    place.setAddress(blank(request.getAddress()));
    place.setRemark(blank(request.getRemark()));
    return placeRepository.save(place);
  }

  @Transactional
  public Place updatePlace(Long id, PlaceRequest request) {
    Place place = placeRepository.findById(id).orElseThrow(() -> new BizException("地点不存在"));
    place.setName(request.getName().trim());
    place.setAddress(blank(request.getAddress()));
    place.setRemark(blank(request.getRemark()));
    placeRepository.save(place);
    for (Building building : buildingRepository.findByPlaceIdOrderByNameAsc(id)) {
      building.setCampusName(place.getName());
      buildingRepository.save(building);
    }
    return place;
  }

  @Transactional
  public void deletePlace(Long id) {
    placeRepository.findById(id).orElseThrow(() -> new BizException("地点不存在"));
    assertNoOccupants(occupantsOfPlace(id), "地点");
    for (Building building : buildingRepository.findByPlaceIdOrderByNameAsc(id)) {
      deleteBuildingCascade(building.getId());
    }
    placeRepository.deleteById(id);
  }

  @Transactional
  public Building createBuilding(Long placeId, BuildingRequest request) {
    Place place = placeRepository.findById(placeId).orElseThrow(() -> new BizException("地点不存在"));
    Building building = new Building();
    building.setPlaceId(place.getId());
    building.setCampusName(place.getName());
    building.setName(request.getName().trim());
    building.setRemark(blank(request.getRemark()));
    return buildingRepository.save(building);
  }

  @Transactional
  public Building updateBuilding(Long id, BuildingRequest request) {
    Building building = buildingRepository.findById(id).orElseThrow(() -> new BizException("楼栋不存在"));
    building.setName(request.getName().trim());
    building.setRemark(blank(request.getRemark()));
    return buildingRepository.save(building);
  }

  @Transactional
  public void deleteBuilding(Long id) {
    buildingRepository.findById(id).orElseThrow(() -> new BizException("楼栋不存在"));
    assertNoOccupants(occupantsOfBuilding(id), "楼栋");
    deleteBuildingCascade(id);
  }

  private void deleteBuildingCascade(Long buildingId) {
    for (Floor floor : floorRepository.findByBuildingIdOrderByFloorNoAsc(buildingId)) {
      deleteFloorCascade(floor.getId());
    }
    buildingRepository.deleteById(buildingId);
  }

  public List<BuildingRow> allBuildings() {
    List<Place> places = placeRepository.findAllByOrderByNameAsc();
    List<Floor> floors = floorRepository.findAllByOrderByFloorNoAsc();
    List<BuildingRow> rows = new ArrayList<>();
    for (Building building : buildingRepository.findAllByOrderByCampusNameAscNameAsc()) {
      Place place =
          places.stream().filter(item -> item.getId().equals(building.getPlaceId())).findFirst().orElse(null);
      BuildingRow row = new BuildingRow();
      row.setId(building.getId());
      row.setPlaceId(building.getPlaceId());
      row.setPlaceName(place == null ? building.getCampusName() : place.getName());
      row.setName(building.getName());
      row.setRemark(building.getRemark());
      row.setFloorCount((int) floors.stream().filter(item -> building.getId().equals(item.getBuildingId())).count());
      rows.add(row);
    }
    return rows;
  }

  public List<FloorRow> allFloors() {
    List<Building> buildings = buildingRepository.findAll();
    List<DormRoom> rooms = roomRepository.findAllByOrderByRoomNoAsc();
    List<FloorRow> rows = new ArrayList<>();
    List<Floor> floors = new ArrayList<>(floorRepository.findAllByOrderByFloorNoAsc());
    floors.sort(Comparator.comparing(Floor::getBuildingId).thenComparing(Floor::getFloorNo));
    for (Floor floor : floors) {
      Building building =
          buildings.stream().filter(item -> item.getId().equals(floor.getBuildingId())).findFirst().orElse(null);
      FloorRow row = new FloorRow();
      row.setId(floor.getId());
      row.setBuildingId(floor.getBuildingId());
      row.setBuildingName(building == null ? "" : building.getName());
      row.setPlaceName(building == null ? "" : building.getCampusName());
      row.setFloorNo(floor.getFloorNo());
      row.setRemark(floor.getRemark());
      row.setRoomCount((int) rooms.stream().filter(item -> floor.getId().equals(item.getFloorId())).count());
      rows.add(row);
    }
    return rows;
  }

  public List<RoomRow> allRooms() {
    List<Floor> floors = floorRepository.findAll();
    List<Building> buildings = buildingRepository.findAll();
    List<Bed> beds = bedRepository.findAllByOrderByBedNoAsc();
    List<RoomRow> rows = new ArrayList<>();
    for (DormRoom room : roomRepository.findAllByOrderByRoomNoAsc()) {
      Floor floor = floors.stream().filter(item -> item.getId().equals(room.getFloorId())).findFirst().orElse(null);
      Building building =
          floor == null
              ? null
              : buildings.stream().filter(item -> item.getId().equals(floor.getBuildingId())).findFirst().orElse(null);
      RoomRow row = new RoomRow();
      row.setId(room.getId());
      row.setFloorId(room.getFloorId());
      row.setFloorNo(floor == null ? null : floor.getFloorNo());
      row.setBuildingId(building == null ? null : building.getId());
      row.setBuildingName(building == null ? "" : building.getName());
      row.setPlaceName(building == null ? "" : building.getCampusName());
      row.setRoomNo(room.getRoomNo());
      row.setGenderLimit(room.getGenderLimit().name());
      row.setRemark(room.getRemark());
      List<Bed> roomBeds = beds.stream().filter(item -> room.getId().equals(item.getRoomId())).toList();
      row.setBedCount(roomBeds.size());
      row.setFreeCount((int) roomBeds.stream().filter(item -> item.getStatus() == DormStatus.FREE).count());
      rows.add(row);
    }
    return rows;
  }

  public List<BedRow> allBeds() {
    Map<Long, DormRoom> rooms =
        roomRepository.findAll().stream().collect(Collectors.toMap(DormRoom::getId, Function.identity()));
    Map<Long, Floor> floors =
        floorRepository.findAll().stream().collect(Collectors.toMap(Floor::getId, Function.identity()));
    Map<Long, Building> buildings =
        buildingRepository.findAll().stream().collect(Collectors.toMap(Building::getId, Function.identity()));
    List<Bed> beds = bedRepository.findAllByOrderByBedNoAsc();
    List<Long> bedIds = beds.stream().map(Bed::getId).toList();
    Map<Long, StudentBed> linksByBed =
        bedIds.isEmpty()
            ? Map.of()
            : studentBedRepository.findByBedIdIn(bedIds).stream()
                .collect(Collectors.toMap(StudentBed::getBedId, Function.identity(), (a, b) -> a));
    Map<Long, Student> students = studentsByIds(linksByBed.values().stream().map(StudentBed::getStudentId).toList());
    List<BedRow> rows = new ArrayList<>();
    for (Bed bed : beds) {
      DormRoom room = rooms.get(bed.getRoomId());
      Floor floor = room == null ? null : floors.get(room.getFloorId());
      Building building = floor == null ? null : buildings.get(floor.getBuildingId());
      BedRow row = new BedRow();
      row.setId(bed.getId());
      row.setBedNo(bed.getBedNo());
      row.setStatus(bed.getStatus().name());
      row.setRoomId(bed.getRoomId());
      row.setRoomNo(room == null ? "" : room.getRoomNo());
      row.setFloorId(floor == null ? null : floor.getId());
      row.setFloorNo(floor == null ? null : floor.getFloorNo());
      row.setBuildingId(building == null ? null : building.getId());
      row.setBuildingName(building == null ? "" : building.getName());
      row.setPlaceName(building == null ? "" : building.getCampusName());
      row.setGenderLimit(room == null || room.getGenderLimit() == null ? null : room.getGenderLimit().name());
      StudentBed link = linksByBed.get(bed.getId());
      if (link != null) {
        Student student = students.get(link.getStudentId());
        if (student != null) {
          row.setStudentId(student.getId());
          row.setStudentName(student.getName());
          row.setStudentPhone(student.getPhone());
        }
      }
      rows.add(row);
    }
    return rows;
  }

  @Transactional
  public Floor updateFloor(Long id, FloorUpdateRequest request) {
    Floor floor = floorRepository.findById(id).orElseThrow(() -> new BizException("楼层不存在"));
    Long buildingId = floor.getBuildingId();
    boolean exists =
        floorRepository.findByBuildingIdOrderByFloorNoAsc(buildingId).stream()
            .anyMatch(item -> !item.getId().equals(id) && item.getFloorNo().equals(request.getFloorNo()));
    if (exists) {
      throw new BizException("该楼栋已有这一层");
    }
    floor.setFloorNo(request.getFloorNo());
    floor.setRemark(blank(request.getRemark()));
    return floorRepository.save(floor);
  }

  @Transactional
  public void deleteFloor(Long id) {
    floorRepository.findById(id).orElseThrow(() -> new BizException("楼层不存在"));
    assertNoOccupants(occupantsOfFloor(id), "楼层");
    deleteFloorCascade(id);
  }

  private void deleteFloorCascade(Long floorId) {
    for (DormRoom room : roomRepository.findByFloorIdOrderByRoomNoAsc(floorId)) {
      deleteRoomCascade(room.getId());
    }
    floorRepository.deleteById(floorId);
  }

  @Transactional
  public Floor createFloor(Long buildingId, FloorRequest request) {
    buildingRepository.findById(buildingId).orElseThrow(() -> new BizException("楼栋不存在"));
    boolean exists =
        floorRepository.findByBuildingIdOrderByFloorNoAsc(buildingId).stream()
            .anyMatch(floor -> floor.getFloorNo().equals(request.getFloorNo()));
    if (exists) {
      throw new BizException("该楼栋已有这一层");
    }
    int bedsEach = request.getBedCount() == null ? 6 : request.getBedCount();
    if (bedsEach < 0 || bedsEach > 12) {
      throw new BizException("单间床位为 0 到 12，默认 6 人间");
    }
    Floor floor = new Floor();
    floor.setBuildingId(buildingId);
    floor.setFloorNo(request.getFloorNo());
    floor.setRemark(blank(request.getRemark()));
    floorRepository.save(floor);
    List<String> roomNos = roomNumbers(request.getRoomNos());
    if (!roomNos.isEmpty()) {
      if (request.getGenderLimit() == null || request.getGenderLimit() == RoomGender.ANY) {
        throw new BizException("批量建房时请选择男女宿舍（男生或女生）");
      }
    }
    for (String roomNo : roomNos) {
      RoomRequest room = new RoomRequest();
      room.setRoomNo(roomNo);
      room.setBedCount(bedsEach);
      room.setGenderLimit(request.getGenderLimit());
      createRoom(floor.getId(), room);
    }
    return floor;
  }

  @Transactional
  public DormRoom updateRoom(Long id, RoomUpdateRequest request) {
    DormRoom room = roomRepository.findById(id).orElseThrow(() -> new BizException("房间不存在"));
    Long floorId = room.getFloorId();
    String roomNo = request.getRoomNo().trim();
    boolean duplicated =
        roomRepository.findByFloorIdOrderByRoomNoAsc(floorId).stream()
            .anyMatch(item -> !item.getId().equals(id) && item.getRoomNo().equals(roomNo));
    if (duplicated) {
      throw new BizException("该楼层已有房间 " + roomNo);
    }
    room.setRoomNo(roomNo);
    if (request.getGenderLimit() == null || request.getGenderLimit() == RoomGender.ANY) {
      throw new BizException("请选择男女宿舍（男生宿舍或女生宿舍）");
    }
    room.setGenderLimit(request.getGenderLimit());
    room.setRemark(blank(request.getRemark()));
    return roomRepository.save(room);
  }

  @Transactional
  public void deleteRoom(Long id) {
    roomRepository.findById(id).orElseThrow(() -> new BizException("房间不存在"));
    assertNoOccupants(occupantsOfRoom(id), "房间");
    deleteRoomCascade(id);
  }

  private void deleteRoomCascade(Long roomId) {
    for (Bed bed : bedRepository.findByRoomIdOrderByBedNoAsc(roomId)) {
      studentBedRepository.findByBedId(bed.getId()).ifPresent(studentBedRepository::delete);
      bedRepository.delete(bed);
    }
    roomRepository.deleteById(roomId);
  }

  @Transactional
  public DormRoom createRoom(Long floorId, RoomRequest request) {
    floorRepository.findById(floorId).orElseThrow(() -> new BizException("楼层不存在"));
    DormRoom room = new DormRoom();
    room.setFloorId(floorId);
    room.setRoomNo(request.getRoomNo().trim());
    if (request.getGenderLimit() == null || request.getGenderLimit() == RoomGender.ANY) {
      throw new BizException("请选择男女宿舍（男生宿舍或女生宿舍）");
    }
    room.setGenderLimit(request.getGenderLimit());
    room.setRemark(blank(request.getRemark()));
    boolean duplicated =
        roomRepository.findByFloorIdOrderByRoomNoAsc(floorId).stream()
            .anyMatch(item -> item.getRoomNo().equals(room.getRoomNo()));
    if (duplicated) {
      throw new BizException("该楼层已有房间 " + room.getRoomNo());
    }
    roomRepository.save(room);
    int count = request.getBedCount() == null ? 6 : request.getBedCount();
    for (int i = 1; i <= count; i++) {
      saveBed(room.getId(), i);
    }
    return room;
  }

  @Transactional
  public Bed addBed(Long roomId) {
    roomRepository.findById(roomId).orElseThrow(() -> new BizException("房间不存在"));
    int next =
        bedRepository.findByRoomIdOrderByBedNoAsc(roomId).stream()
            .map(Bed::getBedNo)
            .max(Integer::compareTo)
            .orElse(0)
            + 1;
    if (next > 12) {
      throw new BizException("单间最多 12 个床位");
    }
    return saveBed(roomId, next);
  }

  private List<String> roomNumbers(List<String> raw) {
    List<String> numbers = new ArrayList<>();
    if (raw == null) {
      return numbers;
    }
    for (String item : raw) {
      if (item == null) {
        continue;
      }
      for (String part : item.split("[,，、\\s]+")) {
        String roomNo = part.trim();
        if (!roomNo.isEmpty() && !numbers.contains(roomNo)) {
          numbers.add(roomNo);
        }
      }
    }
    return numbers;
  }

  private Bed saveBed(Long roomId, int bedNo) {
    Bed bed = new Bed();
    bed.setRoomId(roomId);
    bed.setBedNo(bedNo);
    bed.setStatus(DormStatus.FREE);
    return bedRepository.save(bed);
  }

  @Transactional
  public BedPlace assign(Long bedId, Long studentId) {
    Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new BizException("床位不存在"));
    Student student = studentService.require(studentId);
    DormRoom room = roomRepository.findById(bed.getRoomId()).orElseThrow(() -> new BizException("房间不存在"));
    if (room.getGenderLimit() == RoomGender.MALE && student.getGender() != Gender.MALE) {
      throw new BizException("该房间只住男生");
    }
    if (room.getGenderLimit() == RoomGender.FEMALE && student.getGender() != Gender.FEMALE) {
      throw new BizException("该房间只住女生");
    }
    studentBedRepository
        .findByBedId(bedId)
        .ifPresent(
            occupied -> {
              if (!occupied.getStudentId().equals(studentId)) {
                throw new BizException("该床位已有学员");
              }
            });
    studentBedRepository
        .findByStudentId(studentId)
        .ifPresent(
            old -> {
              if (!old.getBedId().equals(bedId)) {
                releaseBed(old.getBedId());
                studentBedRepository.delete(old);
                studentBedRepository.flush();
              }
            });
    StudentBed link = studentBedRepository.findByStudentId(studentId).orElseGet(StudentBed::new);
    link.setStudentId(student.getId());
    link.setBedId(bed.getId());
    link.setAssignTime(Instant.now());
    studentBedRepository.save(link);
    bed.setStatus(DormStatus.OCCUPIED);
    bedRepository.save(bed);
    student.setUpdatedAt(Instant.now());
    return place(bed.getId(), student.getId());
  }

  @Transactional
  public BedPlace release(Long bedId) {
    bedRepository.findById(bedId).orElseThrow(() -> new BizException("床位不存在"));
    studentBedRepository.findByBedId(bedId).ifPresent(studentBedRepository::delete);
    releaseBed(bedId);
    return place(bedId, null);
  }

  @Transactional
  public List<BedPlace> batchAssign(BatchAssignRequest request) {
    if (request.getItems() == null || request.getItems().isEmpty()) {
      throw new BizException("请选择要分配的床位和学员");
    }
    List<BedPlace> results = new ArrayList<>();
    for (AssignItem item : request.getItems()) {
      results.add(assign(item.getBedId(), item.getStudentId()));
    }
    return results;
  }

  public List<OccupantRow> occupantsOfPlace(Long placeId) {
    placeRepository.findById(placeId).orElseThrow(() -> new BizException("地点不存在"));
    return occupantsOfBeds(bedIdsOfPlace(placeId));
  }

  public List<OccupantRow> occupantsOfBuilding(Long buildingId) {
    buildingRepository.findById(buildingId).orElseThrow(() -> new BizException("楼栋不存在"));
    return occupantsOfBeds(bedIdsOfBuilding(buildingId));
  }

  public List<OccupantRow> occupantsOfFloor(Long floorId) {
    floorRepository.findById(floorId).orElseThrow(() -> new BizException("楼层不存在"));
    return occupantsOfBeds(bedIdsOfFloor(floorId));
  }

  public List<OccupantRow> occupantsOfRoom(Long roomId) {
    roomRepository.findById(roomId).orElseThrow(() -> new BizException("房间不存在"));
    return occupantsOfBeds(bedIdsOfRoom(roomId));
  }

  private void assertNoOccupants(List<OccupantRow> occupants, String scope) {
    if (occupants.isEmpty()) {
      return;
    }
    String names =
        occupants.stream()
            .map(
                item ->
                    item.getStudentName()
                        + (item.getStudentPhone() == null ? "" : "(" + item.getStudentPhone() + ")")
                        + " · "
                        + item.getBedLabel())
            .reduce((a, b) -> a + "；" + b)
            .orElse("");
    throw new BizException("无法删除该" + scope + "：仍有学员入住 — " + names + "。请先退宿或换床后再删。");
  }

  private List<Long> bedIdsOfPlace(Long placeId) {
    List<Long> bedIds = new ArrayList<>();
    for (Building building : buildingRepository.findByPlaceIdOrderByNameAsc(placeId)) {
      bedIds.addAll(bedIdsOfBuilding(building.getId()));
    }
    return bedIds;
  }

  private List<Long> bedIdsOfBuilding(Long buildingId) {
    List<Long> bedIds = new ArrayList<>();
    for (Floor floor : floorRepository.findByBuildingIdOrderByFloorNoAsc(buildingId)) {
      bedIds.addAll(bedIdsOfFloor(floor.getId()));
    }
    return bedIds;
  }

  private List<Long> bedIdsOfFloor(Long floorId) {
    List<Long> bedIds = new ArrayList<>();
    for (DormRoom room : roomRepository.findByFloorIdOrderByRoomNoAsc(floorId)) {
      bedIds.addAll(bedIdsOfRoom(room.getId()));
    }
    return bedIds;
  }

  private List<Long> bedIdsOfRoom(Long roomId) {
    return bedRepository.findByRoomIdOrderByBedNoAsc(roomId).stream().map(Bed::getId).toList();
  }

  private List<OccupantRow> occupantsOfBeds(List<Long> bedIds) {
    List<OccupantRow> rows = new ArrayList<>();
    for (Long bedId : bedIds) {
      studentBedRepository
          .findByBedId(bedId)
          .ifPresent(
              link -> {
                BedPlace view = place(bedId, link.getStudentId());
                OccupantRow row = new OccupantRow();
                row.setStudentId(view.getStudentId());
                row.setStudentName(view.getStudentName());
                Student student = studentService.find(link.getStudentId());
                if (student != null) {
                  row.setStudentPhone(student.getPhone());
                }
                row.setBedId(bedId);
                row.setBedLabel(view.getLabel());
                rows.add(row);
              });
    }
    return rows;
  }

  private void releaseBed(Long bedId) {
    bedRepository
        .findById(bedId)
        .ifPresent(
            bed -> {
              bed.setStatus(DormStatus.FREE);
              bedRepository.save(bed);
            });
  }

  private BedPlace place(Long bedId, Long studentId) {
    Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new BizException("床位不存在"));
    DormRoom room = roomRepository.findById(bed.getRoomId()).orElseThrow(() -> new BizException("房间不存在"));
    Floor floor = floorRepository.findById(room.getFloorId()).orElseThrow(() -> new BizException("楼层不存在"));
    Building building =
        buildingRepository.findById(floor.getBuildingId()).orElseThrow(() -> new BizException("楼栋不存在"));
    BedPlace view = new BedPlace();
    view.setBedId(bed.getId());
    view.setBedNo(bed.getBedNo());
    view.setStatus(bed.getStatus().name());
    view.setRoomId(room.getId());
    view.setRoomNo(room.getRoomNo());
    view.setGenderLimit(room.getGenderLimit().name());
    view.setFloorId(floor.getId());
    view.setFloorNo(floor.getFloorNo());
    view.setBuildingId(building.getId());
    view.setBuildingName(building.getName());
    view.setCampusName(building.getCampusName());
    view.setStudentId(studentId);
    if (studentId != null) {
      Student student = studentService.find(studentId);
      if (student != null) {
        view.setStudentName(student.getName());
      }
    }
    view.setLabel(
        formatBedLabel(
            building.getCampusName(),
            building.getName(),
            floor.getFloorNo(),
            room.getRoomNo(),
            bed.getBedNo()));
    return view;
  }

  public List<BedPlace> freeBeds() {
    Map<Long, DormRoom> rooms =
        roomRepository.findAll().stream().collect(Collectors.toMap(DormRoom::getId, Function.identity()));
    Map<Long, Floor> floors =
        floorRepository.findAll().stream().collect(Collectors.toMap(Floor::getId, Function.identity()));
    Map<Long, Building> buildings =
        buildingRepository.findAll().stream().collect(Collectors.toMap(Building::getId, Function.identity()));
    List<BedPlace> rows = new ArrayList<>();
    for (Bed bed : bedRepository.findAllByOrderByBedNoAsc()) {
      if (bed.getStatus() != DormStatus.FREE) {
        continue;
      }
      rows.add(toPlace(bed, rooms, floors, buildings, null, null));
    }
    rows.sort(Comparator.comparing(BedPlace::getLabel));
    return rows;
  }

  /** 批量查学员床位，避免报到名单对每人单独打库。 */
  public Map<Long, BedPlace> placesByStudentIds(Collection<Long> studentIds) {
    if (studentIds == null || studentIds.isEmpty()) {
      return Map.of();
    }
    List<StudentBed> links = studentBedRepository.findByStudentIdIn(studentIds);
    if (links.isEmpty()) {
      return Map.of();
    }
    Map<Long, Bed> beds =
        bedRepository.findAllById(links.stream().map(StudentBed::getBedId).toList()).stream()
            .collect(Collectors.toMap(Bed::getId, Function.identity()));
    Map<Long, DormRoom> rooms =
        roomRepository.findAllById(beds.values().stream().map(Bed::getRoomId).toList()).stream()
            .collect(Collectors.toMap(DormRoom::getId, Function.identity()));
    Map<Long, Floor> floors =
        floorRepository.findAllById(rooms.values().stream().map(DormRoom::getFloorId).toList()).stream()
            .collect(Collectors.toMap(Floor::getId, Function.identity()));
    Map<Long, Building> buildings =
        buildingRepository.findAllById(floors.values().stream().map(Floor::getBuildingId).toList()).stream()
            .collect(Collectors.toMap(Building::getId, Function.identity()));
    Map<Long, Student> students = studentsByIds(studentIds);
    Map<Long, BedPlace> result = new HashMap<>();
    for (StudentBed link : links) {
      Bed bed = beds.get(link.getBedId());
      if (bed == null) {
        continue;
      }
      result.put(
          link.getStudentId(),
          toPlace(bed, rooms, floors, buildings, link.getStudentId(), students.get(link.getStudentId())));
    }
    return result;
  }

  private Map<Long, Student> studentsByIds(Collection<Long> studentIds) {
    return studentService.mapByIds(studentIds);
  }

  private BedPlace toPlace(
      Bed bed,
      Map<Long, DormRoom> rooms,
      Map<Long, Floor> floors,
      Map<Long, Building> buildings,
      Long studentId,
      Student student) {
    DormRoom room = rooms.get(bed.getRoomId());
    Floor floor = room == null ? null : floors.get(room.getFloorId());
    Building building = floor == null ? null : buildings.get(floor.getBuildingId());
    BedPlace view = new BedPlace();
    view.setBedId(bed.getId());
    view.setBedNo(bed.getBedNo());
    view.setStatus(bed.getStatus().name());
    view.setRoomId(room == null ? null : room.getId());
    view.setRoomNo(room == null ? "" : room.getRoomNo());
    view.setGenderLimit(room == null ? null : room.getGenderLimit().name());
    view.setFloorId(floor == null ? null : floor.getId());
    view.setFloorNo(floor == null ? null : floor.getFloorNo());
    view.setBuildingId(building == null ? null : building.getId());
    view.setBuildingName(building == null ? "" : building.getName());
    view.setCampusName(building == null ? "" : building.getCampusName());
    view.setStudentId(studentId);
    if (student != null) {
      view.setStudentName(student.getName());
    }
    view.setLabel(
        formatBedLabel(
            building == null ? null : building.getCampusName(),
            building == null ? null : building.getName(),
            floor == null ? null : floor.getFloorNo(),
            room == null ? null : room.getRoomNo(),
            bed.getBedNo()));
    return view;
  }

  /** 地点/楼栋/楼层/房间(N床位) */
  private String formatBedLabel(
      String placeName, String buildingName, Integer floorNo, String roomNo, Integer bedNo) {
    String place = placeName == null || placeName.isBlank() ? "-" : placeName.trim();
    String building = buildingName == null || buildingName.isBlank() ? "-" : buildingName.trim();
    String floor = floorNo == null ? "-" : floorNo + "层";
    String room = roomNo == null || roomNo.isBlank() ? "-" : roomNo.trim();
    int no = bedNo == null ? 0 : bedNo;
    return place + "/" + building + "/" + floor + "/" + room + "(" + no + "床位)";
  }

  private String blank(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }

  @Data
  public static class PlaceRequest {
    @NotBlank(message = "请填写地点名称")
    private String name;

    private String address;
    private String remark;
  }

  @Data
  public static class PlaceRow {
    private Long id;
    private String name;
    private String address;
    private String remark;
    private Integer buildingCount;
  }

  @Data
  public static class BuildingRow {
    private Long id;
    private Long placeId;
    private String placeName;
    private String name;
    private String remark;
    private Integer floorCount;
  }

  @Data
  public static class FloorRow {
    private Long id;
    private Long buildingId;
    private String placeName;
    private String buildingName;
    private Integer floorNo;
    private String remark;
    private Integer roomCount;
  }

  @Data
  public static class RoomRow {
    private Long id;
    private Long floorId;
    private Long buildingId;
    private String placeName;
    private String buildingName;
    private Integer floorNo;
    private String roomNo;
    private String genderLimit;
    private String remark;
    private Integer bedCount;
    private Integer freeCount;
  }

  @Data
  public static class BuildingRequest {
    @NotBlank(message = "请填写楼栋")
    private String name;

    private String remark;
  }

  @Data
  public static class FloorRequest {
    @NotNull(message = "请填写楼层")
    @Min(value = 1, message = "楼层从 1 开始")
    @Max(value = 40, message = "楼层不能超过 40")
    private Integer floorNo;

    private String remark;

    @Min(value = 0, message = "床位数不能为负")
    @Max(value = 12, message = "单间最多 12 个床位")
    private Integer bedCount;

    private List<String> roomNos;

    /** 批量建房时的男女宿舍，楼层本身不存性别 */
    private RoomGender genderLimit;
  }

  @Data
  public static class FloorUpdateRequest {
    @NotNull(message = "请填写楼层")
    @Min(value = 1, message = "楼层从 1 开始")
    @Max(value = 40, message = "楼层不能超过 40")
    private Integer floorNo;

    private String remark;
  }

  @Data
  public static class RoomRequest {
    @NotBlank(message = "请填写房间号")
    private String roomNo;

    @Min(value = 0, message = "床位数不能为负")
    @Max(value = 12, message = "单间最多 12 个床位")
    private Integer bedCount;

    @NotNull(message = "请选择男女宿舍")
    private RoomGender genderLimit;

    private String remark;
  }

  @Data
  public static class RoomUpdateRequest {
    @NotBlank(message = "请填写房间号")
    private String roomNo;

    @NotNull(message = "请选择男女宿舍")
    private RoomGender genderLimit;

    private String remark;
  }

  @Data
  public static class BatchAssignRequest {
    @NotNull(message = "请选择要分配的床位和学员")
    private List<@Valid AssignItem> items;
  }

  @Data
  public static class AssignItem {
    @NotNull(message = "请选择床位")
    private Long bedId;

    @NotNull(message = "请选择学员")
    private Long studentId;
  }

  @Data
  public static class OccupantRow {
    private Long studentId;
    private String studentName;
    private String studentPhone;
    private Long bedId;
    private String bedLabel;
  }

  @Data
  public static class BuildingNode {
    private Long id;
    private String campusName;
    private String name;
    private String remark;
    private List<FloorNode> floors;
  }

  @Data
  public static class FloorNode {
    private Long id;
    private Integer floorNo;
    private List<RoomNode> rooms;
  }

  @Data
  public static class RoomNode {
    private Long id;
    private String roomNo;
    private String genderLimit;
    private String remark;
    private List<BedNode> beds;
  }

  @Data
  public static class BedRow {
    private Long id;
    private Integer bedNo;
    private String status;
    private Long studentId;
    private String studentName;
    private String studentPhone;
    private Long roomId;
    private String roomNo;
    private Long floorId;
    private Integer floorNo;
    private Long buildingId;
    private String buildingName;
    private String placeName;
    private String genderLimit;
  }

  @Data
  public static class BedNode {
    private Long id;
    private Integer bedNo;
    private String status;
    private Long studentId;
    private String studentName;
    private String studentPhone;
  }

  @Data
  public static class BedPlace {
    private Long bedId;
    private Integer bedNo;
    private String status;
    private Long roomId;
    private String roomNo;
    private String genderLimit;
    private Long floorId;
    private Integer floorNo;
    private Long buildingId;
    private String buildingName;
    private String campusName;
    private Long studentId;
    private String studentName;
    private String label;
  }
}
