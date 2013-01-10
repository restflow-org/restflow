package org.restflow.reporter;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

public class YamlReporter extends BaseReporter {

	Map<String,Object> structure;

	@Override
	public String getReport( ) throws Exception {
		Yaml yaml = new Yaml();
		try {
			
			Map<String,Object> emptyModel = new HashMap<String,Object>();			
			addMapToModel(emptyModel, structure);
			//for (String key : modelModel.keySet()) {
			//	model.put(key, reportModel.get(key));
			//}
			
			return yaml.dump(emptyModel);
		} catch (Exception e) {
			_reportGenerationErrors.add( e.getMessage());
			throw e;
		} 
	}

	private void addMapToModel( Map<String,Object> outputModel, Map<String,Object> structure) throws Exception {
		for (String key : structure.keySet())  {
			Object value = structure.get(key);

			if (value instanceof Map) {
				Map<String, Object> embeddedMap = new HashMap<String,Object>();
				outputModel.put(key, embeddedMap);
				addMapToModel(embeddedMap,(Map<String,Object>)value);
				continue;
			}
			
			if (value instanceof List) {
				List<Object> embeddedList = new Vector<Object>();
				outputModel.put(key, embeddedList);
				addListToModel(embeddedList,(List<Object>)value);
				continue;
			}

			
			Object published = _reportModel.get(value);
			
			if (published instanceof File) {
				try {
					File file = (File)published;
					String contents = IOUtils.toString(new FileInputStream(file));
					outputModel.put(key, contents);				
				} catch (Exception e) {
					outputModel.put(key, null);
				}
			} else {
				outputModel.put(key, published);
			}
			
		}
			
	}		
		
	private void addListToModel(List<Object> outputModel,
			List<Object> structure) throws Exception {
		for (Object value : structure) {
			if (value instanceof Map) {
				Map<String, Object> embeddedMap = new HashMap<String, Object>();
				outputModel.add(embeddedMap);
				addMapToModel(embeddedMap, (Map<String, Object>) value);
				continue;
			}

			if (value instanceof List) {
				List<Object> embeddedList = new Vector<Object>();
				outputModel.add(embeddedList);
				addListToModel(embeddedList, (List<Object>) value);
				continue;
			}
	
			Object published = _reportModel.get(value);
			
			if (published instanceof File) {
				File file = (File)published;
				try { 
					String contents = IOUtils.toString(new FileInputStream(file));
					outputModel.add(contents);
				} catch (Exception e) {
					outputModel.add(null);
				}
				
			} else {
				outputModel.add(published);
			}
		}
	}

	public Map<String, Object> getStructure() {
		return structure;
	}

	public void setStructure(Map<String, Object> structure) {
		this.structure = structure;
	}
	
	
}
