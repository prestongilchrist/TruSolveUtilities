/**
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version 
 * 1.1 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at 
 * http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * The Original Code is truSolve Utilities.
 * 
 * The Initial Developer of the Original Code is
 * truSolve.com.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * ***** END LICENSE BLOCK *****
 *
 * $Id$
 *
 */

package com.trusolve.ant.filters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.tools.ant.filters.BaseParamFilterReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

/**
 * @author Preston Gilchrist
 *
 */
public class JsonToYamlFilter extends BaseObjectMapperParamFilterReader {

  @SuppressWarnings("unused")
  private static final String CLASS_ID = "$Id$";

  public JsonToYamlFilter(Reader in) throws IOException {
    super(in);
  }

  @Override
  protected Reader getReader() throws IOException {
    ObjectMapper jsonIn = new ObjectMapper(this.jsonFactory);
    JsonNode jn = jsonIn.readTree(this.originalReader);

    StringWriter sw = new StringWriter();

    YAMLGenerator yg = this.yamlFactory.createGenerator(sw);
    yg.setCodec(new ObjectMapper(this.yamlFactory));
    yg.writeObject(jn);

    return new StringReader(sw.toString());
  }
}
