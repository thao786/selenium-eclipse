package selenium;

import java.io.IOException;
import java.io.StringWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

class JsonEncodeDemo {

   public static void main(String[] args) throws IOException, org.json.simple.parser.ParseException{
//      JSONObject obj = new JSONObject();
//
//      obj.put("name","foo");
//      obj.put("num",new Integer(100));
//      obj.put("balance",new Double(1000.21));
//      obj.put("is_vip",new Boolean(true));
//
//      StringWriter out = new StringWriter();
//      obj.writeJSONString(out);
//      
//      String jsonText = out.toString();
//      System.out.print(jsonText);
      
      
		JSONParser parser = new JSONParser();
		String selectors = "{\"selector\": \".container\"}";
		JSONObject hash = (JSONObject) parser.parse(selectors);
		System.out.println((String) hash.get("selector"));
		System.out.println((String) hash.get("eq"));
   }
}