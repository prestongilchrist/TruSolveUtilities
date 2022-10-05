package com.trusolve.ant.filters;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.types.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.trusolve.io.DeferredReader;

public abstract class BaseObjectMapperParamFilterReader extends BaseParamFilterReader {
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseObjectMapperParamFilterReader.class);
  
  protected Reader originalReader;
  protected DeferredReader deferredReader;
  protected JsonFactory jsonFactory = new JsonFactory();
  protected YAMLFactory yamlFactory = new YAMLFactory();
  private boolean initialized = false;

  public BaseObjectMapperParamFilterReader(Reader in) throws IOException {
    this(new DeferredReader());
    originalReader = in;
  }

  private BaseObjectMapperParamFilterReader(DeferredReader in) {
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

  private void initialize() throws IOException {
    if (initialized) {
      return;
    }
    for (Parameter p : this.getParameters()) {
      final String paramName = p.getName();
      
      if( StringUtils.isEmpty(paramName) ){
        continue;
      }
      
      final String[] components = paramName.split("\\.");
      
      if( components.length < 2 ){
        continue;
      }
      
      final String value = p.getValue();
      if( "JsonGenerator".equals(components[0])){
        if( "enable".equals(value) ){
          initJsonGeneratorFeature(components[1], true);
        } else if ("disable".equals(value) ){
          initJsonGeneratorFeature(components[1], false);
        }        
      }
      if( "JsonParser".equals(components[0])){
        if( "enable".equals(value) ){
          initJsonParserFeature(components[1], true);
        } else if ("disable".equals(components[0]) ){
          initJsonParserFeature(components[1], false);
        }        
      }
      if( "YAMLGenerator".equals(components[0])){
        if( "enable".equals(value) ){
          initYAMLGeneratorFeature(components[1], true);
        } else if ("disable".equals(value) ){
          initYAMLGeneratorFeature(components[1], false);
        }        
      }
      if( "YAMLParser".equals(components[0])){
        if( "enable".equals(value) ){
          initYAMLParserFeature(components[1], true);
        } else if ("disable".equals(value) ){
          initYAMLParserFeature(components[1], false);
        }        
      }
    }
    this.deferredReader.setReader(getReader());

    initialized = true;
  }


  private void initYAMLGeneratorFeature(final String featureName, boolean state){
    try{
      yamlFactory.configure(YAMLGenerator.Feature.valueOf(featureName), state);
    } catch ( Exception e ){
      LOGGER.error("Unable to set YAMLGenerator.Feature \"" + featureName + "\" to " + Boolean.toString(state), e);
    }
  }

  private void initYAMLParserFeature(final String featureName, boolean state){
    try{
      yamlFactory.configure(YAMLParser.Feature.valueOf(featureName), state);
    } catch ( Exception e ){
      LOGGER.error("Unable to set YAMLParser.Feature \"" + featureName + "\" to " + Boolean.toString(state), e);
    }
  }

  private void initJsonGeneratorFeature(final String featureName, boolean state){
    try{
      jsonFactory.configure(JsonGenerator.Feature.valueOf(featureName), state);
    } catch ( Exception e ){
      LOGGER.error("Unable to set JsonGenerator.Feature \"" + featureName + "\" to " + Boolean.toString(state), e);
    }
  }

  private void initJsonParserFeature(final String featureName, boolean state){
    try{
      jsonFactory.configure(JsonParser.Feature.valueOf(featureName), state);
    } catch ( Exception e ){
      LOGGER.error("Unable to set JsonGenerator.Feature \"" + featureName + "\" to " + Boolean.toString(state), e);
    }
  }

  protected abstract Reader getReader() throws IOException;
}
