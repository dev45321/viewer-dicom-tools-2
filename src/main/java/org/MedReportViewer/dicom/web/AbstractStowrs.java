/*
 * Copyright (c) 2018-2019 MedReportViewer Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.MedReportViewer.dicom.web;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.xml.parsers.ParserConfigurationException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.BulkData;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.SAXReader;
import org.dcm4che3.util.DateUtils;
import org.dcm4che3.util.UIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.MedReportViewer.dicom.web.Multipart.ContentType;
import org.xml.sax.SAXException;

public class AbstractStowrs implements AutoCloseable {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStowrs.class);
  /** @see <a href="https://tools.ietf.org/html/rfc2387">multipart specifications</a> */
  protected static final String MULTIPART_BOUNDARY = "mimeTypeBoundary";

  private final List<HttpURLConnection> connections;
  private final ContentType contentType;
  private final String requestURL;
  protected final String agentNameValue;
  protected final String contentTypeValue;
  private final Map<String, String> headers;

  /**
   * @param requestURL the URL of the STOW service
   * @param contentType the value of the type in the Content-Type HTTP property
   * @param agentName the value of the User-Agent HTTP property
   * @param headers some additional header properties.
   */
  public AbstractStowrs(
      String requestURL,
      Multipart.ContentType contentType,
      String agentName,
      Map<String, String> headers) {
    this.contentType = Objects.requireNonNull(contentType);
    this.requestURL = Objects.requireNonNull(getFinalUrl(requestURL), "requestURL cannot be null");
    this.headers = headers;
    this.agentNameValue = agentName;
    this.contentTypeValue =
        Multipart.MULTIPART_RELATED
            + "; type=\""
            + contentType
            + "\"; boundary="
            + MULTIPART_BOUNDARY;
    this.connections = new ArrayList<>();
  }

  private String getFinalUrl(String requestURL) {
    String url = requestURL.trim();
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    if (!url.endsWith("/studies")) {
      url += "/studies";
    }
    return url;
  }

  protected HttpURLConnection buildConnection() throws IOException {
    try {

      URL url = new URL(requestURL);
      HttpURLConnection httpPost = (HttpURLConnection) url.openConnection();

      httpPost.setUseCaches(false);
      httpPost.setDoOutput(true); // indicates POST method
      httpPost.setDoInput(true);
      httpPost.setRequestMethod("POST");
      httpPost.setConnectTimeout(10000);
      httpPost.setReadTimeout(60000);
      httpPost.setRequestProperty("Content-Type", contentTypeValue);
      httpPost.setRequestProperty(
          "User-Agent", agentNameValue == null ? "MedReportViewer STOWRS" : agentNameValue);
      httpPost.setRequestProperty(
          "Accept",
          contentType == ContentType.JSON
              ? ContentType.JSON.toString()
              : ContentType.XML.toString());

      if (headers != null && !headers.isEmpty()) {
        for (Entry<String, String> element : headers.entrySet()) {
          httpPost.setRequestProperty(element.getKey(), element.getValue());
        }
      }
      connections.add(httpPost);
      return httpPost;

    } catch (IOException e) {
      try {
        close();
      } catch (Exception e1) {
        // Do nothing
      }
      throw e;
    }
  }

  private void endMarkers(DataOutputStream out) throws IOException {
    // Final part segment
    out.write(Multipart.Separator.BOUNDARY.getType());
    out.writeBytes(MULTIPART_BOUNDARY);
    out.write(Multipart.Separator.STREAM.getType());
    out.flush();
    out.close();
  }

  protected void writeContentMarkers(DataOutputStream out) throws IOException {
    out.write(Multipart.Separator.BOUNDARY.getType());
    out.writeBytes(MULTIPART_BOUNDARY);
    out.write(Multipart.Separator.FIELD.getType());
    out.writeBytes("Content-Type: ");
    out.writeBytes(contentType.toString());
    out.write(Multipart.Separator.HEADER.getType());
  }

  protected void writeEndMarkers(HttpURLConnection httpPost, DataOutputStream out, String iuid)
      throws IOException {
    endMarkers(out);

    int code = httpPost.getResponseCode();
    if (code == HttpURLConnection.HTTP_OK) {
      LOGGER.info("STOWRS server response message: HTTP Status-Code 200: OK for {}", iuid);
    } else {
      throw new HttpServerErrorException(
          String.format("STOWRS server response message: %s", httpPost.getResponseMessage()));
    }
  }

  protected Attributes writeEndMarkers(HttpURLConnection httpPost, DataOutputStream out)
      throws IOException, ParserConfigurationException, SAXException {
    endMarkers(out);

    int code = httpPost.getResponseCode();
    if (code == HttpURLConnection.HTTP_OK) {
      LOGGER.info("STOWRS server response message: HTTP Status-Code 200: OK for all the image set");
    } else if (code == HttpURLConnection.HTTP_ACCEPTED || code == HttpURLConnection.HTTP_CONFLICT) {
      LOGGER.warn(
          "STOWRS server response message: HTTP Status-Code {}: {}",
          code,
          httpPost.getResponseMessage());
      // See
      // http://dicom.nema.org/medical/dicom/current/output/chtml/part18/sect_6.6.html#table_6.6.1-1
      return SAXReader.parse(httpPost.getInputStream());
    } else {
      throw new HttpServerErrorException(
          String.format(
              "STOWRS server response message: HTTP Status-Code %d: %s",
              code, httpPost.getResponseMessage()));
    }
    return null;
  }

  protected static void ensureUID(Attributes attrs, int tag) {
    if (!attrs.containsValue(tag)) {
      attrs.setString(tag, VR.UI, UIDUtils.createUID());
    }
  }

  protected static void setEncapsulatedDocumentAttributes(
      Path bulkDataFile, Attributes metadata, String mimeType) {
    metadata.setInt(Tag.InstanceNumber, VR.IS, 1);
    metadata.setString(
        Tag.ContentDate,
        VR.DA,
        DateUtils.formatDA(null, new Date(bulkDataFile.toFile().lastModified())));
    metadata.setString(
        Tag.ContentTime,
        VR.TM,
        DateUtils.formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
    metadata.setString(
        Tag.AcquisitionDateTime,
        VR.DT,
        DateUtils.formatTM(null, new Date(bulkDataFile.toFile().lastModified())));
    metadata.setString(Tag.BurnedInAnnotation, VR.CS, "YES");
    metadata.setNull(Tag.DocumentTitle, VR.ST);
    metadata.setNull(Tag.ConceptNameCodeSequence, VR.SQ);
    metadata.setString(Tag.MIMETypeOfEncapsulatedDocument, VR.LO, mimeType);
  }

  protected String getContentLocation(Attributes metadata) {
    BulkData data = ((BulkData) metadata.getValue(Tag.EncapsulatedDocument));
    if (data != null) {
      return data.getURI();
    }

    data = ((BulkData) metadata.getValue(Tag.PixelData));
    if (data != null) {
      return data.getURI();
    }
    return null;
  }

  protected void removeConnection(HttpURLConnection httpPost) {
    connections.remove(httpPost);
  }

  @Override
  public void close() throws Exception {
    connections.forEach(HttpURLConnection::disconnect);
    connections.clear();
  }

  public ContentType getContentType() {
    return contentType;
  }

  public String getRequestURL() {
    return requestURL;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }
}
