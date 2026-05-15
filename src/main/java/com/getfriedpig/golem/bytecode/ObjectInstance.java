package com.getfriedpig.golem.bytecode;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a runtime instance of a class
 * Stores class metadata reference and instance properties
 */
public class ObjectInstance {
  private int classId;
  private Map<String, Object> properties;
  private Map<String, String> propertyVisibility;

  public ObjectInstance(int classId) {
    this.classId = classId;
    this.properties = new HashMap<>();
    this.propertyVisibility = new HashMap<>();
  }

  public int getClassId() {
    return classId;
  }

  public Object getProperty(String name) throws Exception {
    if (!properties.containsKey(name)) {
      return null;  // Property doesn't exist yet, return null
    }
    
    // Check visibility
    String visibility = propertyVisibility.getOrDefault(name, "public");
    if ("private".equals(visibility)) {
      // In a full implementation, we'd check if access is from within the class
      // For now, allow access
    }
    
    return properties.get(name);
  }

  public void setProperty(String name, Object value, String visibility) {
    properties.put(name, value);
    propertyVisibility.put(name, visibility == null ? "public" : visibility);
  }

  public void setProperty(String name, Object value) {
    setProperty(name, value, "public");
  }

  public Map<String, Object> getAllProperties() {
    return new HashMap<>(properties);
  }

  @Override
  public String toString() {
    return String.format("ObjectInstance{class=%d, properties=%s}", classId, properties);
  }
}
