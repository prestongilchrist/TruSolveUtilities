package com.trusolve.ant.filters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.trusolve.io.DeferredReader;

public class RemoveJsonAttributes extends BaseParamFilterReader {
  private Reader originalReader = null;
  private DeferredReader deferredReader = null;
  private boolean initialized = false;
  private Set<String> attributesToRemove = new HashSet<>();

  public RemoveJsonAttributes(Reader in) throws IOException {
    this(new DeferredReader());
    originalReader = in;
  }

  private RemoveJsonAttributes(DeferredReader in) {
    super(in);
    deferredReader = in;
  }

  @Override()
  public int read() throws IOException {
    if (!initialized) {
      initialize();
    }
    return (super.read());
  }

  public void initialize() throws IOException {
    for (Parameter p : this.getParameters()) {
      if ("attributesToRemove".equals(p.getName())) {
        this.attributesToRemove.add(p.getValue());
      }
    }
    this.deferredReader.setReader(getReader());

    initialized = true;
  }

  private Reader getReader() throws IOException {
    ObjectMapper jsonIn = new ObjectMapper();
    JsonNode jn = jsonIn.readTree(this.originalReader);

    removeAttributes(jn);

    ObjectMapper om = new ObjectMapper();
    om.enable(SerializationFeature.INDENT_OUTPUT);
    ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
    return new StringReader(ow.writeValueAsString(jn));
  }

  private void removeAttributes(JsonNode jn) {
    final List<String> fieldNames = new ArrayList<>();
    for ( Iterator<String> i = jn.fieldNames() ; i.hasNext() ;){
      fieldNames.add(i.next());
    }
    for ( String key : fieldNames ) {
      if (this.attributesToRemove.contains(key)) {
        ((ObjectNode) jn).remove(key);
      } else {
        removeAttributes(jn.get(key));
      }
    }
  }
}
