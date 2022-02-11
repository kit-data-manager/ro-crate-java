package edu.kit.crate.objectmapper;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Nikola Tzotchev on 4.2.2022 г.
 * @version 1
 */
public class MyObjectMapper {

  private static ObjectMapper mapper = new ObjectMapper().enable(
          SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED).enable(
          DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  private MyObjectMapper() {

  }

  public static ObjectMapper getMapper() {
    return mapper;
  }
}
