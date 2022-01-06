package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultsValues.MpcFeasibilityQueryResultsValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MpcFeasibilityQueryResultsSerializer extends PrimitiveValueSerializer<MpcFeasibilityQueryResultsValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public MpcFeasibilityQueryResultsSerializer(ObjectMapper objectMapper)
	{
		super(MpcFeasibilityQueryResultsValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(MpcFeasibilityQueryResultsValue value, ValueFields valueFields)
	{
		MpcFeasibilityQueryResults results = value.getValue();
		try
		{
			if (results != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(results));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public MpcFeasibilityQueryResultsValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return MpcFeasibilityQueryResultsValues.create((MpcFeasibilityQueryResults) untypedValue.getValue());
	}

	@Override
	public MpcFeasibilityQueryResultsValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			MpcFeasibilityQueryResults results = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, MpcFeasibilityQueryResults.class);
			return MpcFeasibilityQueryResultsValues.create(results);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
