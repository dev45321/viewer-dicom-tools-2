/*
 * Copyright (c) 2014-2019 MedReportViewer Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.MedReportViewer.dicom;

import java.util.List;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.net.Status;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.MedReportViewer.dicom.op.CFind;
import org.MedReportViewer.dicom.param.DicomNode;
import org.MedReportViewer.dicom.param.DicomParam;
import org.MedReportViewer.dicom.param.DicomState;
import org.MedReportViewer.dicom.tool.ModalityWorklist;

public class ModalityWorklistNetTest {

  @Test
  public void testProcess() {

    // Filter by AETitle by setting a value
    final int[] sps = {Tag.ScheduledProcedureStepSequence};
    DicomParam stationAet = new DicomParam(sps, Tag.ScheduledStationAETitle, "ADVT");

    DicomParam[] RETURN_KEYS = {
      CFind.AccessionNumber,
      CFind.IssuerOfAccessionNumberSequence,
      CFind.ReferringPhysicianName,
      CFind.PatientName,
      CFind.PatientID,
      CFind.IssuerOfPatientID,
      CFind.PatientBirthDate,
      CFind.PatientSex,
      ModalityWorklist.PatientWeight,
      ModalityWorklist.MedicalAlerts,
      ModalityWorklist.Allergies,
      ModalityWorklist.PregnancyStatus,
      CFind.StudyInstanceUID,
      ModalityWorklist.RequestingPhysician,
      ModalityWorklist.RequestingService,
      ModalityWorklist.RequestedProcedureDescription,
      ModalityWorklist.RequestedProcedureCodeSequence,
      ModalityWorklist.AdmissionID,
      ModalityWorklist.IssuerOfAdmissionIDSequence,
      ModalityWorklist.SpecialNeeds,
      ModalityWorklist.CurrentPatientLocation,
      ModalityWorklist.PatientState,
      ModalityWorklist.RequestedProcedureID,
      ModalityWorklist.RequestedProcedurePriority,
      ModalityWorklist.PatientTransportArrangements,
      ModalityWorklist.PlacerOrderNumberImagingServiceRequest,
      ModalityWorklist.FillerOrderNumberImagingServiceRequest,
      ModalityWorklist.ConfidentialityConstraintOnPatientDataDescription,
      // Scheduled Procedure Step Sequence
      ModalityWorklist.Modality,
      ModalityWorklist.RequestedContrastAgent,
      stationAet,
      ModalityWorklist.ScheduledProcedureStepStartDate,
      ModalityWorklist.ScheduledProcedureStepStartTime,
      ModalityWorklist.ScheduledPerformingPhysicianName,
      ModalityWorklist.ScheduledProcedureStepDescription,
      ModalityWorklist.ScheduledProcedureStepID,
      ModalityWorklist.ScheduledStationName,
      ModalityWorklist.ScheduledProcedureStepLocation,
      ModalityWorklist.PreMedication,
      ModalityWorklist.ScheduledProcedureStepStatus,
      ModalityWorklist.ScheduledProtocolCodeSequence
    };

    DicomNode calling = new DicomNode("MedReportViewer-SCU");
    DicomNode called = new DicomNode("DICOMSERVER", "dicomserver.co.uk", 11112);
    // DicomNode called = new DicomNode("DCM4CHEE", "localhost", 11112);

    DicomState state = ModalityWorklist.process(null, calling, called, 0, RETURN_KEYS);

    // Should never happen
    Assert.assertNotNull(state);

    List<Attributes> items = state.getDicomRSP();
    for (int i = 0; i < items.size(); i++) {
      Attributes item = items.get(i);
      System.out.println("===========================================");
      System.out.println("Worklist Item " + (i + 1));
      System.out.println("===========================================");
      System.out.println(item.toString(100, 150));
    }

    System.out.println("DICOM Status:" + state.getStatus());
    System.out.println(state.getMessage());

    // see org.dcm4che3.net.Status
    // See server log at http://dicomserver.co.uk/logs/
    Assert.assertThat(state.getMessage(), state.getStatus(), IsEqual.equalTo(Status.Success));
  }
}
