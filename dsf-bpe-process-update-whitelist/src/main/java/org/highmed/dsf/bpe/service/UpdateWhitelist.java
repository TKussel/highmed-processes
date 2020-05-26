package org.highmed.dsf.bpe.service;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.Bundle.HTTPVerb;
import org.hl7.fhir.r4.model.Bundle.SearchEntryMode;
import org.hl7.fhir.r4.model.Endpoint;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.context.FhirContext;

public class UpdateWhitelist extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(UpdateWhitelist.class);

	private final OrganizationProvider organizationProvider;

	public UpdateWhitelist(OrganizationProvider organizationProvider, FhirWebserviceClientProvider clientProvider,
			TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	public void doExecute(DelegateExecution execution) throws Exception
	{
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getLocalWebserviceClient();

		Bundle searchSet = client.search(Organization.class,
				Map.of("active", Collections.singletonList("true"), "identifier",
						Collections.singletonList(organizationProvider.getDefaultIdentifierSystem() + "|"), "_include",
						Collections.singletonList("Organization:endpoint")));

		Bundle transaction = new Bundle().setType(BundleType.TRANSACTION);
		transaction.getMeta().addTag().setSystem("http://highmed.org/fhir/CodeSystem/authorization-role")
				.setCode("REMOTE");
		transaction.getIdentifier().setSystem(Constants.CODESYSTEM_HIGHMED_UPDATE_WHITELIST)
				.setValue(Constants.CODESYSTEM_HIGHMED_UPDATE_WHITELIST_VALUE_WHITE_LIST);
		searchSet.getEntry().stream()
				.filter(e -> e.hasSearch() && SearchEntryMode.MATCH.equals(e.getSearch().getMode()) && e.hasResource()
						&& e.getResource() instanceof Organization)
				.map(e -> (Organization) e.getResource()).forEach(addWhiteListEntry(transaction, searchSet));

		logger.debug("Uploading new white-list transaction bundle: {}",
				FhirContext.forR4().newJsonParser().encodeResourceToString(transaction));

		Bundle result = client.updateConditionaly(transaction,
				Map.of("identifier", Collections.singletonList(Constants.CODESYSTEM_HIGHMED_UPDATE_WHITELIST + "|"
						+ Constants.CODESYSTEM_HIGHMED_UPDATE_WHITELIST_VALUE_WHITE_LIST)));

		Task task = (Task) execution.getVariable(Constants.VARIABLE_LEADING_TASK);
		task.addOutput()
				.setValue(new Reference(new IdType("Bundle", result.getIdElement().getIdPart(),
						result.getIdElement().getVersionIdPart())))
				.getType().addCoding().setSystem(Constants.CODESYSTEM_HIGHMED_UPDATE_WHITELIST)
				.setCode(Constants.CODESYSTEM_HIGHMED_UPDATE_WHITELIST_VALUE_WHITE_LIST);
	}

	private Consumer<? super Organization> addWhiteListEntry(Bundle transaction, Bundle searchSet)
	{
		return organization ->
		{
			Identifier identifier = getDefaultIdentifier(organization).get();

			BundleEntryComponent organizationEntry = transaction.addEntry();
			String organizationId = "urn:uuid:" + UUID.randomUUID();
			organizationEntry.setFullUrl(organizationId);
			organizationEntry.getRequest().setMethod(HTTPVerb.PUT)
					.setUrl("Organization?identifier=" + identifier.getSystem() + "|" + identifier.getValue());

			organization.setIdElement(new IdType(organizationId));
			organization.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);
			organizationEntry.setResource(organization);

			organization.setEndpoint(organization.getEndpoint().stream()
					.map(addWhiteListEntryReturnReference(transaction, organizationId, searchSet))
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
		};
	}

	private Function<Reference, Optional<Reference>> addWhiteListEntryReturnReference(Bundle transaction,
			String organizationId, Bundle searchSet)
	{
		return endpointRef -> getEndpoint(endpointRef, searchSet).map(endpoint ->
		{
			Identifier identifier = getDefaultIdentifier(endpoint).get();

			BundleEntryComponent endpointEntry = transaction.addEntry();
			String endpointId = "urn:uuid:" + UUID.randomUUID();
			endpointEntry.setFullUrl(endpointId);
			endpointEntry.getRequest().setMethod(HTTPVerb.PUT)
					.setUrl("Endpoint?identifier=" + identifier.getSystem() + "|" + identifier.getValue());

			endpoint.setIdElement(new IdType(endpointId));
			endpoint.getMeta().setVersionIdElement(null).setLastUpdatedElement(null);
			endpoint.setManagingOrganization(new Reference().setReference(organizationId).setType("Organization"));
			endpointEntry.setResource(endpoint);

			return new Reference().setReference(endpointId).setType("Endpoint");
		});
	}

	private Optional<Identifier> getDefaultIdentifier(Organization org)
	{
		return org.getIdentifier().stream()
				.filter(i -> organizationProvider.getDefaultIdentifierSystem().equals(i.getSystem())).findFirst();
	}

	private Optional<Identifier> getDefaultIdentifier(Endpoint ept)
	{
		return ept.getIdentifier().stream().filter(i -> Constants.ENDPOINT_IDENTIFIER_SYSTEM.equals(i.getSystem()))
				.findFirst();
	}

	private Optional<Endpoint> getEndpoint(Reference endpoint, Bundle searchSet)
	{
		return searchSet.getEntry().stream()
				.filter(e -> e.hasResource() && e.getResource() instanceof Endpoint
						&& e.getFullUrl().endsWith(endpoint.getReference()))
				.map(e -> (Endpoint) e.getResource()).findFirst();
	}
}
