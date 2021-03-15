package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING;
import static org.highmed.dsf.bpe.ConstantsDataSharing.CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE;
import static org.highmed.dsf.bpe.ConstantsDataSharing.EXTENSION_HIGHMED_GROUP_ID;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.variable.QueryResult;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DownloadMultiMedicResultSets extends DownloadResultSets
{
	public DownloadMultiMedicResultSets(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ObjectMapper openEhrObjectMapper)
	{
		super(clientProvider, taskHelper, openEhrObjectMapper);
	}

	@Override
	protected List<QueryResult> getQueryResults(DelegateExecution execution)
	{
		Task task = getCurrentTaskFromExecutionVariables();
		Reference requester = task.getRequester();

		return getTaskHelper().getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_DATA_SHARING,
				CODESYSTEM_HIGHMED_DATA_SHARING_VALUE_MULTI_MEDIC_RESULT_SET_REFERENCE, EXTENSION_HIGHMED_GROUP_ID)
				.map(input -> toQueryResult(input, requester)).collect(Collectors.toList());
	}

	private QueryResult toQueryResult(Task.ParameterComponent input, Reference requester)
	{
		String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
		String resultSetUrl = ((Reference) input.getValue()).getReference();

		return QueryResult.idResult(requester.getIdentifier().getValue(), cohortId, resultSetUrl);
	}
}
