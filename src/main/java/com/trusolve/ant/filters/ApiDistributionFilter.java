package com.trusolve.ant.filters;

import java.io.IOException;
import java.io.Reader;

import org.apache.tools.ant.filters.BaseParamFilterReader;

import com.trusolve.json.ApiDistribution;

/**
 * This is a filter class that will process include files into a Swagger definition supplied on the
 * Reader "in" and will include the resources designated. The outbound read of the filter will then
 * supply the modified swagger.
 *
 * @author Preston
 * @version 1
 *
 */
public class ApiDistributionFilter extends BaseParamFilterReader {
  /**
   * Standard constructor for a filter reader.
   *
   * @param in
   *          The reader that will supply the swagger to be transformed.
   * @throws IOException
   *           If an io problem occurs.
   */
  public ApiDistributionFilter(Reader in) throws IOException {
    super(new ApiDistribution(in).getReader());
  }
}
