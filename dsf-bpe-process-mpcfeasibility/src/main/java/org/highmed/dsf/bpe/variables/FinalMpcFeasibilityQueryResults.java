package org.highmed.dsf.bpe.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FinalMpcFeasibilityQueryResults
{
	private final List<FinalMpcFeasibilityQueryResult> results = new ArrayList<>();

	@JsonCreator
	public FinalMpcFeasibilityQueryResults(
			@JsonProperty("results") Collection<? extends FinalMpcFeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public void add(FinalMpcFeasibilityQueryResult newResult)
	{
		if (newResult != null)
			results.add(newResult);
	}

	public void addAll(Collection<FinalMpcFeasibilityQueryResult> results)
	{
		if (results != null)
			this.results.addAll(results);
	}

	public List<FinalMpcFeasibilityQueryResult> getResults()
	{
		return Collections.unmodifiableList(results);
	}
}
