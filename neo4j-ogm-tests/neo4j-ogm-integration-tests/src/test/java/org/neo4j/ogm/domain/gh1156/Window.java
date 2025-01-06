/*
 * Copyright (c) 2002-2025 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.gh1156;

import java.io.Serializable;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Version;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Window implements Serializable, Canvas {

  private static final long serialVersionUID = 1L;

  private String suid_ = java.util.UUID.randomUUID().toString();

  public Class<?> getTypeClazz() {
    return Window.class;
  }

  @Id @GeneratedValue public Long __id;

  @Version
  @Property(name = "V_WINDOW")
  private long vWindow;

  public long getVwindow() {
    return vWindow;
  }

  public void setVwindow(long vWindow) {
    this.vWindow = vWindow;
  }

  @Property(name = "UID")
  private String uid;

  public void setUid(java.lang.String uid) {
    this.uid = uid;
  }

  public String getUid() {
    return uid;
  }

  @Property(name = "NAME")
  private String name;

  public void setName(java.lang.String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setSuidCanvas(java.lang.String suid) {
    this.suid_ = suid;
  }

  public String getSuidCanvas() {
    return this.suid_;
  }

  public void setSuid(java.lang.String suid) {
    this.suid_ = suid;
  }

  public String getSuid() {
    return this.suid_;
  }

  @Property(name = "CANVAS_NAME")
  private String canvasName;

  public void setCanvasName(java.lang.String canvasName) {
    this.canvasName = canvasName;
  }

  public String getCanvasName() {
    return canvasName;
  }

  public void setSuidUObject(java.lang.String suid) {
    this.suid_ = suid;
  }

  public String getSuidUObject() {
    return this.suid_;
  }

  @Property(name = "OBJ_NAME")
  private String objName;

  public void setObjName(java.lang.String objName) {
    this.objName = objName;
  }

  public String getObjName() {
    return objName;
  }
}
