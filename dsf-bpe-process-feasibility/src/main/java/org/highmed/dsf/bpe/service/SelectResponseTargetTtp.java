package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGET;
import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE;
import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_TTP;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.EndpointProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.TargetValues;
import org.hl7.fhir.r4.model.Task;
import org.springframework.beans.factory.InitializingBean;

public class SelectResponseTargetTtp extends AbstractServiceDelegate implements InitializingBean
{
	private final EndpointProvider endpointProvider;

	public SelectResponseTargetTtp(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, EndpointProvider endpointProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.endpointProvider = endpointProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(endpointProvider, "endpointProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		String ttpIdentifier = (String) execution.getVariable(BPMN_EXECUTION_VARIABLE_TTP_IDENTIFIER);
		String correlationKey = getCorrelationKey(execution);

		Target ttpTarget = Target.createBiDirectionalTarget(ttpIdentifier,
				endpointProvider.getFirstConsortiumEndpointAdress(
						NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER_HIGHMED_CONSORTIUM,
						CODESYSTEM_HIGHMED_ORGANIZATION_TYPE, CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_TTP,
						ttpIdentifier).get(),
				correlationKey);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGET, TargetValues.create(ttpTarget));
	}

	private String getCorrelationKey(DelegateExecution execution)
	{
		Task task = getCurrentTaskFromExecutionVariables();

		return getTaskHelper()
				.getFirstInputParameterStringValue(task, CODESYSTEM_HIGHMED_BPMN,
						CODESYSTEM_HIGHMED_BPMN_VALUE_CORRELATION_KEY)
				.orElseThrow(() -> new IllegalStateException(
						"No correlation key found, this error should have been caught by resource validation"));
	}
}
