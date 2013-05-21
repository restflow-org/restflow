package org.restflow.reporter;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.restflow.util.PortableIO;
import org.yaml.snakeyaml.Yaml;


public class UnixProcessStatusReporter extends BaseReporter {


	public UnixProcessStatusReporter() {
		super();
		this._includeWorkflowInputs = true;
	}

	@Override
	public String getReport( ) throws Exception {

		Map<String, Object> meta = (Map<String,Object>)_reportModel.get("meta");
		String running = "";
		String hostname = System.getenv("HOSTNAME");
		if (hostname == null ) {
			hostname = System.getenv("HOST");
		}
		if ( hostname == null || !hostname.equals( meta.get("host")) ) {
			running = "unknown";
		} else {
			String cmd[] = {"sh","-c", "ps -eo \"pid\" | grep "+ meta.get("pid") };
			ProcessBuilder pb = new ProcessBuilder(cmd).redirectErrorStream(true);
			Process p = pb.start();
			String ps = IOUtils.toString(p.getInputStream());
			if (ps.equals("") ) {
				running= "false";
			} else {
				running = "true";
			}	
	 	}

		meta.put("running",running);
	      
		Map<String,Object> status = new HashMap<String,Object>();
		status.put("meta", meta);
		status.put("inputs", _reportModel.get("inputs"));
		
		Yaml yaml = new Yaml();
	    return yaml.dump(status) + PortableIO.EOL;
	}	
}
