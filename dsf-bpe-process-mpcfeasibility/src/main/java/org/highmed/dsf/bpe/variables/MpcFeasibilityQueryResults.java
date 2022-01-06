package org.highmed.dsf.bpe.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MpcFeasibilityQueryResults
{
	private final List<MpcFeasibilityQueryResult> results = new ArrayList<>();

	@JsonCreator
	public MpcFeasibilityQueryResults(@JsonProperty("results") Collection<? extends MpcFeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public void add(MpcFeasibilityQueryResult newResult)
	{
		if (newResult != null)
			results.add(newResult);
	}

	public void addAll(Collection<MpcFeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public List<MpcFeasibilityQueryResult> getResults()
	{
		return Collections.unmodifiableList(results);
	}
}
