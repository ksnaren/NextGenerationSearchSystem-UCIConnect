import java.io.BufferedReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.gson.Gson;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/UCIConnectServer")
public class UCIConnectServer extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public static JSONObject readJsonFromUrl(String url) throws IOException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = (JSONObject) JSONValue.parse(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	public static JSONArray readJsonFromUrlId(String url) throws IOException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONArray json = (JSONArray) JSONValue.parse(jsonText);
			return json;
		} finally {
			is.close();
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthRadius * c;

		return dist;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {

			ArrayList<String> alId = new ArrayList<String>();
			HashMap<String, Integer> ruleCategory = new HashMap<>();
			ruleCategory.put("Fan", 91);
			ruleCategory.put("Tubelights", 92);
			ruleCategory.put("Vaccum Cleaner", 93);
			ruleCategory.put("Laptop", 94);
			ruleCategory.put("Adapters", 95);
			ruleCategory.put("Mobiles", 96);
			ruleCategory.put("TV", 97);
			ruleCategory.put("Cars", 90);
			ruleCategory.put("Bikes", 98);
			ruleCategory.put("Auxiliaries", 99);
			ruleCategory.put("Table", 100);
			ruleCategory.put("Chairs", 101);
			ruleCategory.put("Mattress", 102);
			ruleCategory.put("Books", 103);
			ruleCategory.put("Default", 73);

			HashMap<String, Double> weightedCondition = new HashMap<>();
			weightedCondition.put("new", 0.0);
			weightedCondition.put("unused", 0.0);
			weightedCondition.put("excellent", 0.0384);
			weightedCondition.put("good", 0.15);
			weightedCondition.put("fair", 0.15);
			weightedCondition.put("bargain grade", 0.34);
			weightedCondition.put("poor", 0.46);

			System.out.println(request.getParameter("lat") + "     dhfjkds     " + request.getParameter("long"));
			double userLat = Double.parseDouble(request.getParameter("lat"));
			double userLong = Double.parseDouble(request.getParameter("long"));
			double itemLat, itemLong;

			String item = request.getParameter("item");

			String URLId = "http://sln.ics.uci.edu:8085/eventshoplinux/rest/sttwebservice/search/" + ruleCategory.get(item) + "/circle/null/null/null";
			JSONArray jsonObjectid = readJsonFromUrlId(URLId);
			for (int p = 0; p < jsonObjectid.size(); p++) {
				JSONObject outer = (JSONObject) jsonObjectid.get(p);
				alId.add((String) outer.get("stt_id"));
			}
			int countWeightLatest = alId.size();
			MediaJSONObject[] mjo = new MediaJSONObject[alId.size()];
			for (int i = 0; i < alId.size(); i++) { // alId.size()
				mjo[i] = new MediaJSONObject();
				String URL = "http://uciconnect.ngss.uci.krumbs.io/event/" + alId.get(i);
				JSONObject jsonObject = readJsonFromUrl(URL);
				JSONArray jsonarray1 = (JSONArray) jsonObject.get("media");
				mjo[i].id = (String) jsonObject.get("id");
				JSONObject inner = (JSONObject) jsonarray1.get(0);
				/*
				 * JSONObject inner1 = (JSONObject) inner.get("when"); if
				 * (inner1.get("start_time") != null && inner1.get("end_time")
				 * != null) { mjo[i].start_time = (String)
				 * inner1.get("start_time"); mjo[i].end_time = (String)
				 * inner1.get("end_time"); }
				 */

				JSONObject inner1 = (JSONObject) inner.get("where");
				JSONObject inner2 = (JSONObject) inner1.get("geo_location");
				if (inner2.get("latitude") != null && inner2.get("longitude") != null) {
					itemLat = (double) inner2.get("latitude");
					itemLong = (double) inner2.get("longitude");
				} else {
					itemLat = 0.0;
					itemLong = 0.0;
				}

				/*
				 * JSONArray jsonarray2 = (JSONArray) inner.get("why");
				 * JSONObject inner3 = (JSONObject) jsonarray2.get(0); if
				 * (inner3.get("intent_category_name") != null &&
				 * inner3.get("intent_name") != null) {
				 * mjo[i].intent_category_name = (String)
				 * inner3.get("intent_category_name"); mjo[i].intent_name =
				 * (String) inner3.get("intent_name"); }
				 */

				inner1 = (JSONObject) inner.get("media_source");
				if (inner1.get("default_src") != null)
					mjo[i].media_source_image = (String) inner1.get("default_src");

				if (inner.get("caption") != null)
					mjo[i].caption = (String) inner.get("caption");

				Pattern number = Pattern.compile("(\\d+)\\s*\\$|\\$\\s*(\\d+)");
				Matcher mat = number.matcher(mjo[i].caption);
				if (mat.find()) {
					String print1 = mat.group(1);
					String print2 = mat.group(2);
					if (print1 != null)
						mjo[i].cost = Double.parseDouble(print1);
					else if (print2 != null)
						mjo[i].cost = Double.parseDouble(print2);
				}
				if (mjo[i].cost == 0.0)
					mjo[i].cost = 100;

				for (String sub : weightedCondition.keySet()) {
					if (mjo[i].caption.toLowerCase().contains(sub)) {
						mjo[i].condition = sub;
						mjo[i].weightCondition = weightedCondition.get(sub);
						break;
					}
				}

				inner = (JSONObject) jsonarray1.get(1);
				inner1 = (JSONObject) inner.get("media_source");
				if (inner1.get("default_src") != null)
					mjo[i].media_source_audio = (String) inner1.get("default_src");

				mjo[i].distanceFromUser = distFrom(itemLat, itemLong, userLat, userLong);
				MediaJSONObject.totalDistance = MediaJSONObject.totalDistance + mjo[i].distanceFromUser;

				mjo[i].weightLatest = countWeightLatest;
				countWeightLatest = countWeightLatest - 1;
				MediaJSONObject.totalWeightLatest = MediaJSONObject.totalWeightLatest + mjo[i].weightLatest;

				MediaJSONObject.totalCost = MediaJSONObject.totalCost + mjo[i].cost;
			}

			Arrays.sort(mjo, new MediaJSONObject());

			String gsonString = new Gson().toJson(mjo);
			System.out.println(gsonString);
			response.getWriter().print(gsonString);
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
