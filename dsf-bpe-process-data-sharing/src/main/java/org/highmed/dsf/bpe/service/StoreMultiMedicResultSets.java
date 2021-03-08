package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StoreMultiMedicResultSets extends StoreResultSets
{
	public StoreMultiMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper, openEhrObjectMapper);
	}

	@Override
	protected String getSecurityIdentifier(DelegateExecution execution)
	{
		return (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_LEADING_MEDIC_IDENTIFIER);
	}
}
