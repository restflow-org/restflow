package org.restflow.reporter;

import java.util.HashMap;
import java.util.Map;

import org.restflow.metadata.RunMetadata;
import org.restflow.util.PortableIO;
import org.yaml.snakeyaml.Yaml;


public class YamlErrorReporter extends BaseReporter {

	RunMetadata _metadata;

	public YamlErrorReporter( RunMetadata  metadata ) {
		super();
		_metadata = metadata;
	}

	@Override
	public String getReport() throws Exception {
		Yaml yaml = new Yaml();
		try {
			Map<String, Object> emptyModel = new HashMap<String,Object>();
			emptyModel.put("errors", _metadata.getRestoreErrors() );
			emptyModel.put("meta", _metadata.getMetadataStorageProperties());
			return yaml.dump(emptyModel) + PortableIO.EOL;
		} catch (Exception e) {
			_reportGenerationErrors.add( e.getMessage());
			throw e;
		}
	}

	
}
