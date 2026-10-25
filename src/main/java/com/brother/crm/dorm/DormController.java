package com.brother.crm.dorm;

import com.brother.crm.common.ApiResponse;
import com.brother.crm.entity.Bed;
import com.brother.crm.entity.Building;
import com.brother.crm.entity.DormRoom;
import com.brother.crm.entity.Floor;
import com.brother.crm.entity.Place;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dorms")
@RequiredArgsConstructor
public class DormController {

  private final DormService dormService;

  @GetMapping("/places")
  public ApiResponse<List<DormService.PlaceRow>> places() {
    return ApiResponse.ok(dormService.places());
  }

  @PostMapping("/places")
  public ApiResponse<Place> createPlace(@Valid @RequestBody DormService.PlaceRequest request) {
    return ApiResponse.ok(dormService.createPlace(request));
  }

  @PutMapping("/places/{id}")
  public ApiResponse<Place> updatePlace(
      @PathVariable Long id, @Valid @RequestBody DormService.PlaceRequest request) {
    return ApiResponse.ok(dormService.updatePlace(id, request));
  }

  @DeleteMapping("/places/{id}")
  public ApiResponse<Void> deletePlace(@PathVariable Long id) {
    dormService.deletePlace(id);
    return ApiResponse.ok();
  }

  @GetMapping("/places/{id}/occupants")
  public ApiResponse<List<DormService.OccupantRow>> placeOccupants(@PathVariable Long id) {
    return ApiResponse.ok(dormService.occupantsOfPlace(id));
  }

  @GetMapping("/buildings")
  public ApiResponse<List<DormService.BuildingRow>> allBuildings() {
    return ApiResponse.ok(dormService.allBuildings());
  }

  @GetMapping("/floors")
  public ApiResponse<List<DormService.FloorRow>> allFloors() {
    return ApiResponse.ok(dormService.allFloors());
  }

  @GetMapping("/rooms")
  public ApiResponse<List<DormService.RoomRow>> allRooms() {
    return ApiResponse.ok(dormService.allRooms());
  }

  @GetMapping("/beds")
  public ApiResponse<List<DormService.BedRow>> allBeds() {
    return ApiResponse.ok(dormService.allBeds());
  }

  @GetMapping("/places/{id}/buildings")
  public ApiResponse<List<DormService.BuildingRow>> buildings(@PathVariable Long id) {
    return ApiResponse.ok(dormService.buildings(id));
  }

  @PostMapping("/places/{id}/buildings")
  public ApiResponse<Building> createBuilding(
      @PathVariable Long id, @Valid @RequestBody DormService.BuildingRequest request) {
    return ApiResponse.ok(dormService.createBuilding(id, request));
  }

  @PutMapping("/buildings/{id}")
  public ApiResponse<Building> updateBuilding(
      @PathVariable Long id, @Valid @RequestBody DormService.BuildingRequest request) {
    return ApiResponse.ok(dormService.updateBuilding(id, request));
  }

  @DeleteMapping("/buildings/{id}")
  public ApiResponse<Void> deleteBuilding(@PathVariable Long id) {
    dormService.deleteBuilding(id);
    return ApiResponse.ok();
  }

  @GetMapping("/buildings/{id}/occupants")
  public ApiResponse<List<DormService.OccupantRow>> buildingOccupants(@PathVariable Long id) {
    return ApiResponse.ok(dormService.occupantsOfBuilding(id));
  }

  @GetMapping("/buildings/{id}/floors")
  public ApiResponse<List<DormService.FloorRow>> floors(@PathVariable Long id) {
    return ApiResponse.ok(dormService.floors(id));
  }

  @GetMapping("/floors/{id}/rooms")
  public ApiResponse<List<DormService.RoomRow>> rooms(@PathVariable Long id) {
    return ApiResponse.ok(dormService.rooms(id));
  }

  @GetMapping("/rooms/{id}/beds")
  public ApiResponse<List<DormService.BedNode>> beds(@PathVariable Long id) {
    return ApiResponse.ok(dormService.beds(id));
  }

  @PostMapping("/rooms/{id}/beds")
  public ApiResponse<Bed> addBed(@PathVariable Long id) {
    return ApiResponse.ok(dormService.addBed(id));
  }

  @GetMapping("/tree")
  public ApiResponse<List<DormService.BuildingNode>> tree() {
    return ApiResponse.ok(dormService.tree());
  }

  @GetMapping("/beds/free")
  public ApiResponse<List<DormService.BedPlace>> freeBeds() {
    return ApiResponse.ok(dormService.freeBeds());
  }

  @PostMapping("/buildings/{id}/floors")
  public ApiResponse<Floor> createFloor(
      @PathVariable Long id, @Valid @RequestBody DormService.FloorRequest request) {
    return ApiResponse.ok(dormService.createFloor(id, request));
  }

  @PutMapping("/floors/{id}")
  public ApiResponse<Floor> updateFloor(
      @PathVariable Long id, @Valid @RequestBody DormService.FloorUpdateRequest request) {
    return ApiResponse.ok(dormService.updateFloor(id, request));
  }

  @DeleteMapping("/floors/{id}")
  public ApiResponse<Void> deleteFloor(@PathVariable Long id) {
    dormService.deleteFloor(id);
    return ApiResponse.ok();
  }

  @GetMapping("/floors/{id}/occupants")
  public ApiResponse<List<DormService.OccupantRow>> floorOccupants(@PathVariable Long id) {
    return ApiResponse.ok(dormService.occupantsOfFloor(id));
  }

  @PostMapping("/floors/{id}/rooms")
  public ApiResponse<DormRoom> createRoom(
      @PathVariable Long id, @Valid @RequestBody DormService.RoomRequest request) {
    return ApiResponse.ok(dormService.createRoom(id, request));
  }

  @PutMapping("/rooms/{id}")
  public ApiResponse<DormRoom> updateRoom(
      @PathVariable Long id, @Valid @RequestBody DormService.RoomUpdateRequest request) {
    return ApiResponse.ok(dormService.updateRoom(id, request));
  }

  @DeleteMapping("/rooms/{id}")
  public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
    dormService.deleteRoom(id);
    return ApiResponse.ok();
  }

  @GetMapping("/rooms/{id}/occupants")
  public ApiResponse<List<DormService.OccupantRow>> roomOccupants(@PathVariable Long id) {
    return ApiResponse.ok(dormService.occupantsOfRoom(id));
  }

  @PostMapping("/beds/{id}/assign")
  public ApiResponse<DormService.BedPlace> assign(
      @PathVariable Long id, @Valid @RequestBody AssignRequest request) {
    return ApiResponse.ok(dormService.assign(id, request.getStudentId()));
  }

  @PostMapping("/beds/batch-assign")
  public ApiResponse<List<DormService.BedPlace>> batchAssign(
      @Valid @RequestBody DormService.BatchAssignRequest request) {
    return ApiResponse.ok(dormService.batchAssign(request));
  }

  @PostMapping("/beds/{id}/release")
  public ApiResponse<DormService.BedPlace> release(@PathVariable Long id) {
    return ApiResponse.ok(dormService.release(id));
  }

  @Data
  public static class AssignRequest {
    @NotNull(message = "请选择学员")
    private Long studentId;
  }
}
