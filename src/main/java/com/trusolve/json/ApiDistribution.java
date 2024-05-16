package com.trusolve.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import org.apache.commons.lang.StringUtils;

/**
 * Class to process a swagger file, reading the include aliases, processing those files to include
 * resource endpoints that have been designated as being included on this API.
 *
 * @author Preston
 * @version 1
 *
 */
public class ApiDistribution {
  private static final String API_SETTINGS = "x-api-settings";
  private static final String API_INCLUDE = "x-api-include";
  private static final String API_INCLUDE_ALL = "x-api-includeAll";
  private static final String API_INCLUDE_LIMIT_VERB = "x-api-includeAllLimitVerb";
  private static final String PATHS = "paths";

  private enum Verb {
    head, get, post, put, delete, patch, options;
    public static boolean isVerb(final String verb){
      for( Verb v : Verb.values() ){
        if( StringUtils.equals(v.toString(), verb)){
          return true;
        }
      }
      return false;
    }
  }

  private String apiName = null;
  private ObjectNode includeAllConfig = null;
  private ArrayNode includeAllLimitVerb = null;

  private ObjectNode rootDocument = null;
  private ObjectNode pathsNode = null;

  /**
   * Test main method to allow running this on the command line. Output will be on STDOUT.
   *
   * @param args
   *          First argument should be the file name to be processes.
   */
  public static void main(String[] args) {
    try {
      FileReader fr = new FileReader(args[0]);
      ObjectMapper mapper;
      if (args[0].endsWith(".yaml")) {
        mapper = new ObjectMapper(new YAMLFactory());
      } else {
        mapper = new ObjectMapper();
      }
      ObjectNode jn = (ObjectNode) mapper.readTree(fr);
      ApiDistribution ad = new ApiDistribution(jn);
      Reader r = ad.getReader();
      int c;
      while ((c = r.read()) > -1) {
        System.out.print((char) c);
      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Create a new instance of this object by processing the Swagger supplied by the reader.
   *
   * @param in
   *          Reader supplying the swagger definition.
   */
  public ApiDistribution(Reader in) {
    ObjectMapper jsonIn = new ObjectMapper();
    try {
      rootDocument = (ObjectNode) jsonIn.readTree(in);

      processApi();
    } catch (Exception e) {
      throw new RuntimeException("Could not parse json", e);
    }
  }

  /**
   * Create a new instance of this object by processing the Swagger supplied by the reader.
   *
   * @param rootDocument
   *          Json Document supplying the swagger definition.
   */
  public ApiDistribution(ObjectNode rootDocument) {
    try {
      this.rootDocument = rootDocument;

      processApi();
    } catch (Exception e) {
      throw new RuntimeException("Could not parse json", e);
    }
  }

  /**
   * Get a ready to read the resulting swagger file after processing.
   *
   * @return Reader that supplies the resulting swagger file.
   */
  public Reader getReader() {
    if (rootDocument == null) {
      return new StringReader("");
    }
    ObjectMapper om = new ObjectMapper();

    om.enable(SerializationFeature.INDENT_OUTPUT);
    ObjectWriter ow = om.writer().withDefaultPrettyPrinter();

    String document;
    try {
      document = ow.writeValueAsString(rootDocument);
    } catch (Exception e) {
      throw new RuntimeException("Could not process document.", e);
    }
    return new StringReader(document);
  }

  private void processApi() throws JsonProcessingException, IOException {
    try {
      apiName = rootDocument.get("info").get("title").asText();
    } catch (Exception e1) {
      return;
    }
    ObjectNode refAliases = (ObjectNode) rootDocument.get("$refAliases");

    if (refAliases == null) {
      return;
    }
    this.includeAllConfig = (ObjectNode) rootDocument.get(API_INCLUDE_ALL);
    final JsonNode limit = rootDocument.get(API_INCLUDE_LIMIT_VERB);
    if (limit != null && limit.isArray()) {
      this.includeAllLimitVerb = (ArrayNode)limit;
    }
    for (Iterator<Map.Entry<String, JsonNode>> i = refAliases.fields(); i.hasNext();) {
      Map.Entry<String, JsonNode> e = i.next();
      String alias = e.getKey();
      String fileName = e.getValue().asText();
      processIncludeApi(alias, fileName);
    }
  }

  private void processIncludeApi(String apiRefAlias, String apiDefinitionFilePath) {
    ObjectMapper om = new ObjectMapper();
    ObjectNode currentDocument;
    try {
      currentDocument = (ObjectNode) om.readTree(new File(new URI(apiDefinitionFilePath)));
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse Swagger file", e);
    }
    if (!currentDocument.isObject() || !currentDocument.has(PATHS)) {
      return;
    }

    processPaths((ObjectNode) currentDocument.get(API_SETTINGS), apiRefAlias,
        (ObjectNode) currentDocument.get(PATHS));
  }

  private void processPaths(final ObjectNode includeConfig, final String apiRefAlias,
      final ObjectNode currentPathsNode) {
    for (Iterator<Map.Entry<String, JsonNode>> i = currentPathsNode.fields(); i.hasNext();) {
      Map.Entry<String, JsonNode> e = i.next();
      processPath(includeConfig, apiRefAlias, e.getKey(), (ObjectNode) e.getValue());
    }
  }

  private void processPath(final ObjectNode includeConfig, final String apiRefAlias,
      final String path, final ObjectNode currentPathNode) {
    boolean addAttributes = false;
    ObjectNode pathConfig = (ObjectNode) currentPathNode.get(API_SETTINGS);

    for (Verb v : Verb.values()) {
      if (currentPathNode.has(v.toString())) {
        if (processVerb(includeConfig, v.toString(), (ObjectNode) currentPathNode.get(v.toString()),
              path, pathConfig, apiRefAlias)){
          // if a verb was added, make sure we include the parameters
          addAttributes = true;
        }
      }
    }
    if ( addAttributes ){
      final Iterator<Entry<String, JsonNode>> i =  currentPathNode.fields();
      while(i.hasNext()) {
        final Entry<String, JsonNode> entry = i.next();
        final String key = entry.getKey();
        if( ! Verb.isVerb(key) ){
          final ObjectNode pathNode = getPathNode(path);
          final ObjectNode newParametersNode = pathNode.putObject(key);
          newParametersNode.put("$ref", constructPathVerbRef(apiRefAlias, path, key));
          newParametersNode.put("$refDeep", true);
        }
      }
    }
  }

  private String constructPathVerbRef(final String apiRefAlias, final String path, final String verb) {
    StringBuilder sb = new StringBuilder();
    sb.append("@");
    sb.append(apiRefAlias);
    sb.append("#/paths/");
    sb.append(jsonPathEscapePath(path));
    sb.append("/");
    sb.append(verb);
    return sb.toString();
  }

  private String jsonPathEscapePath(final String path) {
    return path.replace("/", "~1").replace("{", "%7b").replace("}", "%7d");
  }

  private boolean processVerb(final ObjectNode includeConfig, final String verb,
      final ObjectNode verbNode, final String path, final ObjectNode pathConfig, final String apiRefAlias) {
    final ObjectNode verbConfig = (ObjectNode) verbNode.get(API_SETTINGS);
    final ObjectNode apiIncludeNode = (ObjectNode) verbNode.get(API_INCLUDE);
    boolean includeAll = false;
    if (this.includeAllConfig != null) {
      if (this.includeAllLimitVerb != null) {
        for (Iterator<JsonNode> i = this.includeAllLimitVerb.elements(); i.hasNext();) {
          JsonNode n = i.next();
          if (StringUtils.equals(verb, n.asText())) {
            includeAll = true;
          }
        }
      } else {
        includeAll = true;
      }
    }
    if (!includeAll && ( apiIncludeNode == null || ( !apiIncludeNode.has(apiName) && !apiIncludeNode.has("*")))) {
      return false;
    }
    final ObjectNode pathNode = getPathNode(path);
    
    final ObjectNode newVerbNode = pathNode.putObject(verb);
    if (this.includeAllConfig != null) {
      newVerbNode.setAll(this.includeAllConfig);
    }
    if (includeConfig != null) {
      newVerbNode.setAll(includeConfig);
    }
    if (pathConfig != null) {
      newVerbNode.setAll(pathConfig);
    }
    if (verbConfig != null) {
      newVerbNode.setAll(verbConfig);
    }
    if (apiIncludeNode!=null) {
      if (apiIncludeNode.has(apiName)) {
        newVerbNode.setAll((ObjectNode) apiIncludeNode.get(apiName));
      } else if (apiIncludeNode.has("*")) {
        newVerbNode.setAll((ObjectNode) apiIncludeNode.get("*"));
      } 
    }
    newVerbNode.put("$ref", constructPathVerbRef(apiRefAlias, path, verb));
    newVerbNode.put("$refDeep", true);
    
    return true;
  }

  private ObjectNode getPathsNode() {
    if (this.pathsNode == null) {
      try {
        this.pathsNode = (ObjectNode) rootDocument.get("paths");
      } catch (Exception e) {
          this.pathsNode = this.rootDocument.putObject("paths");
      }
      if (this.pathsNode == null) {
        this.pathsNode = this.rootDocument.putObject("paths");
      }
    }
    return this.pathsNode;
  }
  
  private ObjectNode getPathNode(final String path){
	  JsonNode pathNode = getPathsNode().get(path);
	  if( pathNode != null && pathNode.isObject() ){
		  return (ObjectNode)pathNode;
	  }
	  return getPathsNode().putObject(path);
  }
}
