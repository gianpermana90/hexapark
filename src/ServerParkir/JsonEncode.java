/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerParkir;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author Hades
 */
public class JsonEncode {

    public static void main(String[] args) {
        JSONObject obj = new JSONObject();

        obj.put("name", "foo");
        obj.put("num", new Integer(100));
        obj.put("balance", new Double(1000.21));
        obj.put("is_vip", new Boolean(true));

        StringWriter out = new StringWriter();
        try {
            obj.writeJSONString(out);
        } catch (IOException ex) {
            Logger.getLogger(JsonEncode.class.getName()).log(Level.SEVERE, null, ex);
        }

        String jsonText = out.toString();
        System.out.print(jsonText);
    }
}
