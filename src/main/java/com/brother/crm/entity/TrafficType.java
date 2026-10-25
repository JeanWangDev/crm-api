package com.brother.crm.entity;

/** 怎么来：深圳北站 / 深圳站 / 大巴 / 飞机 / 家人送 / 自驾 / 自行到达 */
public enum TrafficType {
  SZ_NORTH,
  SZ_STATION,
  BUS,
  FLIGHT,
  FAMILY,
  DRIVE,
  SELF,

  /** @deprecated 旧数据兼容，界面不再展示 */
  TRAIN_1,
  /** @deprecated 旧数据兼容，界面不再展示 */
  TRAIN_2
}
